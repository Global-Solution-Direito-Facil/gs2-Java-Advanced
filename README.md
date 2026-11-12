# ⚖️ Direito Fácil API (Java)

API REST do **Direito Fácil**, a solução CitizenTech do grupo para a Global Solution 2026/2: o cidadão envia o laudo médico, a **regra decide** a quais direitos federais ele tem acesso e a **IA explica** o passo a passo, e cada pedido (caso) é acompanhado até a concessão.

O backend foi desenvolvido com **Java, Spring Boot, Oracle Database, Flyway, Spring Security com JWT, RabbitMQ e Spring AI (Google Gemini)**. É a mesma API consumida pelo app Mobile.

---

# 📹 Demonstração

- **Vídeo pitch (até 5 minutos):** _link a adicionar_

# 🌐 API publicada

- **Deploy:** _link a adicionar, se houver._ Enquanto isso, a API roda localmente seguindo a seção "Como executar".

---

# 👥 Integrantes da Equipe

| Nome                               | RM     | Turma  | GitHub                                          | LinkedIn                                                            |
| ---------------------------------- | ------ | ------ | ----------------------------------------------- | ------------------------------------------------------------------- |
| **Alexander Dennis Isidro Mamani** | 565554 | 2TDSPG | [alex-isidro](https://github.com/alex-isidro)   | [LinkedIn](https://www.linkedin.com/in/alexander-dennis-a3b48824b/) |
| **Gabriel Neris Losano**           | 564093 | 2TDSPG | [GNLosano](https://github.com/GNLosano)         | [LinkedIn](https://www.linkedin.com/in/gabriellosano/)              |
| **João Vítor Biribilli Ravelli**   | 565594 | 2TDSPG | [biribillidev](https://github.com/biribillidev) | —                                                                   |
| **Kelson Zhang**                   | 563748 | 2TDSPG | [KelsonZh0](https://github.com/KelsonZh0)       | [LinkedIn](https://www.linkedin.com/in/kelson-zhang-211456323/)     |
| **Pietro Paranhos Wilhelm**        | 561378 | 2TDSPG | —                                               | —                                                                   |

---

# 📌 Sobre o Direito Fácil

Muitas famílias têm um laudo médico na gaveta e não sabem que ele dá acesso a uma série de direitos (BPC, passe livre, isenção de IPI, CIPTEA, cotas, meia-entrada...). O Direito Fácil:

- cadastra os beneficiários de cada cidadão (por exemplo, o filho autista);
- registra os laudos e avalia **20 direitos federais** com a regra do Oracle (`PKG_ELEGIBILIDADE`);
- abre um **caso** para cada direito `ELEGIVEL` ou `POSSIVEL` e acompanha o status até a concessão;
- avisa o assistente social (gestor) sobre direitos identificados, prazos vencendo e casos parados, via **RabbitMQ**;
- consulta a receita no **elenco oficial do Farmácia Popular** e diz o que é gratuito.

**A regra decide, a IA explica:** quem decide se a pessoa tem direito é a função PL/SQL, de forma determinística, a partir dos critérios em tabela. A IA (Spring AI) só extrai os dados do laudo e da receita e redige o passo a passo citando a fonte.

O banco de dados (scripts Oracle, PL/SQL e MongoDB) está no repositório [DireitoFacil-DB](https://github.com/Global-Solution-Direito-Facil/DireitoFacil-DB). As migrations `V1` a `V5` deste projeto são as mesmas de lá.

---

# ✅ Requisitos da disciplina

| # | Requisito | Onde está |
|---|---|---|
| 01 | API REST: 3+ entidades relacionadas, CRUD com Spring Data JPA e paginação | Pessoa, Direito, Caso, Documento, Laudo, Receita... CRUD completo de Pessoa e Direito; listagens com `Pageable` e `PageResponse` |
| 02 | Bean Validation nos DTOs e `@RestControllerAdvice` | `dto/request` (`@NotBlank`, `@Size`, `@Cpf`, `@AssertTrue`...) e `exception/GlobalExceptionHandler` com `ErroResponse` padronizado |
| 03 | Spring Security + JWT, 2 papéis, endpoints protegidos por papel | `CIDADAO` e `GESTOR`; `SecurityConfig` e `@PreAuthorize` nos controllers |
| 04 | Spring AI integrado ao fluxo | Leitura do laudo e da receita (multimodal, saída estruturada) e passo a passo do caso com RAG: pacote `ia` e seção [Spring AI](#-spring-ai) |
| 05 | Arquitetura distribuída: **opção A (RabbitMQ)** | Eventos de caso publicados e consumidos de forma assíncrona, e consumo do evento `DocumentoRecebido` da API .NET: seção [RabbitMQ](#-requisito-5--opção-a-rabbitmq) |
| 06 | Swagger/OpenAPI, README, API consumida pelo Mobile | `/swagger-ui.html`, este README e a seção [Integração](#-integração-com-o-mobile-e-a-net) |

---

# 🧰 Tecnologias utilizadas

- Java 25
- Spring Boot 4.1.2 (Spring MVC)
- Spring Data JPA (Hibernate)
- Spring Security + OAuth2 Resource Server
- JWT assinado com HS256 (chave compartilhada com a API .NET)
- BCrypt
- Oracle Database + PL/SQL (pacotes chamados com `SimpleJdbcCall`)
- Flyway
- Spring AMQP (RabbitMQ) e `@Retryable` do Spring (resiliência)
- `@Scheduled` (monitor de prazos)
- Spring AI 2.0.1 com Google Gemini: chat multimodal, saída estruturada, embeddings e vector store (RAG)
- Springdoc OpenAPI / Swagger
- Lombok
- Maven Wrapper
- Docker Compose (RabbitMQ)

---

# 🏗️ Arquitetura

A aplicação segue a arquitetura em camadas:

```text
Controller
    ↓
Service  ──→  RabbitMQ (eventos de caso)
    ↓
Repository / PlSqlRepository
    ↓
Oracle Database (tabelas T_DF_* e pacotes PL/SQL)
```

```text
src/main/java/br/com/fiap/direitofacil/

├── auth/            Segurança: SecurityConfig, usuário autenticado, respostas 401/403
├── config/          JWT, Swagger, Spring AI (IaConfig), endereço da .NET e conversor S/N
├── controller/      Endpoints REST (/api/v1/...); os de IA começam com "Ia"
├── ia/              Spring AI: leitura do laudo e da receita e passo a passo com RAG
├── integracao/      Cliente HTTP da API .NET (download do arquivo e status do documento)
├── dto/
│   ├── request/     Dados recebidos, com Bean Validation
│   └── response/    Dados devolvidos
├── entity/          Entidades JPA (tabelas T_DF_*)
├── enums/           Enumerações do domínio (mesmos valores dos CHECK do banco)
├── exception/       GlobalExceptionHandler e ErroResponse
├── mensageria/      RabbitConfig, publicador e consumidores dos eventos
├── repository/      Spring Data JPA e chamadas aos pacotes PL/SQL
├── service/         Regras da aplicação
├── specification/   Filtros dinâmicos de casos
└── validation/      Validadores de CPF e e-mail
```

```text
src/main/resources/

├── application.properties
├── conhecimento/    Base de conhecimento do RAG: como pedir cada direito, com a fonte
└── db/migration/    V1 a V5 (schema, procedures de carga, carga, pacotes/gatilhos, casos da demo)
```

---

# 🔐 Segurança

A API usa **Spring Security** com autenticação **JWT** e senhas em **BCrypt**.

- O token é assinado com **HS256** e a chave da variável `JWT_SECRET`, usada como **texto UTF-8** (sem decodificar Base64). É a mesma chave da API .NET, que valida o mesmo token.
- Claims: `iss` = `direitofacil`, `aud` = `direitofacil-app`, `sub` = e-mail, `email`, `role` = `CIDADAO` ou `GESTOR` (sem o prefixo `ROLE_`), `usuarioId` e `nome`. O token vale 8 horas.
- O token emitido pela .NET (sem `usuarioId`) também é aceito: a conta é localizada pelo e-mail do `sub`.
- O papel é lido da claim `role` e conferido no filtro de segurança e nos controllers com `@PreAuthorize`.
- Sem token, com token inválido ou expirado: **401**. Papel errado: **403**. Os dois devolvem o mesmo corpo de erro da API.

# 👤 Perfis e permissões

## CIDADAO

Responsável que cadastra os próprios beneficiários. Pode:

- cadastrar, editar e excluir os próprios beneficiários e ver a elegibilidade deles;
- ver os próprios casos, o histórico e a orientação de cada caso;
- registrar documentos e receitas dos próprios beneficiários;
- consultar o catálogo de direitos e o elenco do Farmácia Popular.

O cidadão só acessa o que é dos próprios beneficiários (tentativa em dado de outra pessoa: 403).

## GESTOR

Assistente social. Além de ver tudo, pode:

- manter o catálogo de direitos (criar, alterar, excluir);
- registrar laudos estruturados;
- mudar o status dos casos (o gatilho do banco valida o fluxo e grava a auditoria);
- registrar a orientação de cada caso;
- ver o painel de avisos (RabbitMQ) e rodar a verificação de prazos;
- criar outros gestores.

# 🔓 Endpoints públicos

```text
POST /api/v1/auth/login
POST /api/v1/auth/registrar

/swagger-ui.html
/swagger-ui/**
/v3/api-docs/**
```

Todos os outros exigem o token.

# 🔑 Autenticação

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "ana.almeida@email.com",
  "senha": "Senha@123"
}
```

Resposta:

```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-11T16:50:00Z",
  "role": "CIDADAO",
  "usuarioId": 1
}
```

Nos demais endpoints, envie o cabeçalho `Authorization: Bearer <token>`. No Swagger, use o botão **Authorize**.

---

# 👥 Usuários de demonstração

Criados pela migration `V3__carga_inicial.sql`. Todos com a senha **`Senha@123`** (gravada em BCrypt).

| E-mail | Papel | Beneficiário |
|---|---|---|
| ana.almeida@email.com | CIDADAO | Lucas, 7 anos, autismo |
| carlos.menezes@email.com | CIDADAO | Carlos, cadeirante |
| paula.souza@email.com | CIDADAO | Helena, 68 anos |
| marcos.teixeira@email.com | CIDADAO | Marcos, sem deficiência (teste negativo) |
| rita.barbosa@email.com | CIDADAO | Rita, 66 anos, renda no limite |
| rafaela.gomes@cras.gov.br | GESTOR | assistente social |

> Dados, pessoas e laudos fictícios, apenas para demonstração.

---

# 🚀 Como executar o projeto do zero

## 1. Pré-requisitos

- **JDK 25** (`java -version`);
- Git;
- **Docker** (para o RabbitMQ);
- acesso a um schema Oracle.

## 2. Clonar o repositório

```bash
git clone https://github.com/Global-Solution-Direito-Facil/gs2-Java-Advanced.git
cd gs2-Java-Advanced
```

## 3. Subir o RabbitMQ

```bash
docker compose up -d
```

O painel do RabbitMQ fica em `http://localhost:15672` (usuário `myuser`, senha `secret`).

## 4. Preparar o Oracle

O Flyway cria as tabelas, os pacotes PL/SQL e os dados de demonstração na primeira subida.

- O schema pode ter objetos de outros projetos: a aplicação usa `baseline-on-migrate` com versão 0 e uma tabela de histórico própria (`FLYWAY_DIREITOFACIL_HISTORY`).
- As tabelas `T_DF_*` **não podem existir antes**. Se você já rodou os scripts do repositório do banco no mesmo schema, rode antes o `00_DROP.sql` de lá.

## 5. Configurar as variáveis de ambiente

| Variável | Obrigatória | Exemplo / padrão |
|---|---|---|
| `DB_URL` | sim | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USERNAME` | sim | seu usuário (RM) |
| `DB_PASSWORD` | sim | sua senha |
| `JWT_SECRET` | sim | chave com **pelo menos 32 caracteres**; a mesma da API .NET |
| `JWT_EXPIRATION_MINUTES` | não | `480` (8 horas) |
| `JWT_ISSUER` / `JWT_AUDIENCE` | não | `direitofacil` / `direitofacil-app` (iguais aos da .NET) |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | não | `localhost` / `5672` |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | não | `myuser` / `secret` (iguais ao `docker-compose.yml`) |
| `RABBITMQ_VHOST` / `RABBITMQ_SSL` | não | `/` / `false` |
| `DOTNET_API_URL` | não | URL da API .NET, sem o prefixo (ex.: `https://host`); sem ela as chamadas para a .NET ficam indisponíveis |
| `DOTNET_API_PREFIX` | não | `/api/v1` |
| `GEMINI_API_KEY` | não | chave do Google Gemini (Google AI Studio); sem ela a API sobe e só as funções de IA respondem 503 |
| `GEMINI_CHAT_MODEL` / `GEMINI_EMBEDDING_MODEL` | não | `gemini-2.5-flash` / `gemini-embedding-001` |
| `MONITOR_CRON` | não | `0 0 8 * * *` (todo dia às 8h) |
| `PORT` | não | `8080` |

Sem `JWT_SECRET` (ou com menos de 32 caracteres) a aplicação não sobe e avisa o motivo. `GEMINI_API_KEY` e `DOTNET_API_URL` são opcionais: sem elas todo o restante da API funciona, e o laudo pode ser registrado pelo gestor em `POST /api/v1/laudos`.

### Windows (PowerShell)

```powershell
$env:DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
$env:DB_USERNAME="SEU_RM"
$env:DB_PASSWORD="SUA_SENHA"
$env:JWT_SECRET="uma-chave-com-pelo-menos-32-caracteres"
```

### IntelliJ IDEA

`Run → Edit Configurations → DireitoFacilApplication → Environment variables`, com o JDK 25 selecionado:

```text
DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL;DB_USERNAME=SEU_RM;DB_PASSWORD=SUA_SENHA;JWT_SECRET=uma-chave-com-pelo-menos-32-caracteres
```

### Linux/macOS

```bash
export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
export DB_USERNAME="SEU_RM"
export DB_PASSWORD="SUA_SENHA"
export JWT_SECRET="uma-chave-com-pelo-menos-32-caracteres"
```

## 6. Executar

```bash
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # Linux/macOS
```

Na primeira subida o log mostra:

```text
Successfully baselined schema with version: 0      (só quando o schema já tem objetos de outros projetos)
Successfully applied 5 migrations ...
Started DireitoFacilApplication
```

## 7. Swagger

```text
http://localhost:8080/swagger-ui.html
```

---

# 📚 Principais endpoints

Todos com o prefixo `/api/v1`.

## Autenticação

| Método | Rota | Quem |
|---|---|---|
| POST | `/auth/login` | público |
| POST | `/auth/registrar` | público (cria um CIDADAO e devolve o token) |
| PATCH | `/auth/senha` | logado |

## Pessoas e elegibilidade

| Método | Rota | Quem |
|---|---|---|
| GET | `/pessoas` (paginado, filtro `nome` para o gestor) | logado |
| GET / PUT / DELETE | `/pessoas/{id}` | dono ou gestor |
| POST | `/pessoas` | logado (o gestor informa `usuarioId`) |
| GET | `/pessoas/{id}/laudos` | dono ou gestor |
| GET | `/pessoas/{id}/elegibilidade` | dono ou gestor |

Ao cadastrar uma pessoa, a regra PL/SQL já abre os casos. Ao atualizar (por exemplo, a renda), a API chama `PRC_REAVALIAR_CASOS`. `rendaIndividual` nula significa "não sei". O DELETE devolve 409 se algum caso já saiu de `ABERTO`.

## Direitos

| Método | Rota | Quem |
|---|---|---|
| GET | `/direitos` (paginado, filtro `grupoAlvo`), `/direitos/{id}`, `/direitos/codigo/{codigo}` | logado |
| POST / PUT / DELETE | `/direitos`, `/direitos/{id}` | GESTOR |
| GET | `/salario-minimo` | logado |

## Laudos e documentos

| Método | Rota | Quem |
|---|---|---|
| POST | `/laudos` | GESTOR (ou a extração da IA) |
| GET | `/laudos/{id}` | dono ou gestor |
| GET | `/documentos` (paginado) / `/documentos/{id}` | dono ou gestor |
| POST | `/documentos` (idempotente pela `chaveIdempotencia`) | dono ou gestor |
| DELETE | `/documentos/{id}` (409 se já virou laudo ou receita) | dono ou gestor |

## Casos

| Método | Rota | Quem |
|---|---|---|
| GET | `/casos` (paginado, filtros `pessoaId`, `status`, `resultado`) | logado |
| GET | `/casos/me` | CIDADAO |
| GET | `/casos/{id}`, `/casos/{id}/historico`, `/casos/{id}/orientacao` | dono ou gestor |
| PATCH | `/casos/{id}/status` | GESTOR |
| PUT | `/casos/{id}/orientacao` | GESTOR |

## Farmácia Popular

| Método | Rota | Quem |
|---|---|---|
| GET | `/farmacia-popular/medicamentos` (paginado, filtro `indicacao`) | logado |
| GET | `/farmacia-popular/consulta?principioAtivo=&concentracao=` | logado |
| GET / POST | `/receitas` | logado |
| GET / DELETE | `/receitas/{id}` | dono ou gestor |
| POST | `/receitas/{id}/itens` | dono ou gestor |

## Inteligência artificial (Spring AI)

| Método | Rota | Quem |
|---|---|---|
| POST | `/receitas/extracao` (lê a foto da receita e devolve os remédios para conferência) | dono do documento |
| POST | `/casos/{id}/orientacao/gerar` (gera o passo a passo com RAG e grava no caso) | dono ou gestor |

A leitura do laudo não tem rota: ela acontece sozinha quando a API .NET publica o evento `DocumentoRecebido`.

## Painel do gestor e usuários

| Método | Rota | Quem |
|---|---|---|
| GET | `/painel/eventos` | GESTOR |
| POST | `/painel/verificar-prazos?diasPrazo=&diasParado=` (202) | GESTOR |
| GET / POST | `/gestores` | GESTOR |
| GET | `/usuarios/me`, `/usuarios/{id}` | logado (cidadão só a própria conta) |
| PUT | `/usuarios/{id}` | logado (cidadão só a própria conta) |

---

# 📨 Requisito 5 — Opção A (RabbitMQ)

O grupo escolheu a **opção A**: evento publicado e consumido de forma assíncrona.

```text
API (service)  --convertAndSend-->  exchange direitofacil.casos.exchange (topic)
                                         │  binding "caso.#"
                                         ▼
                                   fila direitofacil.painel-gestor (durável)
                                         │
                                   @RabbitListener (PainelGestorListener)
                                         ▼
                                   GET /api/v1/painel/eventos
```

| Evento | Routing key | Quando |
|---|---|---|
| `DIREITO_IDENTIFICADO` | `caso.direito-identificado` | a regra PL/SQL abriu um caso novo (cadastro de pessoa, mudança de renda ou laudo) |
| `PRAZO_VENCENDO` | `caso.prazo-vencendo` | caso em andamento com prazo nos próximos 7 dias |
| `CASO_PARADO` | `caso.parado` | caso em andamento sem atualização há 15 dias |

- **Configuração:** `RabbitConfig`, com constantes, `TopicExchange`, fila durável e `Binding`.
- **Publicação:** `EventoCasoPublisher` usa `rabbitTemplate.convertAndSend`. Se o RabbitMQ falhar, `@Retryable` tenta de novo com backoff exponencial (3 tentativas, 500 ms, multiplicador 2). Se ainda assim falhar, a requisição do cidadão não quebra: o aviso só vai para o log.
- **Agendamento:** `MonitorCasosService` roda com `@Scheduled` todo dia às 8h. Para a demonstração, o gestor pode rodar na hora com `POST /api/v1/painel/verificar-prazos`, que responde **202 Accepted**: os eventos vão para a fila e o consumidor processa depois.
- **Consumo:** `PainelGestorListener` recebe cada evento e o guarda em memória (até 200, os mais recentes primeiro). O histórico oficial do caso é a auditoria do banco.

Além do painel, a API tem mais dois consumidores:

| Fila | Consumidor | O que faz |
|---|---|---|
| `direitofacil.orientacao-ia` (binding `caso.direito-identificado`) | `OrientacaoIaListener` | a cada caso aberto, gera o passo a passo com RAG e grava no caso |
| `documentos.recebidos` (exchange padrão, publicada pela API .NET) | `DocumentoRecebidoListener` | a cada laudo enviado pelo app, baixa o arquivo, lê com a IA e grava o laudo |

Nenhum dos dois devolve a mensagem para a fila em caso de falha: o erro fica no log e no status do documento, e a operação pode ser refeita depois.

Exemplo de mensagem (JSON):

```json
{
  "tipo": "DIREITO_IDENTIFICADO",
  "casoId": 32,
  "pessoaId": 6,
  "pessoaNome": "Dona Maria Demonstracao",
  "codigoDireito": "BPC_IDOSO",
  "nomeDireito": "BPC/LOAS - Idoso 65+",
  "status": "ABERTO",
  "resultadoAvaliacao": "ELEGIVEL",
  "motivo": "Renda per capita 0.2159 SM dentro do limite de 0.2500 SM",
  "dataPrazo": "2026-12-26T12:12:43",
  "dataAtualizacao": "2026-11-11T12:12:43",
  "ocorridoEm": "2026-11-11T12:12:43"
}
```

---

# 💊 Farmácia Popular

Funcionalidade separada dos direitos: não abre casos. A IA lê a foto da receita, **a pessoa confere na tela** e só então o app grava cada remédio em `POST /receitas/{id}/itens`.

Quem decide é a tabela `T_DF_MEDICAMENTO_FP`, com o elenco oficial de 41 itens (atualizado em 31/08/2026), pelo pacote `PKG_FARMACIA_POPULAR`. A IA não decide nada aqui.

| Resultado | Significa |
|---|---|
| `GRATUITO` | princípio ativo e dosagem estão no elenco |
| `DOSAGEM_DIVERGENTE` | o princípio está no elenco, a dosagem não |
| `NAO_ENCONTRADO` | não consta no elenco gratuito |

- A comparação ignora acento e maiúsculas, aceita o princípio sem o sal (`metformina` acha `cloridrato de metformina`) e trata vírgula e ponto como iguais (`2.5 mg` = `2,5mg`).
- A orientação nunca sugere trocar o remédio: só informa o resultado e manda confirmar na farmácia credenciada, levando receita válida e documento com foto e CPF.

---

# 🤖 Spring AI

A IA usa **Spring AI 2.0.1 com o Google Gemini** e está toda no pacote `ia`. Ela tem três funções, e em nenhuma decide se a pessoa tem direito: ela lê documentos e explica como pedir.

| Função | Classe | Recurso do Spring AI | Quando roda |
|---|---|---|---|
| Ler o laudo | `IaLaudoService` | multimodal (foto ou PDF) + saída estruturada (`.entity`) | evento `DocumentoRecebido` da API .NET |
| Ler a receita | `IaReceitaService` | multimodal + saída estruturada | `POST /api/v1/receitas/extracao` |
| Escrever o passo a passo | `IaOrientacaoService` | RAG: embeddings + vector store + saída estruturada | evento `DIREITO_IDENTIFICADO` ou `POST /api/v1/casos/{id}/orientacao/gerar` |

## Leitura do laudo

```text
App → API .NET (upload) → RabbitMQ documentos.recebidos → DocumentoRecebidoListener
        │
        ├─ identifica o beneficiário (pessoaId do evento, conferido com a conta que enviou)
        ├─ baixa o arquivo na .NET com o token SERVICO
        ├─ Gemini lê e devolve CID, condição, tipo da condição e data
        ├─ LaudoService grava o laudo → PRC_REAVALIAR_CASOS abre os casos
        └─ PATCH na .NET: EXTRAIDO ou FALHA
```

- A IA só transcreve: temperatura 0, e o prompt proíbe deduzir ou opinar sobre direitos. Se faltar CID, condição ou data, o laudo **não é gravado**.
- O mesmo evento entregue duas vezes não gera um segundo laudo (a chave de idempotência é o `eventId`).
- Se o evento não trouxer `pessoaId`, vale o único beneficiário da conta. Com mais de um, a leitura é marcada como `FALHA` em vez de adivinhar.

## Leitura da receita

`POST /api/v1/receitas/extracao` recebe o id do documento na .NET e devolve os remédios lidos:

```json
[
  { "nome": "Losartana Potássica", "principioAtivo": "losartana potássica", "dosagem": "50 mg" }
]
```

- Nada é gravado: a pessoa confere na tela e só depois a tabela do Farmácia Popular decide o que é gratuito.
- O arquivo é baixado com o token de quem fez o pedido, e não com o de serviço. Assim a própria .NET recusa o documento de outra pessoa.
- O princípio ativo só é preenchido se estiver escrito na receita. A IA nunca sugere trocar remédio ou dosagem.

## Passo a passo com RAG

```text
caso aberto → evento DIREITO_IDENTIFICADO → OrientacaoIaListener
        │
        ├─ busca por semelhança na base de conhecimento (embeddings do Gemini)
        ├─ Gemini escreve de 3 a 7 passos usando só os trechos encontrados
        └─ grava a orientação no caso, com a fonte do trecho usado
```

- **Base de conhecimento:** `src/main/resources/conhecimento/direitos.md`, com um texto para cada um dos 20 direitos do catálogo ("como pedir" e a fonte). Um teste garante que todo direito do banco tem texto na base.
- **A fonte não é escrita pela IA:** ela vem do trecho que foi realmente usado, então a IA não tem como citar uma lei que não existe.
- **Nenhum dado da pessoa vai para o modelo:** só o nome do direito, o órgão e a base legal.
- **Sem trecho, sem orientação:** se a base não tiver o direito, nada é gravado e o app mostra a base legal.
- **Vector store em memória** (`SimpleVectorStore`): o Oracle 19c da FIAP não tem tipo vetorial e a base é pequena. A indexação acontece na primeira consulta, não na subida.
- Os casos da carga de demonstração não passam pelo evento. Para eles, use `POST /api/v1/casos/{id}/orientacao/gerar`.

## Erros da IA

| Código | Quando |
|---|---|
| 422 | o arquivo não é um laudo ou uma receita legível, ou a base não tem orientação para o direito |
| 503 | `GEMINI_API_KEY` ausente ou Gemini indisponível |
| 404 | documento inexistente na .NET ou de outra pessoa |
| 502 | API .NET fora do ar |

---

# 📱 Integração com o Mobile e a .NET

- **App Mobile:** faz login só nesta API (`POST /api/v1/auth/login`) e consome as rotas `/api/v1/...`. As listagens são paginadas (`content`, `page`, `size`, `totalElements`, `totalPages`).
- **API .NET (documentos e envios):** tem banco próprio. Aceita o token emitido por esta API: HS256, mesma `JWT_SECRET`, mesmos `iss` e `aud`, `sub` = e-mail, `role` sem prefixo. O e-mail liga os documentos à pessoa.
- **Chamadas do Java para a .NET (`integracao/DocumentosDotnetClient`):** `GET /api/v1/documentos/{id}/arquivo` para baixar o arquivo e `PATCH /api/v1/documentos/{id}/status` com `EXTRAIDO` ou `FALHA`. As duas usam um token de papel `SERVICO`, gerado pelo próprio Java, que vale 5 minutos e nunca passa pelo app.
- **Evento `DocumentoRecebido`:** publicado pela .NET na fila `documentos.recebidos` quando o arquivo chega, e consumido aqui pelo `DocumentoRecebidoListener` (seção [Spring AI](#-spring-ai)). O `pessoaId` é opcional:

```json
{
  "eventId": "8c1f0c5e-1f0a-4a55-9d7b-2f6f0d1b7a11",
  "documentoId": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
  "cidadaoId": "ana.almeida@email.com",
  "pessoaId": 1,
  "tipoDocumento": "LAUDO",
  "contentType": "application/pdf",
  "criadoEm": "2026-11-11T14:30:00Z"
}
```

- **Sem a .NET no ar**, a API continua funcionando: o gestor registra o laudo por `POST /api/v1/laudos` e o documento por `POST /api/v1/documentos`.

---

# ✅ Validações e erros

Toda falha devolve o mesmo corpo (`ErroResponse`):

```json
{
  "timestamp": "2026-11-11T11:50:54",
  "status": 400,
  "erro": "Validação falhou",
  "mensagem": "Campos inválidos na requisição",
  "path": "/api/v1/auth/registrar",
  "detalhes": ["cpf: CPF inválido", "senha: tamanho deve ser entre 8 e 72"]
}
```

| Código | Quando |
|---|---|
| 200 / 201 / 204 | sucesso |
| 202 | verificação de prazos aceita (eventos na fila) |
| 400 | corpo ou parâmetro inválido |
| 401 | sem token, token inválido ou senha errada |
| 403 | papel errado ou dado de outra pessoa |
| 404 | registro não encontrado (inclui `ORA-20022`, `-20023`, `-20040`, `-20041`, `-20051`, `-20060`, `-20061`) |
| 409 | duplicado ou fluxo de status inválido (`ORA-20032`, `-20033`) |
| 422 | regra violada (`ORA-20031`, `-20034`, senha atual errada, documento de outra pessoa) ou arquivo que a IA não conseguiu ler |
| 502 | a API .NET não respondeu ao download do arquivo |
| 503 | IA indisponível (sem `GEMINI_API_KEY` ou Gemini fora do ar) ou API .NET não configurada |

Os erros `ORA-200xx` vêm dos pacotes e gatilhos PL/SQL. A mensagem do banco vai em `mensagem`, e o código em `detalhes`.

---

# 🧪 Testando a API com Insomnia

O repositório tem a coleção `DireitoFacil-Insomnia.json`, com 57 requisições em 10 pastas, na ordem da demonstração.

1. No Insomnia: `Import → File → DireitoFacil-Insomnia.json`.
2. No `Base Environment`, confira `base_url` (`http://localhost:8080`).
3. Rode `00 - Auth` e copie os tokens para `token_gestor`, `token_cidadao` (Ana) e `token_cidadao_helena` (Paula). Eles valem 8 horas.
4. Nas requisições que criam algo (direito de teste, pessoa nova, documento, receita), copie o `id` devolvido para a variável indicada na descrição.

As variáveis já vêm com os ids de um banco recém-criado pelo Flyway. Cada requisição diz na descrição o status esperado.

# 🧪 Testes automatizados

```bash
mvnw.cmd clean test      # Windows
./mvnw clean test        # Linux/macOS
```

- `SegurancaEndpointsTest` (MockMvc, sem banco): 401 sem token com o corpo padrão, 403 para papel errado (antes da validação do corpo), 400 de validação para o gestor, painel restrito ao GESTOR e login público.
- `TokenServiceTest` (sem banco): contrato do token com a .NET (HS256, `sub` = e-mail, `role` sem `ROLE_`), token recusado com outra chave e chave curta impedindo a subida.
- `DocumentosDotnetClientTest` (sem banco, servidor HTTP local): rotas, token `SERVICO` e corpo do `PATCH` nas chamadas para a .NET, e o comportamento quando ela falha.
- `ProcessamentoDocumentoServiceTest` (sem banco): o caminho do laudo que chega pela fila, incluindo evento repetido, beneficiário de outra conta e falha da leitura.
- `LaudoLidoTest`, `ReceitaLidaTest` e `OrientacaoGeradaTest` (sem IA): a conferência do que a IA devolve antes de gravar (dados obrigatórios, datas, limites das colunas).
- `IaReceitaServiceTest` e `IaOrientacaoServiceTest` (sem IA): download em nome de quem pediu, escolha dos trechos do RAG e origem da fonte.
- `IaBaseConhecimentoTest`: todo direito do catálogo do banco tem texto na base de conhecimento.
- `DireitoFacilApplicationTests`: sobe o contexto completo. Só roda com `DB_URL` e `JWT_SECRET` definidas.

---

# ❗ Solução de problemas

| Problema | Causa provável |
|---|---|
| A aplicação não sobe e fala de `JWT_SECRET` | variável ausente ou com menos de 32 caracteres |
| Erro do Flyway com tabela `T_DF_*` já existente | o schema já tem as tabelas: rode o `00_DROP.sql` do repositório do banco |
| `401` em endpoint protegido | token ausente ou expirado: faça login de novo |
| `403` | papel errado (ex.: cidadão em rota de gestor) ou dado de outra pessoa |
| Avisos não aparecem no painel | RabbitMQ fora: rode `docker compose up -d` (a API continua funcionando sem ele) |
| `503` nas rotas de IA | `GEMINI_API_KEY` ausente ou inválida, ou limite de requisições do Gemini atingido |
| Caso sem orientação | caso da carga de demonstração ou IA indisponível na hora: use `POST /api/v1/casos/{id}/orientacao/gerar` |
| `401` da .NET ao baixar o arquivo | `JWT_SECRET`, issuer ou audience diferentes entre as duas APIs |
| `404` da .NET em todas as chamadas | prefixo das rotas diferente: ajuste `DOTNET_API_PREFIX` |

---

# 📝 Declaração de uso de IA generativa

Ferramenta: **Claude (Anthropic)**, usada como apoio pelo integrante responsável (João Vitor Biribilli Ravelli) para gerar rascunhos de código, testes, coleção do Insomnia e deste README, a partir dos requisitos da disciplina e do padrão ensinado em aula (projeto PetPulse e aulas de Mensageria e Resiliência). O código foi executado e conferido contra o Oracle e o RabbitMQ reais antes de cada commit.

A funcionalidade de IA do produto (Spring AI) é descrita na seção [Spring AI](#-spring-ai).

---

# 📌 Repositórios

- API Java (este): https://github.com/Global-Solution-Direito-Facil/gs2-Java-Advanced
- Banco de dados (Oracle e MongoDB): https://github.com/Global-Solution-Direito-Facil/DireitoFacil-DB
