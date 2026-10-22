package com.digitalize.post;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Os nomes em JSON seguem o que a tela de importação já envia (canal_id, views, likes...).
 */
public final class PostDtos {

    private PostDtos() {
    }

    public record PostRequest(
            @JsonProperty("canal_id")
            @NotNull(message = "Dados do post incompletos.") @Positive(message = "Canal inválido.")
            Long canalId,

            @JsonProperty("data_hora")
            @NotNull(message = "Dados do post incompletos.")
            LocalDateTime dataHora,

            String legenda,

            @PositiveOrZero Integer duracao,
            @PositiveOrZero Integer alcance,
            @PositiveOrZero Integer views,
            @PositiveOrZero Integer likes,
            @PositiveOrZero Integer shares,
            @PositiveOrZero Integer comentarios,
            @PositiveOrZero Integer saves,

            @JsonProperty("imp_arquivo_original") @JsonAlias("importacao_arquivo_original")
            String arquivoImportacao,

            @JsonProperty("imp_periodo_inicio") @JsonAlias("importacao_periodo_inicio")
            LocalDate inicioImportacao) {
    }

    public record PostResponse(
            @JsonProperty("canal_id") Long canalId,
            @JsonProperty("data_hora") LocalDateTime dataHora,
            String legenda,
            int duracao,
            int alcance,
            int views,
            int likes,
            int shares,
            int comentarios,
            int saves) {

        public static PostResponse de(Post post) {
            return new PostResponse(
                    post.getCanal().getId(),
                    post.getPublicadoEm(),
                    post.getLegenda(),
                    post.getDuracaoSegundos(),
                    post.getAlcance(),
                    post.getVisualizacoes(),
                    post.getCurtidas(),
                    post.getCompartilhamentos(),
                    post.getComentarios(),
                    post.getSalvamentos());
        }
    }
}
