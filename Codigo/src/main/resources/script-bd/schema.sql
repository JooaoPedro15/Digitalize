CREATE SCHEMA IF NOT EXISTS midiasocial;
SET search_path TO midiasocial, public;


CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE IF EXISTS post
  ADD COLUMN IF NOT EXISTS embedding public.vector(1536);

CREATE INDEX IF NOT EXISTS idx_post_embedding_ivfflat

ON post USING ivfflat (embedding public.vector_cosine_ops)
  WITH (lists = 100);

-- EMPRESA
CREATE TABLE IF NOT EXISTS empresa
(
    cnpj           CHAR(14) PRIMARY KEY CHECK (cnpj ~ '^[0-9]{14}$'),
    nome_fantasia  VARCHAR(120) NOT NULL,
    razao_social   VARCHAR(120) NOT NULL,
    segmento       VARCHAR(40),
    endereco       VARCHAR(200),
    status         VARCHAR(12) NOT NULL DEFAULT 'ATIVA'
        CHECK (status IN ('ATIVA','INATIVA'))
);

-- CANAL (forte) + chave candidata
CREATE TABLE IF NOT EXISTS canal
(
    canal_id            BIGSERIAL PRIMARY KEY,
    empresa_cnpj        CHAR(14) NOT NULL REFERENCES empresa(cnpj) ON DELETE CASCADE,
    plataforma          VARCHAR(20) NOT NULL,
    canal_identificador VARCHAR(80) NOT NULL,
    CONSTRAINT canal_ak UNIQUE (empresa_cnpj, plataforma, canal_identificador)
);

-- IMPORTACAO (fraca) - PK composta
CREATE TABLE IF NOT EXISTS importacao
(
    canal_id                    BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE,
    importacao_arquivo_original VARCHAR(255) NOT NULL,
    importacao_periodo_inicio   DATE NOT NULL,
    importacao_periodo_fim      DATE NOT NULL,
    importacao_status           VARCHAR(12) NOT NULL DEFAULT 'PENDENTE'
        CHECK (importacao_status IN ('PENDENTE','PROCESSADA','FALHA')),
    CONSTRAINT chk_periodo_ok CHECK (importacao_periodo_fim >= importacao_periodo_inicio),
    CONSTRAINT importacao_pk PRIMARY KEY (canal_id, importacao_arquivo_original, importacao_periodo_inicio)
);

-- POST (fraca) - PK composta + FK opcional para importacao
CREATE TABLE IF NOT EXISTS post
(
    canal_id      BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE,
    data_hora     TIMESTAMP NOT NULL,
    legenda       VARCHAR(300) NOT NULL,

    duracao       INTEGER DEFAULT 0 CHECK (duracao >= 0),
    alcance       INTEGER DEFAULT 0 CHECK (alcance >= 0),
    views         INTEGER DEFAULT 0 CHECK (views >= 0),
    likes         INTEGER DEFAULT 0 CHECK (likes >= 0),
    shares        INTEGER DEFAULT 0 CHECK (shares >= 0),
    comentarios   INTEGER DEFAULT 0 CHECK (comentarios >= 0),
    saves         INTEGER DEFAULT 0 CHECK (saves >= 0),

    importacao_arquivo_original VARCHAR(255),
    importacao_periodo_inicio   DATE,

    CONSTRAINT post_pk PRIMARY KEY (canal_id, data_hora, legenda),

    CONSTRAINT fk_post_importacao
        FOREIGN KEY (canal_id, importacao_arquivo_original, importacao_periodo_inicio)
        REFERENCES importacao (canal_id, importacao_arquivo_original, importacao_periodo_inicio)
        DEFERRABLE INITIALLY DEFERRED
);

-- indices
CREATE INDEX IF NOT EXISTS idx_canal_empresa ON canal(empresa_cnpj);
CREATE INDEX IF NOT EXISTS idx_post_canal_data ON post(canal_id, data_hora);
