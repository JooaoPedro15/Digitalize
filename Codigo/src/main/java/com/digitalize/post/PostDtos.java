package com.digitalize.post;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public final class PostDtos {

    private PostDtos() {
    }

    public record PostResponse(
            Long id,
            @JsonProperty("publicado_em") LocalDateTime publicadoEm,
            String legenda,
            int alcance,
            int visualizacoes,
            int curtidas,
            int comentarios,
            int compartilhamentos,
            int salvamentos,
            @JsonProperty("engajamento_por_mil") int engajamentoPorMil) {

        public static PostResponse de(Post post) {
            return new PostResponse(
                    post.getId(),
                    post.getPublicadoEm(),
                    post.getLegenda(),
                    post.getAlcance(),
                    post.getVisualizacoes(),
                    post.getCurtidas(),
                    post.getComentarios(),
                    post.getCompartilhamentos(),
                    post.getSalvamentos(),
                    post.engajamentoPorMil());
        }
    }
}
