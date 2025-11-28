package com.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SchemaMigrator {
    public static void migrate() {
        Connection c = null;
        try {
            c = DAO.getConnection();
            c.setAutoCommit(false);

            try (Statement st = c.createStatement()) {
                st.execute("CREATE SCHEMA IF NOT EXISTS midiasocial;");
                st.execute("SET search_path TO midiasocial;");

                // Tabela Usuarios
                st.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "senha VARCHAR(255) NOT NULL, " +
                    "tipo VARCHAR(50) DEFAULT 'usuario', " +
                    "ativo BOOLEAN DEFAULT true, " +
                    "data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

                // Admin Padrão
                st.execute("INSERT INTO usuarios (nome, email, senha, tipo, ativo) " +
                    "VALUES ('Admin', 'admin@digitalize.com', '123456', 'admin', true) " +
                    "ON CONFLICT (email) DO NOTHING;");

                // Tabela Empresa
                st.execute("CREATE TABLE IF NOT EXISTS empresa (" +
                    "cnpj CHAR(14) PRIMARY KEY CHECK (cnpj ~ '^[0-9]{14}$'), " +
                    "nome_fantasia VARCHAR(120) NOT NULL, " +
                    "razao_social VARCHAR(120) NOT NULL, " +
                    "segmento VARCHAR(40), " +
                    "endereco VARCHAR(200), " +
                    "status VARCHAR(12) DEFAULT 'pendente', " +
                    "responsavel_email VARCHAR(255), " +
                    "email_contato VARCHAR(255)" +
                    ");");

                // Correções de segurança para tabela empresa existente
                try {
                    st.execute("ALTER TABLE empresa ADD COLUMN IF NOT EXISTS responsavel_email VARCHAR(255);");
                    st.execute("ALTER TABLE empresa ADD COLUMN IF NOT EXISTS email_contato VARCHAR(255);");
                    st.execute("ALTER TABLE empresa DROP CONSTRAINT IF EXISTS empresa_status_check;");
                    st.execute("ALTER TABLE empresa ADD CONSTRAINT empresa_status_check CHECK (status IN ('ATIVA','INATIVA','pendente','aprovada','rejeitada'));");
                } catch (Exception e) { /* Ignora se já existir */ }

                // Outras tabelas
                st.execute("CREATE TABLE IF NOT EXISTS canal (canal_id BIGSERIAL PRIMARY KEY, empresa_cnpj CHAR(14) NOT NULL REFERENCES empresa(cnpj) ON DELETE CASCADE, plataforma VARCHAR(20) NOT NULL, canal_identificador VARCHAR(80) NOT NULL, CONSTRAINT canal_ak UNIQUE (empresa_cnpj, plataforma, canal_identificador));");
                st.execute("CREATE TABLE IF NOT EXISTS importacao (canal_id BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE, importacao_arquivo_original VARCHAR(255) NOT NULL, importacao_periodo_inicio DATE NOT NULL, importacao_periodo_fim DATE NOT NULL, importacao_status VARCHAR(12) NOT NULL DEFAULT 'PENDENTE', CONSTRAINT importacao_pk PRIMARY KEY (canal_id, importacao_arquivo_original, importacao_periodo_inicio));");
                st.execute("CREATE TABLE IF NOT EXISTS post (canal_id BIGINT NOT NULL REFERENCES canal(canal_id) ON DELETE CASCADE, data_hora TIMESTAMP NOT NULL, legenda VARCHAR(300) NOT NULL, duracao INTEGER DEFAULT 0, alcance INTEGER DEFAULT 0, views INTEGER DEFAULT 0, likes INTEGER DEFAULT 0, shares INTEGER DEFAULT 0, comentarios INTEGER DEFAULT 0, saves INTEGER DEFAULT 0, imp_arquivo_original VARCHAR(255), imp_periodo_inicio DATE, CONSTRAINT post_pk PRIMARY KEY (canal_id, data_hora, legenda));");

                c.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try { if (c != null) c.rollback(); } catch (SQLException ex) { }
        } finally {
            DAO.close(c);
        }
    }
}