package com.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Cliente HTTP simples para chamar o Azure OpenAI por REST no projeto Digitalize.
 *
 * Funcionalidades principais:
 *  - gerarEmbedding: chama o deployment de embeddings e retorna o vetor (float[]).
 *  - gerarGuiaJson : chama o deployment de chat e retorna um JSON (String) com o guia.
 */
public class AzureOpenAIClient
{
    // -----------------------------
    // Variaveis de ambiente (Azure)
    // -----------------------------

    // Endpoint base do Azure OpenAI (ex: https://digitalize-openai.services.ai.azure.com/)
    private static final String RAW_ENDPOINT = System.getenv("AZURE_OPENAI_ENDPOINT");

    // Endpoint tratado para nao terminar com "/"
    private static final String ENDPOINT =
        (RAW_ENDPOINT != null && RAW_ENDPOINT.endsWith("/"))
            ? RAW_ENDPOINT.substring(0, RAW_ENDPOINT.length() - 1)
            : RAW_ENDPOINT;

    // Chave de acesso do recurso Azure OpenAI
    private static final String API_KEY  = System.getenv("AZURE_OPENAI_API_KEY");

    // Nome do deployment de embeddings (ex: text-embedding-3-small)
    private static final String EMB_DEP  = System.getenv("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");

    // Nome do deployment de chat (ex: gpt-5.1-chat)
    private static final String CHAT_DEP = System.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT");

    // Versao da API; se nao for definida, usa 2024-10-21 como padrao
    private static final String API_VER  =
        System.getenv().getOrDefault("AZURE_OPENAI_API_VERSION", "2024-10-21");

    // Cliente HTTP reutilizado pelas chamadas
    private static final HttpClient http = HttpClient.newHttpClient();

    // ObjectMapper do Jackson para montar/ler JSON
    private static final ObjectMapper om = new ObjectMapper();

    /**
     * Gera um embedding (vetor de floats) para um texto usando o deployment de embeddings.
     *
     * Fluxo:
     *  - Monta um JSON {"input": "texto"}.
     *  - Envia POST para /embeddings do deployment configurado.
     *  - Extrai o campo data[0].embedding da resposta.
     *
     * @param input Texto de entrada para vetorizacao.
     * @return vetor float[] com as dimensoes do modelo de embedding.
     * @throws Exception quando a chamada HTTP falhar ou as envs nao estiverem configuradas.
     */
    public static float[] gerarEmbedding(String input) throws Exception
    {
        // Valida se as variaveis estao definidas para embeddings
        if (ENDPOINT == null || API_KEY == null || EMB_DEP == null)
        {
            throw new IllegalStateException(
                "Variaveis AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY ou "
              + "AZURE_OPENAI_EMBEDDING_DEPLOYMENT nao configuradas para embeddings."
            );
        }

        // Monta o corpo JSON: { "input": "texto" }
        ObjectNode root = om.createObjectNode();
        root.put("input", input);

        // URL do endpoint de embeddings do deployment escolhido
        String url = ENDPOINT
                   + "/openai/deployments/"
                   + EMB_DEP
                   + "/embeddings?api-version="
                   + API_VER;

        // Requisicao HTTP POST
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("api-key", API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
            .build();

        // Executa e valida status HTTP
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200)
        {
            throw new RuntimeException("Erro ao gerar embedding (status "
                + resp.statusCode() + "): " + resp.body());
        }

        // Extrai o array "embedding" do JSON e converte para float[]
        JsonNode v = om.readTree(resp.body()).get("data").get(0).get("embedding");
        float[] out = new float[v.size()];

        for (int i = 0; i < out.length; i = i + 1)
        {
            out[i] = (float) v.get(i).asDouble();
        }

        return out;
    }

    /**
     * Chama o deployment de chat para produzir um JSON (response_format = json_object)
     * a partir de um system prompt (instrucoes) e um user prompt (contexto).
     *
     * Fluxo:
     *  - Monta as mensagens [system, user].
     *  - Forca response_format = { "type": "json_object" } para receber JSON estruturado.
     *  - Envia POST para /chat/completions do deployment configurado.
     *  - Retorna o conteudo do primeiro choice como String (que deve ser um JSON valido).
     *
     * @param systemPrompt Instrucoes fixas (papel do assistente e formato do JSON).
     * @param userPrompt   Contexto com dados do canal/periodo (winners, horarios, etc.).
     * @return String contendo apenas um JSON valido (conteudo do modelo).
     * @throws Exception quando a chamada HTTP falhar ou envs nao estiverem configuradas.
     */
    public static String gerarGuiaJson(String systemPrompt, String userPrompt) throws Exception
    {
        // Valida se as variaveis estao definidas para chat
        if (ENDPOINT == null || API_KEY == null || CHAT_DEP == null)
        {
            throw new IllegalStateException(
                "Variaveis AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY ou "
              + "AZURE_OPENAI_CHAT_DEPLOYMENT nao configuradas para chat."
            );
        }

        // Monta a lista de mensagens [system, user]
        ObjectNode root = om.createObjectNode();
        ArrayNode msgs = root.putArray("messages");

        ObjectNode sys = om.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        msgs.add(sys);

        ObjectNode usr = om.createObjectNode();
        usr.put("role", "user");
        usr.put("content", userPrompt);
        msgs.add(usr);

        // Forca retorno como JSON estruturado
        ObjectNode format = om.createObjectNode();
        format.put("type", "json_object");
        root.set("response_format", format);

        // IMPORTANTE: esse modelo nao aceita mudar temperature,
        // entao NAO enviamos o campo "temperature" aqui.

        String url = ENDPOINT
                   + "/openai/deployments/"
                   + CHAT_DEP
                   + "/chat/completions?api-version="
                   + API_VER;

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("api-key", API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200)
        {
            throw new RuntimeException("Erro ao gerar guia (status "
                + resp.statusCode() + "): " + resp.body());
        }

        // Pega o content do primeiro choice
        JsonNode j = om.readTree(resp.body());
        return j.get("choices").get(0).get("message").get("content").asText();
    }

}
