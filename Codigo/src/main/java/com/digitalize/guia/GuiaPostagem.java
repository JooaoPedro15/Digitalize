package com.digitalize.guia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Guia de postagem devolvido ao front-end. É o mesmo formato pedido ao modelo
 * de linguagem, então a resposta dele pode ser lida direto neste record.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GuiaPostagem(
        @JsonProperty("resumo_periodo") String resumoPeriodo,
        @JsonProperty("top_3_insights") List<String> insights,
        @JsonProperty("melhores_horarios") List<String> melhoresHorarios,
        @JsonProperty("diretrizes_tom_voz") List<String> diretrizesTomVoz,
        @JsonProperty("o_que_evitar") List<String> oQueEvitar,
        @JsonProperty("sugestoes_posts") List<Sugestao> sugestoesPosts) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sugestao(
            String titulo,
            @JsonProperty("descricao_legendada") String descricaoLegendada,
            String justificativa) {
    }

    public static GuiaPostagem semDados() {
        return new GuiaPostagem("Sem dados suficientes no período.", List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
