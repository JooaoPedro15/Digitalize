-- Schema inicial do Digitalize.
-- Todas as tabelas usam chave substituta (id) e guardam a data de criação.

CREATE TABLE usuario (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    senha_hash  VARCHAR(100) NOT NULL,
    papel       VARCHAR(10)  NOT NULL DEFAULT 'USUARIO',
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_email_minusculo CHECK (email = lower(email)),
    CONSTRAINT ck_usuario_papel CHECK (papel IN ('USUARIO', 'ADMIN'))
);

CREATE TABLE empresa (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id     BIGINT REFERENCES usuario (id) ON DELETE SET NULL,
    cnpj           VARCHAR(14)  NOT NULL,
    nome_fantasia  VARCHAR(120) NOT NULL,
    razao_social   VARCHAR(120) NOT NULL,
    segmento       VARCHAR(60),
    endereco       VARCHAR(200),
    email_contato  VARCHAR(255),
    status         VARCHAR(10)  NOT NULL DEFAULT 'PENDENTE',
    criado_em      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_empresa_cnpj UNIQUE (cnpj),
    CONSTRAINT ck_empresa_cnpj CHECK (cnpj ~ '^[0-9]{14}$'),
    CONSTRAINT ck_empresa_status CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA'))
);

CREATE INDEX ix_empresa_usuario ON empresa (usuario_id);

CREATE TABLE canal (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id     BIGINT      NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    plataforma     VARCHAR(20) NOT NULL,
    identificador  VARCHAR(80) NOT NULL,
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_canal UNIQUE (empresa_id, plataforma, identificador)
);

CREATE TABLE importacao (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    canal_id          BIGINT       NOT NULL REFERENCES canal (id) ON DELETE CASCADE,
    arquivo_original  VARCHAR(255) NOT NULL,
    periodo_inicio    DATE         NOT NULL,
    periodo_fim       DATE         NOT NULL,
    status            VARCHAR(12)  NOT NULL DEFAULT 'PENDENTE',
    criado_em         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_importacao UNIQUE (canal_id, arquivo_original, periodo_inicio),
    CONSTRAINT ck_importacao_periodo CHECK (periodo_fim >= periodo_inicio),
    CONSTRAINT ck_importacao_status CHECK (status IN ('PENDENTE', 'PROCESSADA', 'FALHA'))
);

CREATE TABLE post (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    canal_id           BIGINT    NOT NULL REFERENCES canal (id) ON DELETE CASCADE,
    importacao_id      BIGINT    REFERENCES importacao (id) ON DELETE SET NULL,
    publicado_em       TIMESTAMP NOT NULL,
    legenda            TEXT      NOT NULL DEFAULT '',
    duracao_segundos   INTEGER   NOT NULL DEFAULT 0,
    alcance            INTEGER   NOT NULL DEFAULT 0,
    visualizacoes      INTEGER   NOT NULL DEFAULT 0,
    curtidas           INTEGER   NOT NULL DEFAULT 0,
    compartilhamentos  INTEGER   NOT NULL DEFAULT 0,
    comentarios        INTEGER   NOT NULL DEFAULT 0,
    salvamentos        INTEGER   NOT NULL DEFAULT 0,

    CONSTRAINT ck_post_metricas CHECK (
        duracao_segundos >= 0 AND alcance >= 0 AND visualizacoes >= 0 AND curtidas >= 0
        AND compartilhamentos >= 0 AND comentarios >= 0 AND salvamentos >= 0
    )
);

-- Um mesmo post (canal + horário + legenda) não pode ser importado duas vezes.
-- A legenda entra como hash porque TEXT longo não cabe bem em índice.
CREATE UNIQUE INDEX uq_post_canal_publicacao ON post (canal_id, publicado_em, md5(legenda));
CREATE INDEX ix_post_importacao ON post (importacao_id);
