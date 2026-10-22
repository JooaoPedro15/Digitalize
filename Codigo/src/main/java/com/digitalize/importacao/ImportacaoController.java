package com.digitalize.importacao;

import com.digitalize.seguranca.UsuarioLogado;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    public record ImportacaoRequest(
            @JsonProperty("canal_id")
            @NotNull(message = "Dados da importação incompletos.") @Positive(message = "Canal inválido.")
            Long canalId,

            @JsonProperty("importacao_arquivo_original")
            @NotBlank(message = "Dados da importação incompletos.") @Size(max = 255, message = "Nome de arquivo muito longo.")
            String arquivoOriginal,

            @JsonProperty("importacao_periodo_inicio")
            @NotNull(message = "Dados da importação incompletos.")
            LocalDate periodoInicio,

            @JsonProperty("importacao_periodo_fim")
            @NotNull(message = "Dados da importação incompletos.")
            LocalDate periodoFim,

            @JsonProperty("importacao_status")
            StatusImportacao status) {
    }

    @PostMapping("/importacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Boolean> registrar(@Valid @RequestBody ImportacaoRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        importacaoService.registrar(
                UsuarioLogado.de(jwt),
                request.canalId(),
                request.arquivoOriginal().trim(),
                request.periodoInicio(),
                request.periodoFim(),
                request.status() != null ? request.status() : StatusImportacao.PROCESSADA);
        return Map.of("ok", true);
    }
}
