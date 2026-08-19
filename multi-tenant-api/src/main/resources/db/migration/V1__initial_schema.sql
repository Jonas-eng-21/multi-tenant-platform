CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT uk_users_tenant_username
        UNIQUE (tenant_id, username)
);

CREATE TABLE pessoas (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    data_nascimento DATE NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_pessoas_cpf
        UNIQUE (cpf)
);

CREATE TABLE beneficiarios (
    id UUID PRIMARY KEY,
    pessoa_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    matricula VARCHAR(50) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_adesao DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_beneficiarios_pessoa
        FOREIGN KEY (pessoa_id)
        REFERENCES pessoas (id),

    CONSTRAINT fk_beneficiarios_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT uk_beneficiarios_tenant_matricula
        UNIQUE (tenant_id, matricula),

    CONSTRAINT ck_beneficiarios_tipo
        CHECK (tipo IN ('TITULAR', 'DEPENDENTE')),

    CONSTRAINT ck_beneficiarios_status
        CHECK (status IN ('ATIVO', 'INATIVO'))
);

CREATE INDEX idx_beneficiarios_tenant_id
    ON beneficiarios (tenant_id);

CREATE INDEX idx_beneficiarios_pessoa_id
    ON beneficiarios (pessoa_id);