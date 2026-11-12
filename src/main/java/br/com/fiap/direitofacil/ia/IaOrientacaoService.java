package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.auth.ContextoInterno;
import br.com.fiap.direitofacil.dto.request.RegistrarOrientacaoRequest;
import br.com.fiap.direitofacil.dto.response.CasoResponse;
import br.com.fiap.direitofacil.dto.response.DireitoResumoResponse;
import br.com.fiap.direitofacil.dto.response.OrientacaoResponse;
import br.com.fiap.direitofacil.service.CasoService;
import br.com.fiap.direitofacil.service.OrientacaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Passo a passo do caso com RAG: busca na base de conhecimento os trechos sobre o direito,
 * pede ao Gemini um passo a passo em linguagem simples usando só esses trechos e grava a
 * orientação com a fonte dos trechos. A IA explica como pedir; não decide quem tem direito.
 */
@Slf4j
@Service
public class IaOrientacaoService {

    private static final int TRECHOS = 3;

    private static final String PROMPT_SISTEMA = """
            Você escreve orientações para cidadãos brasileiros sobre como pedir um direito social. \
            Use somente as informações dos TRECHOS fornecidos. Nunca acrescente regra, valor, prazo, \
            endereço, telefone ou site que não esteja nos trechos, e nunca diga se a pessoa tem ou \
            não direito: isso já foi decidido antes.

            Escreva o passo a passo assim:
            - de 3 a 7 passos, na ordem em que a pessoa deve fazer;
            - cada passo em uma única frase curta, começando com um verbo, sem numeração;
            - linguagem simples, sem termos jurídicos e sem siglas não explicadas.

            Preencha os campos:
            - encontrada: true somente se os trechos tratarem do direito pedido. Se tratarem de \
            outro direito, responda false e deixe passos vazio.
            - passos: a lista de passos.
            """;

    private final IaBaseConhecimento baseConhecimento;
    private final ChatClient chatClient;
    private final CasoService casoService;
    private final OrientacaoService orientacaoService;
    private final String modelo;

    public IaOrientacaoService(IaBaseConhecimento baseConhecimento, ChatClient chatClient,
                               CasoService casoService, OrientacaoService orientacaoService,
                               @Value("${spring.ai.google.genai.chat.options.model:gemini}") String modelo) {
        this.baseConhecimento = baseConhecimento;
        this.chatClient = chatClient;
        this.casoService = casoService;
        this.orientacaoService = orientacaoService;
        this.modelo = modelo;
    }

    /**
     * Gera e grava a orientação do caso para quem pode vê-lo (o dono ou um gestor); gerar de novo
     * substitui a anterior. Só o direito vai para a IA: nenhum dado da pessoa sai do sistema.
     */
    public OrientacaoResponse gerar(Long casoId) {
        CasoResponse caso = casoService.buscarPorId(casoId);
        RegistrarOrientacaoRequest request = escrever(caso.direito());
        return ContextoInterno.executar(() -> orientacaoService.registrar(casoId, request));
    }

    /** Usada pelo consumidor da fila: gera a orientação de um caso recém-aberto, sem substituir uma que já exista. */
    public boolean gerarParaCasoNovo(Long casoId) {
        return ContextoInterno.executar(() -> {
            CasoResponse caso = casoService.buscarPorId(casoId);
            if (caso.orientacao() != null) {
                return false;
            }
            orientacaoService.registrar(casoId, escrever(caso.direito()));
            return true;
        });
    }

    private RegistrarOrientacaoRequest escrever(DireitoResumoResponse direito) {
        List<Document> trechos = trechosDoDireito(direito);
        if (trechos.isEmpty()) {
            throw new IaLeituraException("A base de conhecimento não tem orientação para este direito.");
        }
        String contexto = trechos.stream().map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));
        String pedido = """
                DIREITO: %s
                ÓRGÃO: %s
                BASE LEGAL: %s

                TRECHOS:
                %s
                """.formatted(direito.nome(), direito.orgao(), direito.baseLegal(), contexto);

        OrientacaoGerada gerada;
        try {
            gerada = chatClient.prompt()
                    .system(PROMPT_SISTEMA)
                    .user(usuario -> usuario.text("{pedido}").param("pedido", pedido))
                    .call()
                    .entity(OrientacaoGerada.class);
        } catch (RuntimeException e) {
            log.warn("Falha ao gerar a orientação de {}: {}", direito.codigo(), e.getMessage());
            throw IaLeituraException.indisponivel("A geração da orientação falhou. Tente novamente em instantes.", e);
        }
        if (gerada == null) {
            throw IaLeituraException.indisponivel("A IA não devolveu a orientação.", null);
        }
        return gerada.paraRequest(fonte(trechos), modelo);
    }

    /**
     * Busca por semelhança e, se algum trecho for do mesmo código do direito, fica só com ele:
     * evita misturar, por exemplo, o BPC do idoso com o da pessoa com deficiência.
     */
    private List<Document> trechosDoDireito(DireitoResumoResponse direito) {
        String pergunta = "Como pedir %s. Órgão: %s. Base legal: %s."
                .formatted(direito.nome(), direito.orgao(), direito.baseLegal());
        List<Document> parecidos = baseConhecimento.buscar(pergunta, TRECHOS);
        List<Document> doMesmoCodigo = parecidos.stream()
                .filter(trecho -> direito.codigo().equals(trecho.getMetadata().get(IaBaseConhecimento.CODIGO)))
                .toList();
        return doMesmoCodigo.isEmpty() ? parecidos : doMesmoCodigo;
    }

    private String fonte(List<Document> trechos) {
        return trechos.stream()
                .map(trecho -> String.valueOf(trecho.getMetadata().getOrDefault(IaBaseConhecimento.FONTE, "")))
                .filter(fonte -> !fonte.isBlank())
                .distinct()
                .collect(Collectors.joining(" | "));
    }
}
