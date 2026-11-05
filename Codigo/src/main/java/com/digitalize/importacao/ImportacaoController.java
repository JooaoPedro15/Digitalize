package com.digitalize.importacao;

import com.digitalize.importacao.ImportacaoDtos.ImportacaoRequest;
import com.digitalize.importacao.ImportacaoDtos.ImportacaoResumo;
import com.digitalize.importacao.ImportacaoDtos.ResultadoImportacao;
import com.digitalize.seguranca.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{cnpj}/importacoes")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoImportacao importar(@PathVariable String cnpj, @Valid @RequestBody ImportacaoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return importacaoService.importar(cnpj, request, UsuarioLogado.de(jwt));
    }

    @GetMapping
    public List<ImportacaoResumo> listar(@PathVariable String cnpj, @AuthenticationPrincipal Jwt jwt) {
        return importacaoService.listar(cnpj, UsuarioLogado.de(jwt));
    }
}
