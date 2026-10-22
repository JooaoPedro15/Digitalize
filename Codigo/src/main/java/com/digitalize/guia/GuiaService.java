package com.digitalize.guia;

import com.digitalize.empresa.EmpresaService;
import com.digitalize.post.Post;
import com.digitalize.post.PostRepository;
import com.digitalize.seguranca.UsuarioLogado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gera o Guia de Postagem a partir dos posts dos últimos 30 dias:
 * calcula o engajamento, encontra os melhores e piores posts e os melhores horários.
 * Tenta escrever o guia com o Azure OpenAI; sem ele, monta um guia simples localmente.
 */
@Service
public class GuiaService {

    private static final Logger log = LoggerFactory.getLogger(GuiaService.class);
    private static final int DIAS_ANALISADOS = 30;

    private static final String PROMPT_SISTEMA = """
            Você é um assistente de marketing digital para microempresas.
            Use EXCLUSIVAMENTE as informações fornecidas no contexto para construir um guia prático.
            Responda SOMENTE com JSON válido no formato:
            { "resumo_periodo": "...",
              "top_3_insights": ["...","...","..."],
              "melhores_horarios": ["HH:mm","HH:mm","HH:mm"],
              "diretrizes_tom_voz": ["..."],
              "o_que_evitar": ["..."],
              "sugestoes_posts": [{"titulo":"...","descricao_legendada":"...","justificativa":"..."}] }
            """;

    private final EmpresaService empresaService;
    private final PostRepository posts;
    private final AzureOpenAiClient azureOpenAi;
    private final JsonMapper jsonMapper;

    public GuiaService(EmpresaService empresaService, PostRepository posts,
                       AzureOpenAiClient azureOpenAi, JsonMapper jsonMapper) {
        this.empresaService = empresaService;
        this.posts = posts;
        this.azureOpenAi = azureOpenAi;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public GuiaPostagem gerar(String cnpj, UsuarioLogado solicitante) {
        String cnpjEmpresa = empresaService.buscarComAcesso(cnpj, solicitante).getCnpj();
        LocalDate hoje = LocalDate.now();
        List<Post> doPeriodo = posts.listarDaEmpresaNoPeriodo(
                cnpjEmpresa, hoje.minusDays(DIAS_ANALISADOS).atStartOfDay(), hoje.plusDays(1).atStartOfDay());

        if (doPeriodo.isEmpty()) {
            return GuiaPostagem.semDados();
        }

        Analise analise = Analise.de(doPeriodo);
        return gerarComIa(analise).orElseGet(() -> guiaLocal(analise));
    }

    private Optional<GuiaPostagem> gerarComIa(Analise analise) {
        try {
            return azureOpenAi.gerarJson(PROMPT_SISTEMA, analise.contexto())
                    .map(json -> jsonMapper.readValue(json, GuiaPostagem.class));
        } catch (RuntimeException e) {
            log.warn("Falha ao gerar guia com Azure OpenAI, usando guia local: {}", e.getMessage());
            return Optional.empty();
        }
    }

    static GuiaPostagem guiaLocal(Analise analise) {
        String inspiracao = analise.melhores().isEmpty() ? "" : corta(analise.melhores().get(0).getLegenda(), 60);
        return new GuiaPostagem(
                "Guia gerado sem IA, a partir das métricas do período.",
                List.of("Replicar padrões dos posts com melhor engajamento",
                        "Evitar os temas dos posts mais fracos",
                        "Postar nos melhores horários"),
                analise.melhoresHorarios().stream().map(hora -> String.format("%02d:00", hora)).toList(),
                List.of("Clara", "Objetiva", "Com chamada para ação explícita"),
                List.of("Postar fora dos horários bons", "Legendas longas demais"),
                List.of(new GuiaPostagem.Sugestao(
                        "Ideia 1",
                        "Inspirada no melhor post: " + inspiracao,
                        "Baseada no maior engajamento do período")));
    }

    private static String corta(String texto, int limite) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= limite ? texto : texto.substring(0, limite - 3) + "...";
    }

    /** Números calculados sobre os posts do período. */
    record Analise(int total, List<Post> melhores, List<Post> piores, List<Integer> melhoresHorarios) {

        static Analise de(List<Post> posts) {
            Comparator<Post> porEngajamento = Comparator.comparingInt(Post::engajamentoPorMil);

            List<Post> melhores = posts.stream().sorted(porEngajamento.reversed()).limit(5).toList();
            List<Post> piores = posts.stream().sorted(porEngajamento).limit(5).toList();

            // Agrupa por hora do dia e pega as 3 horas com maior engajamento médio
            Map<Integer, Double> mediaPorHora = posts.stream().collect(Collectors.groupingBy(
                    post -> post.getPublicadoEm().getHour(),
                    Collectors.averagingInt(Post::engajamentoPorMil)));
            List<Integer> melhoresHorarios = mediaPorHora.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .toList();

            return new Analise(posts.size(), melhores, piores, melhoresHorarios);
        }

        String contexto() {
            StringBuilder sb = new StringBuilder()
                    .append("Total de posts analisados nos últimos 30 dias: ").append(total).append("\n")
                    .append("\nPosts com melhor desempenho (engajamento por mil views):\n");
            melhores.forEach(p -> sb.append("- [").append(p.engajamentoPorMil()).append("] ")
                    .append(corta(p.getLegenda(), 120)).append("\n"));
            sb.append("\nPosts com pior desempenho:\n");
            piores.forEach(p -> sb.append("- [").append(p.engajamentoPorMil()).append("] ")
                    .append(corta(p.getLegenda(), 120)).append("\n"));
            sb.append("\nMelhores horários (hora do dia): ").append(melhoresHorarios).append("\n");
            return sb.toString();
        }
    }
}
