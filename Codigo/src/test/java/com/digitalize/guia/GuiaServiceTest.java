package com.digitalize.guia;

import com.digitalize.post.Post;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuiaServiceTest {

    private static Post post(int hora, String legenda, int views, int curtidas) {
        Post post = new Post(null, LocalDateTime.of(2026, 10, 1, hora, 0), legenda);
        post.atualizarMetricas(0, views, views, curtidas, 0, 0, 0);
        return post;
    }

    @Test
    void engajamentoPorMilIgnoraPostSemVisualizacao() {
        assertThat(post(10, "a", 0, 50).engajamentoPorMil()).isZero();
        assertThat(post(10, "a", 1000, 50).engajamentoPorMil()).isEqualTo(50);
    }

    @Test
    void analiseOrdenaHorariosPeloEngajamentoMedio() {
        List<Post> posts = List.of(
                post(9, "manhã fraca", 1000, 10),
                post(20, "noite forte", 1000, 200),
                post(20, "noite média", 1000, 100),
                post(13, "almoço", 1000, 60));

        GuiaService.Analise analise = GuiaService.Analise.de(posts);

        assertThat(analise.melhoresHorarios()).containsExactly(20, 13, 9);
        assertThat(analise.melhores().get(0).getLegenda()).isEqualTo("noite forte");
        assertThat(analise.piores().get(0).getLegenda()).isEqualTo("manhã fraca");
    }

    @Test
    void guiaLocalUsaHorariosEMelhorPost() {
        GuiaService.Analise analise = GuiaService.Analise.de(List.of(
                post(20, "Bolo de pote", 1000, 200),
                post(9, "Bom dia", 1000, 10)));

        GuiaPostagem guia = GuiaService.guiaLocal(analise);

        assertThat(guia.melhoresHorarios()).containsExactly("20:00", "09:00");
        assertThat(guia.sugestoesPosts().get(0).descricaoLegendada()).contains("Bolo de pote");
    }
}
