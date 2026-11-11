package br.com.fiap.direitofacil.auth;

import br.com.fiap.direitofacil.config.JwtProperties;
import br.com.fiap.direitofacil.controller.AuthController;
import br.com.fiap.direitofacil.controller.DireitoController;
import br.com.fiap.direitofacil.controller.PainelController;
import br.com.fiap.direitofacil.controller.PessoaController;
import br.com.fiap.direitofacil.service.AuthService;
import br.com.fiap.direitofacil.service.DireitoService;
import br.com.fiap.direitofacil.service.ElegibilidadeService;
import br.com.fiap.direitofacil.service.LaudoService;
import br.com.fiap.direitofacil.service.MonitorCasosService;
import br.com.fiap.direitofacil.service.PainelEventosService;
import br.com.fiap.direitofacil.service.PessoaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regras de acesso da API sem precisar do Oracle: os services são mocks e o token é simulado
 * com o papel na authority (CIDADAO ou GESTOR), como faz o conversor da claim "role".
 */
@WebMvcTest(controllers = {PessoaController.class, DireitoController.class, PainelController.class, AuthController.class})
@Import({SecurityConfig.class, RespostaErroSeguranca.class, SegurancaEndpointsTest.Config.class})
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-32-caracteres!!")
class SegurancaEndpointsTest {

    @TestConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    static class Config {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private PessoaService pessoaService;
    @MockitoBean private LaudoService laudoService;
    @MockitoBean private ElegibilidadeService elegibilidadeService;
    @MockitoBean private DireitoService direitoService;
    @MockitoBean private PainelEventosService painelEventosService;
    @MockitoBean private MonitorCasosService monitorCasosService;
    @MockitoBean private AuthService authService;

    private static RequestPostProcessor cidadao() {
        return jwt().jwt(j -> j.subject("ana.almeida@email.com").claim("role", "CIDADAO").claim("usuarioId", 1))
                .authorities(new SimpleGrantedAuthority("CIDADAO"));
    }

    private static RequestPostProcessor gestor() {
        return jwt().jwt(j -> j.subject("rafaela.gomes@cras.gov.br").claim("role", "GESTOR").claim("usuarioId", 6))
                .authorities(new SimpleGrantedAuthority("GESTOR"));
    }

    @Test
    void semTokenDevolve401ComCorpoPadronizado() throws Exception {
        mockMvc.perform(get("/api/v1/pessoas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/pessoas"));
    }

    @Test
    void cidadaoAutenticadoListaPessoas() throws Exception {
        when(pessoaService.listar(any(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/v1/pessoas").with(cidadao()))
                .andExpect(status().isOk());
    }

    @Test
    void cidadaoNaoCriaDireitoMesmoComCorpoInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/direitos").with(cidadao())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void gestorComCorpoInvalidoRecebe400DaValidacao() throws Exception {
        mockMvc.perform(post("/api/v1/direitos").with(gestor())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Validação falhou"));
    }

    @Test
    void painelDoGestorBloqueiaCidadao() throws Exception {
        mockMvc.perform(get("/api/v1/painel/eventos").with(cidadao()))
                .andExpect(status().isForbidden());
    }

    @Test
    void painelDoGestorLiberaGestor() throws Exception {
        when(painelEventosService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/painel/eventos").with(gestor()))
                .andExpect(status().isOk());
    }

    @Test
    void loginEPublicoEValidaOCorpo() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"x\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
