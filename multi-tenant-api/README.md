# Multi-Tenant Platform - Backend API Documentation

Esta é a documentação técnica oficial do backend da plataforma Multi-Tenant. O documento descreve não apenas a estrutura técnica do projeto, mas também as motivações arquiteturais, a estratégia de isolamento de dados e as decisões de engenharia adotadas na construção da API.

> **[INSERIR IMAGEM — Arquitetura geral do Backend]**

## 1. Idealização do Backend

O backend foi projetado para suportar um ecossistema Multi-Tenant, onde múltiplas empresas (Tenants) operam em uma plataforma centralizada sem enxergar os dados umas das outras. 

### Conceitos Centrais
- **Tenant:** Representa uma organização cliente.
- **Pessoa:** Entidade **global**. Pessoas não pertencem a um Tenant. Isso permite que uma mesma Pessoa exista uma única vez no banco de dados centralizado.
- **Beneficiário:** Representa o vínculo de uma Pessoa com um Tenant específico. Uma Pessoa pode possuir múltiplos vínculos (Beneficiários) em Tenants diferentes.

### Consequências das Decisões
- **Visibilidade:** O isolamento ocorre na entidade `Beneficiario`. Consultas de Beneficiários são filtradas rigidamente pelo contexto do Tenant logado.
- **Exclusão:** Não é possível excluir uma Pessoa caso ela possua vínculos ativos como Beneficiário em *qualquer* Tenant (garantia de integridade referencial mantida na base de dados e checada via service). 
- **Segurança:** O contexto do Tenant nunca é definido via payloads informados pela requisição frontend (o que seria uma vulnerabilidade de "Insecure Direct Object Reference" - IDOR), mas sim de forma criptograficamente garantida através do JWT (Claim `tenantId`).

## 2. Decisões Arquiteturais

A solução foi estruturada sob a ótica de uma **Arquitetura Monolítica Modular (Monolito Estruturado)** orientada a domínio, dividida por pacotes funcionais (Pessoa, Beneficiário, Tenant, Auth, Common, Config).

### Controller → Service → Repository
Essa é uma separação de camadas clássica do ecossistema Spring:
- **Controller:** Lida com HTTP, Validações de Borda (DTOs), e mapeamento REST. Nenhuma regra de negócio reside aqui.
- **Service:** Responsável exclusivo por orquestrar regras de negócio, transações (`@Transactional`) e delegar restrições globais ou de Tenant.
- **Repository:** Exclusivo para operações no banco, utilizando Spring Data JPA / Specifications.

### Design Patterns e Princípios (SOLID/KISS)
- **SRP (Single Responsibility):** Entidades como `TenantContext` focam exclusivamente em gerenciar o estado da thread atual.
- **KISS e YAGNI:** Optou-se por um Monolito bem testado e escalável em vez de microsserviços. No escopo apresentado, microsserviços gerariam overengineering (latência e complexidade de orquestração).
- **DTOs In-Out:** Isolamento explícito. O banco de dados (Entidade) não vaza no JSON, prevenindo exposições acidentais de segurança (ex: hashes de senha).
- **Tratamento Centralizado de Exceções:** Ao invés de `try-catch` espalhados, temos o `GlobalExceptionHandler`, garantindo conformidade com a RFC-7807 e saídas uniformes padronizadas (`ApiErrorResponse`).

## 3. Multi-Tenancy

> **[INSERIR IMAGEM — Fluxo Multi-Tenant]**

A estratégia escolhida foi **Shared Database, Shared Schema, Discriminator Column (`tenant_id`)**. 

### Isolamento Pragmático
Por que não um schema por Tenant? Um schema por Tenant implicaria em gerenciar centenas de schemas (complexidade de migrações e pool de conexões). A coluna separadora foi ideal pelo baixo atrito.

### Fluxo de uma requisição Multi-Tenant:
1. **Request:** O cliente envia o cabeçalho `Authorization: Bearer <token>`.
2. **JWT Extract:** O Spring Security via `TenantContextFilter` intercepta a chamada antes dos controllers.
3. **Tenant Context:** O filtro extrai a Claim `tenantId` contida no payload do JWT e popula o `TenantContext` (usando ThreadLocal).
4. **Service:** Os Services acessam `TenantContext.getTenantId()`.
5. **Repository:** O `tenantId` é inserido obrigatoriamente nas queries do Spring Data (`findByTenantIdAndMatricula` ou `BeneficiarioSpecification`). 

