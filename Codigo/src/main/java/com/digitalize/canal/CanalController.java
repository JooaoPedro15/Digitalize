package com.digitalize.canal;

import com.digitalize.canal.CanalDtos.CanalCriadoResponse;
import com.digitalize.canal.CanalDtos.CanalRequest;
import com.digitalize.canal.CanalDtos.CanalResponse;
import com.digitalize.seguranca.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CanalController {

    private final CanalService canalService;

    public CanalController(CanalService canalService) {
        this.canalService = canalService;
    }

    @PostMapping("/canais")
    @ResponseStatus(HttpStatus.CREATED)
    public CanalCriadoResponse criar(@Valid @RequestBody CanalRequest request, @AuthenticationPrincipal Jwt jwt) {
        return new CanalCriadoResponse(canalService.criar(request, UsuarioLogado.de(jwt)).getId());
    }

    @GetMapping("/canais/{cnpj}")
    public List<CanalResponse> listar(@PathVariable String cnpj, @AuthenticationPrincipal Jwt jwt) {
        return canalService.listarPorEmpresa(cnpj, UsuarioLogado.de(jwt));
    }
}
