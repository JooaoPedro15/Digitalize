package com.digitalize.importacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ImportacaoDtos {

    private ImportacaoDtos() {
    }

    /** Um CSV inteiro de métricas, já lido e convertido pelo front-end. */
    public record ImportacaoRequest(
            @NotBlank(message = "Informe o nome do arquivo.")
            @Size(max = 255, message = "Nome de arquivo muito longo.")
            String arquivo,

            @NotEmpty(message = "O arquivo não tem nenhum post válido.")
            @Size(max = 5000, message = "Importe no máximo 5000 posts por vez.")
            List<@Valid PostImportado> posts) {
    }

    public record PostImportado(
            @JsonProperty("publicado_em")
            @NotNull(message = "Todo post precisa de data e hora.")
            LocalDateTime publicadoEm,

            String legenda,

            @JsonProperty("duracao_segundos") @PositiveOrZero Integer duracaoSegundos,
            @PositiveOrZero Integer alcance,
            @PositiveOrZero Integer visualizacoes,
            @PositiveOrZero Integer curtidas,
            @PositiveOrZero Integer comentarios,
            @PositiveOrZero Integer compartilhamentos,
            @PositiveOrZero Integer salvamentos) {
    }

    public record ResultadoImportacao(
            @JsonProperty("importacao_id") Long importacaoId,
            @JsonProperty("periodo_inicio") LocalDate periodoInicio,
            @JsonProperty("periodo_fim") LocalDate periodoFim,
            @JsonProperty("posts_novos") int postsNovos,
            @JsonProperty("posts_atualizados") int postsAtualizados) {
    }

    public record ImportacaoResumo(
            Long id,
            String arquivo,
            @JsonProperty("periodo_inicio") LocalDate periodoInicio,
            @JsonProperty("periodo_fim") LocalDate periodoFim,
            StatusImportacao status,
            @JsonProperty("total_posts") long totalPosts,
            @JsonProperty("criado_em") Instant criadoEm) {
    }
}
