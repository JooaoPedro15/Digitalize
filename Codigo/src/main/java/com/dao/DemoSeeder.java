package com.dao;

import com.config.EnvConfig;
import com.service.PasswordHasher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cria uma conta de demonstracao com empresa, canal e posts de exemplo.
 *
 * So roda quando DIGITALIZE_DEMO=true. Os posts sao recriados a cada
 * inicializacao com datas relativas ao dia atual, para que o Guia de
 * Postagem (que analisa os ultimos 30 dias) sempre tenha dados.
 */
public class DemoSeeder {

    public static final String DEMO_EMAIL = "demo@digitalize.app";
    public static final String DEMO_NOME = "Visitante Demo";
    private static final String DEMO_SENHA = "demo-digitalize";
    private static final String DEMO_CNPJ = "12345678000100";
    private static final String ARQUIVO_DEMO = "demo-instagram.csv";

    private static final String[] LEGENDAS = {
        "Bastidores da producao do bolo de pote da semana",
        "Promocao relampago: 2 brigadeiros gourmet por R$10",
        "Conheca a historia da nossa confeitaria",
        "Receita rapida de brigadeiro de colher",
        "Cliente do mes: obrigado pela parceria!",
        "Novo sabor chegando: pistache com frutas vermelhas",
        "Como montamos uma caixa de presente",
        "Enquete: qual sabor voce quer na proxima fornada?",
        "Dicas para conservar seus doces por mais tempo",
        "Tour pela cozinha antes da abertura",
        "Encomendas abertas para o fim de semana",
        "Antes e depois da decoracao do bolo de aniversario"
    };

    public static boolean ativo() {
        return "true".equalsIgnoreCase(EnvConfig.getOrDefault("DIGITALIZE_DEMO", "false"));
    }

    public static void seed() {
        if (!ativo()) {
            return;
        }

        try {
            garantirUsuarioEEmpresa();
            long canalId = new CanalDAO().criarCanalGenericoSeNaoExistir(DEMO_CNPJ);
            recriarPosts(canalId);
            System.out.println("Conta demo pronta: " + DEMO_EMAIL);
        } catch (Exception e) {
            System.err.println("Aviso: nao foi possivel criar os dados de demonstracao: " + e.getMessage());
        }
    }

    public static String senha() {
        return DEMO_SENHA;
    }

    private static void garantirUsuarioEEmpresa() throws SQLException {
        try (Connection c = DAO.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO midiasocial.usuarios (nome, email, senha, tipo, ativo) " +
                    "VALUES (?, ?, ?, 'usuario', true) " +
                    "ON CONFLICT (email) DO UPDATE SET senha = EXCLUDED.senha, ativo = true")) {
                ps.setString(1, DEMO_NOME);
                ps.setString(2, DEMO_EMAIL);
                ps.setString(3, PasswordHasher.hash(DEMO_SENHA));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO midiasocial.empresa " +
                    "(cnpj, nome_fantasia, razao_social, segmento, endereco, status, responsavel_email, email_contato) " +
                    "VALUES (?, 'Doce Lar Confeitaria', 'Doce Lar Confeitaria LTDA (exemplo)', 'Alimentacao', " +
                    "'Belo Horizonte - MG', 'aprovada', ?, ?) " +
                    "ON CONFLICT (cnpj) DO UPDATE SET status = 'aprovada', responsavel_email = EXCLUDED.responsavel_email")) {
                ps.setString(1, DEMO_CNPJ);
                ps.setString(2, DEMO_EMAIL);
                ps.setString(3, DEMO_EMAIL);
                ps.executeUpdate();
            }
        }
    }

    private static void recriarPosts(long canalId) throws SQLException {
        if (canalId <= 0) {
            return;
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(28);

        try (Connection c = DAO.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM midiasocial.post WHERE canal_id = ?")) {
                    ps.setLong(1, canalId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM midiasocial.importacao WHERE canal_id = ?")) {
                    ps.setLong(1, canalId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO midiasocial.importacao " +
                        "(canal_id, importacao_arquivo_original, importacao_periodo_inicio, importacao_periodo_fim, importacao_status) " +
                        "VALUES (?, ?, ?, ?, 'PROCESSADA')")) {
                    ps.setLong(1, canalId);
                    ps.setString(2, ARQUIVO_DEMO);
                    ps.setDate(3, Date.valueOf(inicio));
                    ps.setDate(4, Date.valueOf(hoje));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO midiasocial.post " +
                        "(canal_id, data_hora, legenda, duracao, alcance, views, likes, shares, comentarios, saves, " +
                        "importacao_arquivo_original, importacao_periodo_inicio) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    int[] horas = {9, 12, 18, 20, 11, 19, 21, 8, 13, 18, 20, 17};
                    for (int i = 0; i < 24; i++) {
                        int hora = horas[i % horas.length];
                        LocalDateTime dataHora = inicio.plusDays(1 + i).atTime(hora, (i * 7) % 60);

                        // Posts a noite engajam mais, para o guia ter um padrao claro de horario
                        boolean noite = hora >= 18;
                        int views = 800 + (i * 137) % 1500 + (noite ? 900 : 0);
                        int likes = views / (noite ? 9 : 18) + (i * 11) % 40;
                        int comentarios = likes / 6 + i % 5;
                        int shares = likes / 10 + i % 3;

                        ps.setLong(1, canalId);
                        ps.setTimestamp(2, Timestamp.valueOf(dataHora));
                        ps.setString(3, LEGENDAS[i % LEGENDAS.length] + (i >= LEGENDAS.length ? " (parte 2)" : ""));
                        ps.setInt(4, 15 + (i * 9) % 45);
                        ps.setInt(5, (int) (views * 0.8));
                        ps.setInt(6, views);
                        ps.setInt(7, likes);
                        ps.setInt(8, shares);
                        ps.setInt(9, comentarios);
                        ps.setInt(10, likes / 5);
                        ps.setString(11, ARQUIVO_DEMO);
                        ps.setDate(12, Date.valueOf(inicio));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
