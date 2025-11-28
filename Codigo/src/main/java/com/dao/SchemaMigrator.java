package com.dao;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Classe responsável por criar e atualizar o schema e as tabelas do banco de dados.
 * Esta migration é executada automaticamente no início da aplicação.
 */
public class SchemaMigrator
{
    /**
     * Executa a criação do schema e das tabelas necessárias para o sistema.
     * Caso já existam, elas são apenas validadas.
     */
    public static void migrate()
    {
        Connection c = null;

        try
        {
            // Obtém conexão com o banco e inicia transação manual
            c = DAO.getConnection();
            c.setAutoCommit(false);

            try (Statement st = c.createStatement())
            {
                // Criação do schema
                st.execute("CREATE SCHEMA IF NOT EXISTS midiasocial;");
                st.execute("SET search_path TO midiasocial;");

                // Tabela EMPRESA
                // Armazena empresas clientes que possuem canais/plataformas
                st.execute(
                    "CREATE TABLE IF NOT EXISTS empresa (" +
                    "cnpj CHAR(14) PRIMARY KEY CHECK (cnpj ~ '^[0-9]{14}$'), " + // Validação básica do CNPJ
                    "nome_fantasia VARCHAR(120) NOT NULL, " +
                    "razao_social VARCHAR(120) NOT NULL, " +
                    "segmento VARCHAR(40), " +
                    "endereco VARCHAR(200), " +
                    "responsavel_email VARCHAR(150), " +
                    "status VARCHAR(12) DEFAULT 'ATIVA' CHECK (status IN ('ATIVA','INATIVA'))" + // Controle de status
                    ");"
                );

                // Tabela CANAL
                // Representa um canal de mídia social vinculado a uma empresa (Instagram, Facebook etc.)
                st.execute(
                    "CREATE TABLE IF NOT EXISTS canal (" +
                    "canal_id BIGSERIAL PRIMARY KEY, " + // Identificador único
                    "empresa_cnpj CHAR(14) NOT NULL REFERENCES empresa(cnpj) ON DELETE CASCADE, " + // Relação com empresa
                    "plataforma VARCHAR(20) NOT NULL, " + // Ex: Instagram, TikTok
                    "canal_identificador VARCHAR(80) NOT NULL, " + // Ex: @perfil
                    "CONSTRAINT canal_ak UNIQUE (empresa_cnpj, plataforma, canal_identificador)" + // Evita duplicidade
                    ");"
                );

                // Tabela IMPORTACAO
                // Controle de arquivos importados (CSV, Excel, etc.) com métricas de postagens
                st.execute(
                    "CREATE TABLE IF NOT EXISTS importacao (" +
                    "canal_id BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE, " +
                    "importacao_arquivo_original VARCHAR(255) NOT NULL, " + // Nome do arquivo importado
                    "importacao_periodo_inicio DATE NOT NULL, " +
                    "importacao_periodo_fim DATE NOT NULL, " +
                    "importacao_status VARCHAR(12) NOT NULL DEFAULT 'PENDENTE' " +
                    "CHECK (importacao_status IN ('PENDENTE','PROCESSADA','FALHA')), " + // Status do processamento
                    "CONSTRAINT importacao_pk PRIMARY KEY (canal_id, importacao_arquivo_original, importacao_periodo_inicio), " + // Evita importações duplicadas
                    "CONSTRAINT chk_periodo_ok CHECK (importacao_periodo_fim >= importacao_periodo_inicio)" + // Garantia de período válido
                    ");"
                );

                // Tabela POST
                // Armazena cada postagem e suas métricas de engajamento
                st.execute(
                    "CREATE TABLE IF NOT EXISTS post (" +
                    "canal_id BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE, " +
                    "data_hora TIMESTAMP NOT NULL, " + // Data e hora da postagem
                    "legenda VARCHAR(300) NOT NULL, " + // Texto da postagem
                    "duracao INTEGER DEFAULT 0 CHECK (duracao >= 0), " + // Duração de vídeos
                    "alcance INTEGER DEFAULT 0 CHECK (alcance >= 0), " +
                    "views INTEGER DEFAULT 0 CHECK (views >= 0), " +
                    "likes INTEGER DEFAULT 0 CHECK (likes >= 0), " +
                    "shares INTEGER DEFAULT 0 CHECK (shares >= 0), " +
                    "comentarios INTEGER DEFAULT 0 CHECK (comentarios >= 0), " +
                    "saves INTEGER DEFAULT 0 CHECK (saves >= 0), " +
                    "imp_arquivo_original VARCHAR(255), " + // Vincula com importação
                    "imp_periodo_inicio DATE, " +
                    "CONSTRAINT post_pk PRIMARY KEY (canal_id, data_hora, legenda), " + // Identificação única
                    "CONSTRAINT post_fk_importacao FOREIGN KEY (canal_id, imp_arquivo_original, imp_periodo_inicio) " +
                    "REFERENCES importacao(canal_id, importacao_arquivo_original, importacao_periodo_inicio) ON DELETE SET NULL" +
                    ");"
                );

                // Finaliza transação
                c.commit();
            }
        }
        catch (Exception e)
        {
            // Caso ocorra erro, encapsula em RuntimeException
            throw new RuntimeException("Schema migration failed", e);
        }
        finally
        {
            // Fecha conexão
            DAO.close(c);
        }
    }
}
