package com.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Classe responsável por criar e atualizar o schema e as tabelas do banco de dados.
 * Esta migration é executada automaticamente no início da aplicação.
 */
public class SchemaMigrator
{
    /**
     * Executa a criação do schema e das tabelas necessárias para o sistema.
     * Caso já existam, elas são apenas validadas ou atualizadas.
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
                // 1. Configuração do Schema
                st.execute("CREATE SCHEMA IF NOT EXISTS midiasocial;");
                st.execute("SET search_path TO midiasocial;");

                // 2. Tabela USUARIOS (Essencial para o Login)
                st.execute(
                    "CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "senha VARCHAR(255) NOT NULL, " +
                    "tipo VARCHAR(50) DEFAULT 'usuario', " +
                    "ativo BOOLEAN DEFAULT true, " +
                    "data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");"
                );

                // 3. Criar Admin Padrão (Para você não ficar trancado para fora)
                // A senha "123456" é inserida diretamente. Se usar criptografia no futuro, ajuste aqui.
                st.execute(
                    "INSERT INTO usuarios (nome, email, senha, tipo, ativo) " +
                    "VALUES ('Admin', 'admin@digitalize.com', '123456', 'admin', true) " +
                    "ON CONFLICT (email) DO NOTHING;" // Se já existir, não faz nada
                );

                // 4. Tabela EMPRESA
                // Atualizada com as colunas novas: responsavel_email e email_contato
                st.execute(
                    "CREATE TABLE IF NOT EXISTS empresa (" +
                    "cnpj CHAR(14) PRIMARY KEY CHECK (cnpj ~ '^[0-9]{14}$'), " +
                    "nome_fantasia VARCHAR(120) NOT NULL, " +
                    "razao_social VARCHAR(120) NOT NULL, " +
                    "segmento VARCHAR(40), " +
                    "endereco VARCHAR(200), " +
                    "status VARCHAR(12) DEFAULT 'ATIVA' CHECK (status IN ('ATIVA','INATIVA')), " +
                    "responsavel_email VARCHAR(255), " + // Nova coluna necessária
                    "email_contato VARCHAR(255)" +       // Nova coluna necessária
                    ");"
                );

                // ATUALIZAÇÃO SEGURA: Se a tabela empresa já existia sem as colunas novas,
                // esses comandos garantem que elas sejam criadas.
                try {
                    st.execute("ALTER TABLE empresa ADD COLUMN IF NOT EXISTS responsavel_email VARCHAR(255);");
                    st.execute("ALTER TABLE empresa ADD COLUMN IF NOT EXISTS email_contato VARCHAR(255);");
                } catch (SQLException e) {
                    // Ignora erro se coluna já existir em versões antigas do Postgres
                    System.out.println("Aviso: Colunas de empresa já existiam ou erro ao alterar: " + e.getMessage());
                }

                // 5. Tabela CANAL
                st.execute(
                    "CREATE TABLE IF NOT EXISTS canal (" +
                    "canal_id BIGSERIAL PRIMARY KEY, " +
                    "empresa_cnpj CHAR(14) NOT NULL REFERENCES empresa(cnpj) ON DELETE CASCADE, " +
                    "plataforma VARCHAR(20) NOT NULL, " +
                    "canal_identificador VARCHAR(80) NOT NULL, " +
                    "CONSTRAINT canal_ak UNIQUE (empresa_cnpj, plataforma, canal_identificador)" +
                    ");"
                );

                // 6. Tabela IMPORTACAO
                st.execute(
                    "CREATE TABLE IF NOT EXISTS importacao (" +
                    "canal_id BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE, " +
                    "importacao_arquivo_original VARCHAR(255) NOT NULL, " +
                    "importacao_periodo_inicio DATE NOT NULL, " +
                    "importacao_periodo_fim DATE NOT NULL, " +
                    "importacao_status VARCHAR(12) NOT NULL DEFAULT 'PENDENTE' " +
                    "CHECK (importacao_status IN ('PENDENTE','PROCESSADA','FALHA')), " +
                    "CONSTRAINT importacao_pk PRIMARY KEY (canal_id, importacao_arquivo_original, importacao_periodo_inicio), " +
                    "CONSTRAINT chk_periodo_ok CHECK (importacao_periodo_fim >= importacao_periodo_inicio)" +
                    ");"
                );

                // 7. Tabela POST
                st.execute(
                    "CREATE TABLE IF NOT EXISTS post (" +
                    "canal_id BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE, " +
                    "data_hora TIMESTAMP NOT NULL, " +
                    "legenda VARCHAR(300) NOT NULL, " +
                    "duracao INTEGER DEFAULT 0 CHECK (duracao >= 0), " +
                    "alcance INTEGER DEFAULT 0 CHECK (alcance >= 0), " +
                    "views INTEGER DEFAULT 0 CHECK (views >= 0), " +
                    "likes INTEGER DEFAULT 0 CHECK (likes >= 0), " +
                    "shares INTEGER DEFAULT 0 CHECK (shares >= 0), " +
                    "comentarios INTEGER DEFAULT 0 CHECK (comentarios >= 0), " +
                    "saves INTEGER DEFAULT 0 CHECK (saves >= 0), " +
                    "imp_arquivo_original VARCHAR(255), " +
                    "imp_periodo_inicio DATE, " +
                    "CONSTRAINT post_pk PRIMARY KEY (canal_id, data_hora, legenda), " +
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
            // Em caso de erro, imprime no log mas NÃO trava a aplicação (para garantir que o site suba)
            System.err.println("ERRO CRÍTICO NA MIGRATION: " + e.getMessage());
            e.printStackTrace();
            try {
                if (c != null) c.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            // Importante: Não lançamos throw new RuntimeException aqui para permitir 
            // que a aplicação continue rodando e carregue as rotas mesmo se o banco falhar.
        }
        finally
        {
            DAO.close(c);
        }
    }
}