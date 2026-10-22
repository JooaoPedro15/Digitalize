package com.digitalize.guia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Chama o chat do Azure OpenAI pedindo uma resposta em JSON.
 * Se as variáveis AZURE_OPENAI_* não estiverem configuradas, fica desligado.
 */
@Component
public class AzureOpenAiClient {

    private final String endpoint;
    private final String apiKey;
    private final String chatDeployment;
    private final String apiVersion;
    private final RestClient restClient = RestClient.create();

    public AzureOpenAiClient(@Value("${digitalize.azure-openai.endpoint}") String endpoint,
                             @Value("${digitalize.azure-openai.api-key}") String apiKey,
                             @Value("${digitalize.azure-openai.chat-deployment}") String chatDeployment,
                             @Value("${digitalize.azure-openai.api-version}") String apiVersion) {
        this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        this.apiKey = apiKey;
        this.chatDeployment = chatDeployment;
        this.apiVersion = apiVersion;
    }

    public boolean configurado() {
        return !endpoint.isBlank() && !apiKey.isBlank() && !chatDeployment.isBlank();
    }

    /** Devolve o conteúdo JSON gerado pelo modelo, ou vazio se não estiver configurado. */
    public Optional<String> gerarJson(String promptSistema, String promptUsuario) {
        if (!configurado()) {
            return Optional.empty();
        }

        // Esse deployment não aceita mudar "temperature", por isso o campo não é enviado.
        Map<String, Object> corpo = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", promptSistema),
                        Map.of("role", "user", "content", promptUsuario)),
                "response_format", Map.of("type", "json_object"));

        JsonNode resposta = restClient.post()
                .uri(endpoint + "/openai/deployments/{deployment}/chat/completions?api-version={versao}",
                        chatDeployment, apiVersion)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(JsonNode.class);

        return Optional.ofNullable(resposta)
                .map(json -> json.path("choices").path(0).path("message").path("content").asString(null));
    }
}
