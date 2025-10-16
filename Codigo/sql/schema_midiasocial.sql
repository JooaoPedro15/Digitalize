-- schema
CREATE SCHEMA IF NOT EXISTS midiasocial;
SET search_path TO midiasocial;

-- TABELA: EMPRESA
CREATE TABLE IF NOT EXISTS empresa (
    cnpj             CHAR(14) PRIMARY KEY
                     CHECK (cnpj ~ '^[0-9]{14}$'),
    nome_fantasia    VARCHAR(120) NOT NULL,
    razao_social     VARCHAR(120) NOT NULL,   -- 120 da mais folga
    segmento         VARCHAR(40),
    endereco         VARCHAR(200),
    status           VARCHAR(12) NOT NULL DEFAULT 'ATIVA'
                     CHECK (status IN ('ATIVA','INATIVA'))
);

-- TABELA: CANAL  (canal + importacao + post em uma tabela; cada linha = 1 post)
CREATE TABLE IF NOT EXISTS canal (
    canal_id                     BIGSERIAL PRIMARY KEY,
    plataforma                   VARCHAR(20)  NOT NULL
                                  CHECK (plataforma IN ('YouTube','TikTok','Instagram')),
    canal_identificador          VARCHAR(80)  NOT NULL,

    importacao_arquivo_original  VARCHAR(255),
    importacao_periodo_inicio    DATE,
    importacao_periodo_fim       DATE,
    importacao_status            VARCHAR(12)  DEFAULT 'PENDENTE'
                                  CHECK (importacao_status IN ('PENDENTE','PROCESSADO','ERRO')),
    CONSTRAINT ck_import_periodo_ok
        CHECK (importacao_periodo_fim IS NULL
            OR importacao_periodo_inicio IS NULL
            OR importacao_periodo_fim >= importacao_periodo_inicio),


    data_hora                    TIMESTAMP NOT NULL DEFAULT NOW(),
    legenda                      VARCHAR(300),
    duracao                      INT          CHECK (duracao >= 0),
    alcance                      INT NOT NULL DEFAULT 0 CHECK (alcance      >= 0),
    views                        INT NOT NULL DEFAULT 0 CHECK (views        >= 0),
    likes                        INT NOT NULL DEFAULT 0 CHECK (likes        >= 0),
    shares                       INT NOT NULL DEFAULT 0 CHECK (shares       >= 0),
    comentarios                  INT NOT NULL DEFAULT 0 CHECK (comentarios  >= 0),
    saves                        INT NOT NULL DEFAULT 0 CHECK (saves        >= 0),

    empresa_cnpj                 CHAR(14) NOT NULL,
    CONSTRAINT fk_canal_empresa
        FOREIGN KEY (empresa_cnpj)
        REFERENCES empresa (cnpj)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- TABELA: RECOMENDACAO
CREATE TABLE IF NOT EXISTS recomendacao (
    recomendacao_id  BIGSERIAL PRIMARY KEY,
    tipo             VARCHAR(20) NOT NULL
                     CHECK (tipo IN ('POSTAGEM','ESTRATEGIA','OTIMIZACAO')),
    detalhes         TEXT,
    empresa_cnpj     CHAR(14) NOT NULL,
    CONSTRAINT fk_recomendacao_empresa
        FOREIGN KEY (empresa_cnpj)
        REFERENCES empresa (cnpj)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_empresa_status   ON empresa(status);
CREATE INDEX IF NOT EXISTS idx_canal_empresa    ON canal(empresa_cnpj);
CREATE INDEX IF NOT EXISTS idx_canal_chave      ON canal(plataforma, canal_identificador);
CREATE INDEX IF NOT EXISTS idx_canal_data       ON canal(plataforma, canal_identificador, data_hora DESC);
CREATE INDEX IF NOT EXISTS idx_recomend_empresa ON recomendacao(empresa_cnpj);


-- evita importar o mesmo post do mesmo canal com mesma data_hora+legenda
CREATE UNIQUE INDEX IF NOT EXISTS uq_canal_dedup
ON canal (plataforma, canal_identificador, data_hora, (COALESCE(legenda,'')));