### Por que confiar no frontend é uma vulnerabilidade?
Se a API aceitasse `{"tenantId": "uuid"}` no payload POST/GET, um usuário mal-intencionado alteraria esse UUID interceptando o tráfego HTTP, escalando privilégios e exfiltrando dados de Tenants alheios. A injeção do Tenant ocorre única e exclusivamente via Server-Side extraindo o Token.

### Deleção Cross-Tenant
Como a deleção da Pessoa global funciona? Se o Admin tenta excluir um ID, o `PessoaService` valida `beneficiarioRepository.existsByPessoaId(id)`. Como essa validação é isenta de Tenant, ela garante que *nenhum* Tenant seja quebrado se a Pessoa for removida.

## 4. Segurança

- **Autenticação:** Baseada em OAuth2 Resource Server. Os JWTs são emitidos no login com `username` e `tenantId` embarcados (Claims). 
- **Autorização:** Rotas protegidas (todas, exceto de login) rejeitam instantaneamente não autenticados (401 Unauthorized) via `CustomAuthenticationEntryPoint`. Permissões negadas são gerenciadas via `CustomAccessDeniedHandler` (403 Forbidden).
- **Isolamento de Segurança:** Cada credencial é ancorada ao banco por `tenant_id` e `username` (`uk_users_tenant_username`).
- **CORS:** O backend é permissivo com o frontend através de Global CORS config (habilitado para permitir headers).

## 5. Modelo de Dados

O banco de dados relacional (PostgreSQL) adota IDs em formato UUIDv4 (distribuído) ao invés de auto incrementais, para ofuscar tamanho das tabelas e dificultar enumeração (Ataque de Força Bruta em URLs).

- **Tenants:** (id, nome, created_at).
- **Users:** (id, tenant_id, username, password). UNIQUE (tenant_id, username).
- **Pessoas:** (id, nome, cpf, email...). UNIQUE (cpf).
- **Beneficiários:** (id, pessoa_id, tenant_id, matricula, tipo, status). FKs garantem consistência estrutural.
- **Constraints/Índices:** Existem Unique Constraints na matrícula combinada com tenant_id, e checagens fixas de domínio nos enumeradores (TITULAR, DEPENDENTE). A busca por tenant tem índices.

> **[INSERIR IMAGEM — Estrutura do banco/migrations]**

## 6. Banco e Migrations (Flyway)

A gestão evolutiva do banco foi atribuída ao **Flyway**.
- Ao subir a aplicação, a migration `V1__initial_schema.sql` executa DDL idempotente.
- A ferramenta anula a necessidade de aplicar scripts soltos (DBA manual). 
- Toda mudança estrutural ou populacional tem versionamento semântico no próprio repositório, garantindo builds reproduzíveis em qualquer máquina (CI/CD).

A aplicação conta também com Initializers (Ex: `DevDataInitializer`) para gerar a seed data básica (usuários padrão de dev) de forma automática. 

## 7. API REST

A API expõe o ecossistema sob diretrizes RESTful, tipicamente nos endpoints:

- **Autenticação (`/api/auth`)**
  - `POST /login`: Recebe credenciais, emite JWT.
- **Pessoas (`/api/pessoas`)**
  - Entidade genérica administrativa.
  - Paginação via `GET` query param (ex: `page=0&size=10`). 
  - Filtros globais (CPF, Nome).
- **Beneficiários (`/api/beneficiarios`)**
  - Isolado. Paginação, Busca por `matricula`, `status` ou `tipo`.
  - Códigos HTTP: 201 (Created), 200 (OK), 204 (No Content), 404 (Not Found) ou 409 (Conflict).

> **[INSERIR IMAGEM — Swagger/OpenAPI]**

## 8. Tratamento de Erros

O tratamento de exceções é centralizado.
O `GlobalExceptionHandler` captura Exceptions customizadas (`ConflictException`, `ResourceNotFoundException`) e exceções nativas (validações `@Valid`, DataIntegrityViolation), envelopando em um body `ApiErrorResponse` contendo o timestamp e a mensagem humanizada.
Isso melhora drasticamente o debugging e o acoplamento do Frontend.

