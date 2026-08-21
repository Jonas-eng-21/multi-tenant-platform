# Multi-Tenant Platform

Bem-vindo ao repositório do projeto **Multi-Tenant Platform**, uma solução Full Stack construída para um desafio técnico focado em isolamento e segurança em ambientes com múltiplos clientes (Tenants).

> **Aviso:** Esta documentação serve como porta de entrada geral. Para detalhes técnicos aprofundados sobre a arquitetura, acesse as documentações específicas de cada camada nos arquivos correspondentes.
> - [Documentação da API (Backend)](multi-tenant-api/README.md)
> - [Documentação do Frontend](frontend/README.md)

## 1. O Desafio e Domínio de Negócio

O desafio consistiu em desenvolver um sistema de gestão cadastral capaz de operar em um ecossistema Multi-Tenant (múltiplos clientes compartilhando o mesmo sistema). 

### Conceitos Principais
- **Pessoa:** Uma entidade **global**. O indivíduo existe no sistema independentemente de qualquer cliente (nome, CPF, data de nascimento).
- **Tenant:** Uma empresa ou cliente que contrata a plataforma.
- **Beneficiário:** A representação do vínculo de uma Pessoa com um Tenant específico. Uma Pessoa pode ser Beneficiária em vários Tenants, mas as informações do Beneficiário em um Tenant (matrícula, status, plano) são absolutamente invisíveis para os demais.

A plataforma permite realizar operações CRUD completas desses domínios, garantindo que o Tenant A jamais possua capacidade de ler, editar ou apagar um Beneficiário do Tenant B.

## 2. Visão Geral da Solução

O sistema é uma aplicação Full Stack com 3 pilares fundamentais:

```text
       Usuário
          ↓
[ Vue.js 3 + Nginx ] (Frontend)
          ↓
[ Spring Boot 3 API ] (Backend)
          ↓
 [ PostgreSQL 17 ] (Persistência)
```

- **Frontend:** Camada visual baseada em componentes reativos, consumindo a API.
- **Backend:** O núcleo das regras de negócio, encarregado de interceptar a sessão e filtrar, na raiz do banco, os dados do Tenant logado.
- **PostgreSQL:** Banco de dados relacional que garante o isolamento físico via Constraints estruturais (Foreign Keys).

## 3. Principais Decisões Técnicas

- **Vue 3 + TypeScript:** Maior reatividade e segurança de tipagem estática no frontend.
- **Spring Boot + Java 21:** Maior ecossistema empresarial para construção de serviços REST com alta produtividade.
- **PostgreSQL:** Persistência relacional madura, capaz de aplicar travas de integridade em chaves estrangeiras (essencial para isolar Tenants).
- **JWT (JSON Web Token):** Autenticação Stateless. O identificador do Tenant viaja empacotado e assinado dentro do Token, impedindo que requisições Frontend adulterem dados de autorização.
- **Multi-tenancy com `tenant_id`:** Abordagem de _Shared Database, Shared Schema_. Escolhida por possuir menor atrito inicial e evitar sobrecarga de migrações e pools de conexão comparada ao _Schema por Tenant_.
- **Docker e Nginx:** O Nginx serve o build otimizado do Frontend de forma estática, e o Docker encapsula todo o ambiente, removendo a famosa síndrome "na minha máquina funciona".
- **Flyway:** versionamento automatizado e seguro de migrações SQL.

## 4. Estrutura do Repositório

```text
multi-tenant-platform/
├── frontend/             # Aplicação Vue.js, componentes e Dockerfile do Nginx
├── multi-tenant-api/     # Aplicação Java, regras de negócio e configuração do Flyway
├── docker-compose.yml    # Orquestração para subir as três camadas (DB, API, Front) simultaneamente
├── .env                  # Variáveis de ambiente com chaves JWT e senhas locais já configuradas
└── README.md             # Esta documentação
```

## 5. Como Executar (Ambiente Docker)

A execução do projeto foi projetada para ser simples, automatizada e não exigir conhecimentos prévios do avaliador sobre configuração de infraestrutura.

O fluxo de execução **já carrega banco, dependências, migrações SQL e seed de dados** de maneira nativa.

### 5.1. Pré-Requisitos

**O que você PRECISA ter instalado:**
- Docker Engine ou Docker Desktop
- Docker Compose

**O que você NÃO precisa instalar:**
- Node.js / NPM
- Java / Maven
- PostgreSQL

Tudo ocorre magicamente dentro das redes e containers configurados. O projeto já acompanha um arquivo `.env` configurado com valores locais adequados.

### 5.2. Rodando a Aplicação

1. Clone o repositório.
2. Pelo seu terminal, entre na raiz do repositório clonado:
   ```bash
   cd multi-tenant-platform
   ```
3. Execute o Docker Compose em modo *build*:
   ```bash
   docker compose up --build
   ```

Aguarde até que os logs estabilizem. A API Java compila o projeto em tempo de execução via multi-stage, o que pode levar cerca de 1 a 2 minutos na primeira vez.

## 6. Acessos e URLs

Após o carregamento dos containers, os serviços estarão acessíveis nas portas padrão da máquina local:

- **Frontend da Aplicação:** [http://localhost](http://localhost)
- **Documentação da API (Swagger/OpenAPI):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 7. Credenciais de Demonstração (Seed)

O processo de build injeta no banco 3 usuários base, um para cada ambiente Tenant. Isso facilita a validação imediata do login e do isolamento, sem necessidade de inserts manuais.

**Tenant A**
- **Usuário:** `admin-a`
- **Senha:** `password`

**Tenant B**
- **Usuário:** `admin-b`
- **Senha:** `password`

**Tenant C**
- **Usuário:** `admin-c`
- **Senha:** `password`

*As senhas reais do Seed são orientadas por variável de ambiente (`SEED_PASSWORD`) no arquivo `.env` da raiz.*

## 8. Funcionalidades Implementadas

O projeto obedece ao escopo proposto no desafio:
- **Autenticação Segura:** Login central e geração de sessão com JWT.
- **Pessoas:** Operações de CRUD global para a entidade Pessoa. 
- **Beneficiários:** Operações de CRUD isoladas. Ao logar com `admin-a`, toda a gestão reflete apenas o Tenant A. 
- **Regras de Negócio e Tratamento de Erros:** Não se pode deletar uma Pessoa que possua um Beneficiário existente (em *qualquer* Tenant). 
- **Paginação:** As listas consultam os registros no backend sob demanda via `page` e `size`.
- **Interface Responsiva e Dashboard:** Visão agregada de Pessoas ativas/inativas no Frontend com UI unificada.


## 9. Validação do Projeto

Os seguintes fluxos estão 100% validados para demonstração da solução:
- Execução isolada *zero-dependencies* através de Docker Compose.
- Interceptação CORS entre os domínios via Spring Config.
- Frontend servido dinamicamente via reverse proxy do Nginx.
- Isolamento pragmático (um usuário do Tenant A recebe HTTP 404 se tentar forçar edição em registro do Tenant B).
- Versionamento do DB (Flyway).

## 10. Conclusão

Este desafio técnico foi resolvido visando ir além da simples entrega de "CRUDs funcionando". O repositório reflete as preocupações do mundo real com o desenvolvimento Full Stack robusto: tratamento global de exceções, portabilidade extrema (Docker Multistage), isolamento eficaz, padronização da UI (Vue) e regras de integridade consistentes em um ambiente distribuído Multi-Tenant.
