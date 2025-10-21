package model;

/**
 * Classe Canal
 * -------------------------------------------------------
 * Esta classe representa a tabela "canal" do banco de dados.
 * Cada instância corresponde a um post de um canal em uma plataforma
 * (YouTube, TikTok, Instagram, etc.).
 *
 * Estrutura SQL de referência:
 *   canal_id                     BIGSERIAL PRIMARY KEY
 *   plataforma                   VARCHAR(20)
 *   canal_identificador          VARCHAR(80)
 *   importacao_arquivo_original  VARCHAR(255)
 *   importacao_periodo_inicio    DATE
 *   importacao_periodo_fim       DATE
 *   importacao_status            VARCHAR(12)
 *   data_hora                    TIMESTAMP
 *   legenda                      VARCHAR(300)
 *   duracao                      INT
 *   alcance, views, likes, shares, comentarios, saves  INT
 *   empresa_cnpj                 CHAR(14) (FK para empresa)
 */