## 9. Paginação e Performance

Retornar `List<T>` ilimitada degrada CPU, Memória e Bandwidth, além de sobrecarregar o DB com I/O massivo. 
A API utiliza paginação dinâmica com Spring Data (`Pageable`). O objeto customizado `PageResponse` reduz acoplamentos desnecessários (escondendo abstrações nativas do Spring da interface HTTP). 

## 10. Testes


A suíte de testes reflete a criticidade da arquitetura:
- **Unitários:** Cobertura de lógica negocial nos `Services` (ex: falhar criacão de Beneficiário com matrícula duplicada no Tenant). 
- **Integração:** `MultiTenantIsolationIntegrationTest`. Provam que a tentativa de buscar ou excluir um registro de outro Tenant resulta em `ResourceNotFoundException`. Comprovam exclusões globais bloqueadas quando há uso cross-tenant.

## 11. Docker e Infraestrutura

A containerização via **Dockerfile multi-stage build** é a chave da portabilidade.
- **Stage 1 (Build):** Imagem Maven com Eclipse Temurin (Java 21). Baixa dependências (`go-offline`), compila e roda os testes.
- **Stage 2 (Runtime):** Imagem base Eclipse Temurin 21-JRE nua (mínima), carrega o Jar executável e abre a porta 8080.
Isso remove a necessidade de o ambiente hospedeiro (avaliador/operador) ter Java ou Maven instalados em máquina física. Tudo corre perfeitamente encapsulado na sub-rede do `docker-compose`.

## 12. Configuração e Ambientes

Configurado primordialmente via `application.properties` (e profile dev). A injeção de parâmetros (Credenciais de DB, chaves de JWT) vem por Variáveis de Ambiente, providas pelo Docker.

## 13. Observabilidade e OpenAPI

O Swagger foi implementado através do `springdoc-openapi`. A API tem uma especificação Swagger viva que simplifica consumo e fornece sandbox para testes sem necessidade de importações manuais no Postman. A documentação interativa é acessível via root context de `swagger-ui`.

## 14. Estrutura do Projeto

```text
src/main/java/br/com/jonas/multitenant
├── beneficiario     # Domínio Multi-Tenant
├── pessoa           # Domínio Global
├── common           # Validadores, DTOs e Classes Genéricas
├── config           # Configs Spring, OpenAPI, DevSeeds
├── exception        # Handlers padronizados RFC-7807
├── security         # Filters, Entrypoints, TenantContext
├── tenant           # Definição dos Tenants
└── user             # Gestão de credenciais/Authentication
```

## 15. Processo de Desenvolvimento

A arquitetura foi concebida nas seguintes etapas:
1. **Definição de Domínio:** Mapeamento de Pessoa (Global) x Beneficiário (Tenant) e regras de deleção.
2. **Definição da Arquitetura:** Escopo Monolítico MVC API-driven (separação Service vs Controller).
3. **Persistência & Isolamento:** Uso de Flyway para a infra do BD, Modelagem via JPA, e Filters Multi-Tenant robustecendo requisições.
4. **Segurança & JWT:** Criação de rotinas Stateless e OAuth Resource Server, extração criptográfica de `tenantId`.
5. **Quality Assurance:** Casos de Integração End-to-End simulando vazamento de Tenants e tratamento isolado.
6. **Containerização:** Dockerização multi-stage, otimizando CI/CD pipeline virtual.

## 16. Conclusão e Resultado

O desafio backend foi construído não apenas para responder `HTTP 200`, mas com os fundamentos consolidados de **Desenvolvimento de Software Nível Enterprise**. 
A plataforma é **Segura** (vulnerabilidades IDOR eliminadas pelo Server-Side Context Filter), **Isolada** (nenhum dado vaza entre clientes em consultas `findAll`), **Evolutiva** (Flyway), **Desacoplada** e **Reprodutível** em qualquer máquina através de Docker Multi-stage. O projeto se provou um test-case maduro de Multi-Tenancy em aplicações de alta disponibilidade.
