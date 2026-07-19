# Sistema de Gestão de Solicitações de Documentos Acadêmicos

Desafio Programador I — Unoesc (Edital N. 21/UNOESC-R/2025)

API REST para automatizar a solicitação de documentos acadêmicos (Histórico
Escolar, Atestado de Matrícula, Declaração de Conclusão etc.), com fluxo de
aprovação por etapas, dashboard de acompanhamento e autenticação via JWT.

## Stack

- Java 21
- Spring Boot 3.3.4 (Web, Data JPA, Security, Validation, Thymeleaf)
- PostgreSQL
- Maven
- JWT (io.jsonwebtoken)
- Thymeleaf (dashboard e tela de solicitações)
- Docker / Docker Compose

## Como rodar — com Docker (recomendado)

Único pré-requisito: **Docker** e **Docker Compose** instalados.

```bash
docker compose up --build
```

Isso sobe dois containers: o banco PostgreSQL e a aplicação (que espera o
banco ficar saudável antes de iniciar). Na primeira vez que a aplicação
subir, ela já cria as tabelas (`ddl-auto=update`) e semeia um usuário
administrador padrão.

Aplicação disponível em `http://localhost:8080`.

Para parar: `Ctrl+C` e depois `docker compose down` (ou `docker compose down -v`
se quiser também apagar os dados do banco).

## Como rodar — sem Docker

Pré-requisitos:
- Java 21
- Maven (ou use o `./mvnw` / `mvnw.cmd` incluído no projeto, não precisa instalar Maven à parte)
- PostgreSQL rodando localmente, com um banco criado (por padrão, `gestao_documentos`)

Configure as variáveis de ambiente (ou ajuste os valores padrão direto em
`src/main/resources/application.properties`):

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `gestao_documentos` | Nome do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `JWT_SECRET` | (chave de exemplo já incluída) | Chave usada pra assinar o JWT — troque em produção |
| `ADMIN_USERNAME` | `admin` | Usuário administrador semeado no primeiro start |
| `ADMIN_PASSWORD` | `admin123` | Senha do administrador semeado |

Rodando:

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

(No Windows: `mvnw.cmd clean package -DskipTests` e `mvnw.cmd spring-boot:run`)

## Login padrão

Um usuário administrador é criado automaticamente no primeiro start:

```
usuário: admin
senha:   admin123
```

Use esse usuário no `POST /auth/login` (ou na tela de login em `/dashboard`
ou `/solicitacoes`) para obter o token JWT.

## Telas

- **`/dashboard`** — estatísticas (solicitações por status, por período,
  documentos mais solicitados, tempo médio até emissão).
- **`/solicitacoes`** — criar novas solicitações e avançar o status delas
  pelo fluxo de aprovação, direto pela interface.
- **`/cadastros`** — criar, listar e remover Alunos, Cursos e Tipos de Documento (RF01.1).

## Documentação da API (Swagger)

Com a aplicação rodando: `http://localhost:8080/swagger-ui.html`

## Autenticação

Todos os endpoints de `/api/**` exigem um token JWT no header:

```
Authorization: Bearer <token>
```

O token é obtido em `POST /auth/login`:
```json
{ "username": "admin", "senha": "admin123" }
```

## Principais endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/login` | Autentica e devolve o token JWT |
| `POST` | `/api/solicitacoes` | Cria uma solicitação (RF01) |
| `GET` | `/api/solicitacoes` | Lista com filtros (aluno, curso, status, período, tipo de documento) e paginação (RF02) |
| `GET` | `/api/solicitacoes/aluno/{alunoId}` | Solicitações de um aluno específico (RF02) |
| `GET` | `/api/solicitacoes/estatisticas/por-status` | Quantidade por status (RF02/RF06) |
| `GET` | `/api/solicitacoes/estatisticas/por-periodo` | Quantidade por período (RF02/RF06) |
| `GET` | `/api/solicitacoes/estatisticas/documentos-mais-solicitados` | Ranking de documentos (RF02/RF06) |
| `GET` | `/api/solicitacoes/estatisticas/tempo-medio-emissao` | Tempo médio até emissão (RF02/RF06) |
| `PATCH` | `/api/solicitacoes/{id}` | Altera o status, seguindo o fluxo de aprovação (RF03) |
| `GET/POST/PUT/DELETE` | `/api/alunos`, `/api/cursos`, `/api/tipoDocumento`, `/api/status` | CRUD das entidades de apoio (RF01.1) |

O fluxo de status segue:
```
ABERTA -> EM_ANALISE -> APROVADA -> EMITIDA
ABERTA -> EM_ANALISE -> REPROVADA
```
Cada etapa só pode ser avançada por um usuário cujo `codigoResponsavel`
bata com o `responsavel` cadastrado naquele `Status` (usuários com papel
`ADMIN` podem avançar qualquer etapa).

## Requisitos do edital atendidos

| RF | Descrição | Status |
|---|---|---|
| RF01 | Cadastro de solicitações | ✅ |
| RF01.1 | CRUD das demais entidades (Aluno, Curso, TipoDocumento, Status) | ✅ |
| RF02 | Consulta com filtros, paginação e estatísticas | ✅ |
| RF03 | Fluxo de aprovação por etapas | ✅ |
| RF04 | Segurança via JWT | ✅ |
| RF05 | Auditoria de todas as entidades | ✅ |
| RF06 | Dashboard | ✅ |
| RF07 | Telas complementares | ✅ |

## Testes

```bash
./mvnw test
```

## Estrutura do projeto

```
src/main/java/br/edu/unoesc/gestao_documentos/
├── audit/          # RF05 - auditoria genérica (entity listener + tabela)
├── config/         # seed do usuário administrador
├── controller/     # endpoints REST + páginas Thymeleaf
├── domain/         # entidades JPA
├── dto/            # objetos de entrada/saída da API
├── exception/      # exceções de negócio + tratamento global de erros
├── repositories/   # Spring Data JPA + Specifications
├── security/       # JWT (geração, validação, filtro, configuração)
└── service/        # regras de negócio
```