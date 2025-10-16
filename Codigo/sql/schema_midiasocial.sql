-- Criação do esquema
CREATE SCHEMA IF NOT EXISTS midiasocial;
SET search_path TO midiasocial;

-- TABELA: EMPRESA
CREATE TABLE IF NOT EXISTS empresa (
    CNPJ CHAR(14) PRIMARY KEY,
    nome_fantasia     VARCHAR(120) NOT NULL,
    razao_social      VARCHAR(40)  NOT NULL,
    segmento          VARCHAR(40),
    endereco          VARCHAR(200),
    status            VARCHAR(12)  DEFAULT 'ATIVA'
);

-- TABELA: CANAL
CREATE TABLE IF NOT EXISTS canal (
    canal_ID                    BIGSERIAL PRIMARY KEY,
    plataforma                  VARCHAR(20),
    canal_identificador          VARCHAR(80),
    importacao_arquivo_original  VARCHAR(250),
    importacao_periodo_inicio    DATE,
    importacao_periodo_fim       DATE,
    importacao_status            VARCHAR(12),
    data_hora                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    legenda                      VARCHAR(300),
    duracao                      INT,
    alcance                      INT,
    views                        INT,
    likes                        INT,
    shares                       INT,
    comentarios                  INT,
    saves                        INT,
    EMPRESA_CNPJ                 CHAR(14) NOT NULL,
    CONSTRAINT fk_canal_empresa
        FOREIGN KEY (EMPRESA_CNPJ)
        REFERENCES empresa (CNPJ)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- TABELA: RECOMENDACAO
CREATE TABLE IF NOT EXISTS recomendacao (
    recomendacao_ID  BIGSERIAL PRIMARY KEY,
    tipo             VARCHAR(20),
    detalhes         TEXT,
    EMPRESA_CNPJ     CHAR(14) NOT NULL,
    CONSTRAINT fk_recomendacao_empresa
        FOREIGN KEY (EMPRESA_CNPJ)
        REFERENCES empresa (CNPJ)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- INDEXES (opcionais, para performance)
CREATE INDEX idx_empresa_status ON empresa(status);
CREATE INDEX idx_canal_empresa ON canal(EMPRESA_CNPJ);
CREATE INDEX idx_recomendacao_empresa ON recomendacao(EMPRESA_CNPJ);

-- FIM DO SCRIPT
