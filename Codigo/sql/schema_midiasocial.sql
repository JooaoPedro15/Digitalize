-- CRIACAO DO ESQUEMA

CREATE SCHEMA IF NOT EXISTS midiasocial;
SET search_path TO midiasocial;


-- TABELA EMPRESA

CREATE TABLE empresa (
    empresa_id      BIGSERIAL PRIMARY KEY,
    nome_fantasia   VARCHAR(120)  NOT NULL,
    cnpj            CHAR(14)      NOT NULL
                     CHECK (cnpj ~ '^[0-9]{14}$'),
    razao_social    VARCHAR(120)  NOT NULL,
    segmento        VARCHAR(40),
    endereco        VARCHAR(200),
    status          VARCHAR(12)   NOT NULL DEFAULT 'ATIVO'
                     CHECK (status IN ('ATIVO','INATIVO'))
);


-- TABELA CANAL

CREATE TABLE canal (
    canal_id        BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT       NOT NULL REFERENCES empresa(empresa_id)
                                 ON DELETE CASCADE,
    plataforma      VARCHAR(20)  NOT NULL
                     CHECK (plataforma IN ('YouTube','TikTok','Instagram')),
    identificador   VARCHAR(80)  NOT NULL,
    CONSTRAINT uq_canal UNIQUE (empresa_id, plataforma, identificador)
);


-- TABELA IMPORTAÇÃO

CREATE TABLE importacao (
    importacao_id   BIGSERIAL PRIMARY KEY,
    canal_id        BIGINT       NOT NULL REFERENCES canal(canal_id)
                                 ON DELETE CASCADE,
    arquivo_original VARCHAR(255),
    periodo_inicio  DATE         NOT NULL,
    periodo_fim     DATE,
    status          VARCHAR(12)  NOT NULL DEFAULT 'PENDENTE'
                     CHECK (status IN ('PENDENTE','PROCESSADO','ERRO'))
);


-- TABELA POST

CREATE TABLE post (
    post_id        BIGSERIAL PRIMARY KEY,
    canal_id       BIGINT NOT NULL REFERENCES canal(canal_id)
                               ON DELETE CASCADE,
    importacao_id  BIGINT     REFERENCES importacao(importacao_id)
                               ON DELETE SET NULL,
    datahora       TIMESTAMP  NOT NULL DEFAULT NOW(),
    alcance        INT        CHECK (alcance  >= 0),
    views          INT        CHECK (views    >= 0),
    shares         INT        CHECK (shares   >= 0),
    likes          INT        CHECK (likes    >= 0),
    comentarios    INT        CHECK (comentarios >= 0),
    saves          INT        CHECK (saves    >= 0),
    duracao        INT        CHECK (duracao  >  0),
    legenda        VARCHAR(300)
);


-- TABELA RECOMENDACAO

CREATE TABLE recomendacao (
    recomendacao_id BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT  NOT NULL REFERENCES empresa(empresa_id)
                             ON DELETE CASCADE,
    canal_id        BIGINT  NOT NULL REFERENCES canal(canal_id)
                             ON DELETE CASCADE,
    tipo            VARCHAR(20) NOT NULL
                     CHECK (tipo IN ('POSTAGEM','ESTRATEGIA','OTIMIZACAO')),
    detalhes        TEXT
);


-- INDICES EXTRAS (opcionais para performance)

CREATE INDEX idx_post_canal   ON post(canal_id);
CREATE INDEX idx_post_import  ON post(importacao_id);
CREATE INDEX idx_import_canal ON importacao(canal_id);
CREATE INDEX idx_rec_empresa  ON recomendacao(empresa_id);

-- FIM DO SCRIPT
