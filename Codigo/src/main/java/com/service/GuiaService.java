package com.service;

import com.dao.DAO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gera o "Guia de Postagem" (JSON):
 * - Le posts dos ultimos 30 dias para uma empresa (join empresa->canal->post).
 * - Calcula ER, encontra winners/weak e melhores horarios.
 * - Tenta gerar JSON via Azure OpenAI; se falhar, devolve um fallback JSON local.
 */
public class GuiaService
{
    /**
     * Estrutura simples para carregar uma linha de post da consulta SQL.
     */
    private static class PRow
    {
        long canalId;
        LocalDateTime dataHora;
        String legenda;
        int views, likes, comentarios, shares;

        /** Retorna ER por mil views: (likes+comentarios+shares)/views * 1000 */
        int er()
        {
            if (views <= 0) return 0;
            return (likes + comentarios + shares) * 1000 / views;
        }

        /** Extrai a hora do dia (0..23) do timestamp do post */
        int hour()
        {
            return dataHora.getHour();
        }
    }

    /**
     * Ponto principal: monta o contexto e retorna o JSON do guia.
     * @param cnpj CNPJ da empresa a ser analisada.
     * @return JSON com resumo, insights, melhores horarios e sugestoes.
     */
    public String gerarGuiaParaEmpresa(String cnpj) throws Exception
    {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(30);

        // Consulta os posts do periodo
        List<PRow> posts = listarPostsUltimos30Dias(cnpj, inicio, hoje);

        // Caso nao haja dados, retorna JSON minimo
        if (posts.isEmpty())
        {
            return "{ \"resumo_periodo\": \"Sem dados suficientes no periodo.\", " +
                   "\"top_3_insights\": [], " +
                   "\"melhores_horarios\": [], " +
                   "\"diretrizes_tom_voz\": [], " +
                   "\"o_que_evitar\": [], " +
                   "\"sugestoes_posts\": [] }";
        }

        // Ordena por ER para pegar winners e weak
        List<PRow> winners = posts.stream()
                .sorted((a, b) -> Integer.compare(b.er(), a.er()))
                .limit(5)
                .collect(Collectors.toList());

        List<PRow> weak = posts.stream()
                .sorted(Comparator.comparingInt(PRow::er))
                .limit(5)
                .collect(Collectors.toList());

        // Agrupa por hora do dia e pega top-3 horas com maior ER medio
        Map<Integer, List<PRow>> byHour = posts.stream().collect(Collectors.groupingBy(PRow::hour));
        List<Integer> melhoresHoras = byHour.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),
                        (int) Math.round(e.getValue().stream().mapToInt(PRow::er).average().orElse(0))))
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Construi o "contexto" a ser enviado ao LLM
        StringBuilder contexto = new StringBuilder();
        contexto.append("Total de posts analisados nos ultimos 30 dias: ")
                .append(posts.size()).append("\n");

        contexto.append("\nPosts com melhor desempenho (ER):\n");
        for (PRow p : winners)
        {
            contexto.append("- [ER=").append(p.er()).append("] ")
                    .append(corta(p.legenda, 120)).append("\n");
        }

        contexto.append("\nPosts com pior desempenho (ER):\n");
        for (PRow p : weak)
        {
            contexto.append("- [ER=").append(p.er()).append("] ")
                    .append(corta(p.legenda, 120)).append("\n");
        }

        contexto.append("\nMelhores horarios (hora do dia): ")
                .append(melhoresHoras).append("\n");

        // Prompt de sistema que define o formato do JSON de saida
        String systemPrompt =
            "Voce e um assistente de marketing digital para microempresas. " +
            "Use EXCLUSIVAMENTE as informacoes fornecidas no contexto para construir um guia pratico. " +
            "Responda SOMENTE com JSON valido no formato: " +
            "{ \"resumo_periodo\": \"...\", " +
            "\"top_3_insights\": [\"...\",\"...\",\"...\"], " +
            "\"melhores_horarios\": [\"HH:mm\",\"HH:mm\",\"HH:mm\"], " +
            "\"diretrizes_tom_voz\": [\"...\"], " +
            "\"o_que_evitar\": [\"...\"], " +
            "\"sugestoes_posts\": [" +
            "{\"titulo\":\"...\",\"descricao_legendada\":\"...\",\"justificativa\":\"...\"}" +
            "] }.";

        //Tenta usar o LLM; se der erro (sem chave ou sem deployment), cai no fallback
        try
        {
            return AzureOpenAIClient.gerarGuiaJson(systemPrompt, contexto.toString());
        }
        catch (Exception e)
        {
            return fallbackJson(melhoresHoras, winners, weak);
        }
        
        
       

    }

    /**
     * Consulta SQL para listar posts (com métricas) dos ultimos 30 dias de uma empresa.
     * Faz join canal->post para filtrar por empresa (via empresa_cnpj).
     */
    private List<PRow> listarPostsUltimos30Dias(String cnpj, LocalDate inicio, LocalDate fim) throws SQLException
    {
        String sql =
            "SELECT p.canal_id, p.data_hora, p.legenda, " +
            "       COALESCE(p.views,0), COALESCE(p.likes,0), COALESCE(p.comentarios,0), COALESCE(p.shares,0) " +
            "FROM midiasocial.post p " +
            "JOIN midiasocial.canal c ON c.canal_id = p.canal_id " +
            "WHERE c.empresa_cnpj = ? " +
            "  AND p.data_hora >= ?::timestamp " +
            "  AND p.data_hora <  (?::timestamp + INTERVAL '1 day') " +
            "ORDER BY p.data_hora DESC";

        ArrayList<PRow> out = new ArrayList<>();
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, cnpj);
            ps.setDate(2, java.sql.Date.valueOf(inicio));
            ps.setDate(3, java.sql.Date.valueOf(fim));

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    PRow r = new PRow();
                    r.canalId = rs.getLong(1);
                    Timestamp ts = rs.getTimestamp(2);
                    r.dataHora = ts.toLocalDateTime();
                    r.legenda = rs.getString(3);
                    r.views = rs.getInt(4);
                    r.likes = rs.getInt(5);
                    r.comentarios = rs.getInt(6);
                    r.shares = rs.getInt(7);
                    out.add(r);
                }
            }
        }
        return out;
    }

    /**
     * Limita o tamanho de uma string para evitar JSON gigante nas amostras do contexto.
     */
    private static String corta(String s, int n)
    {
        if (s == null) return "";
        if (s.length() <= n) return s;
        return s.substring(0, n - 3) + "...";
    }

    /**
     * Fallback: gera um JSON simples localmente quando o LLM nao esta disponivel.
     * @param melhoresHoras top-3 horas por ER medio.
     * @param winners lista dos melhores posts por ER.
     * @param weak    lista dos piores posts por ER.
     * @return JSON (string) com campos minimos do guia.
     */
    private String fallbackJson(List<Integer> melhoresHoras, List<PRow> winners, List<PRow> weak)
    {
        String hh = melhoresHoras.stream()
                .map(h -> String.format("\"%02d:00\"", h))
                .collect(Collectors.joining(","));

        String w1 = winners.stream()
                .map(p -> "\"" + corta(p.legenda, 60).replace("\"","\\\"") + "\"")
                .collect(Collectors.joining(","));

        String w2 = weak.stream()
                .map(p -> "\"" + corta(p.legenda, 60).replace("\"","\\\"") + "\"")
                .collect(Collectors.joining(","));

        return "{"
            + "\"resumo_periodo\":\"Guia gerado sem LLM (fallback).\","
            + "\"top_3_insights\":[\"Replicar padroes dos winners\",\"Evitar temas dos fracos\",\"Postar nos melhores horarios\"],"
            + "\"melhores_horarios\":[" + hh + "],"
            + "\"diretrizes_tom_voz\":[\"Clara\",\"Objetiva\",\"CTA explicita\"],"
            + "\"o_que_evitar\":[\"Postar fora dos horarios bons\",\"Legendas longas demais\"],"
            + "\"sugestoes_posts\":["
            + "{\"titulo\":\"Ideia 1\",\"descricao_legendada\":\"Inspirada nos winners: "
            + (winners.isEmpty() ? "" : corta(winners.get(0).legenda, 60).replace("\"","\\\""))
            + "\",\"justificativa\":\"Base nos melhores ER\"}"
            + "]"
            + "}";
    }
}
