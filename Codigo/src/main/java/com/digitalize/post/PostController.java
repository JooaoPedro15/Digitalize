package com.digitalize.post;

import com.digitalize.comum.ApiException;
import com.digitalize.post.PostDtos.PostResponse;
import com.digitalize.seguranca.UsuarioLogado;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class PostController {

    private static final int DIAS_PADRAO = 30;

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** Sem datas, devolve os últimos 30 dias. */
    @GetMapping("/api/empresas/{cnpj}/posts")
    public List<PostResponse> listar(
            @PathVariable String cnpj,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @AuthenticationPrincipal Jwt jwt) {
        LocalDate ate = fim != null ? fim : LocalDate.now();
        LocalDate desde = inicio != null ? inicio : ate.minusDays(DIAS_PADRAO);
        if (ate.isBefore(desde)) {
            throw ApiException.requisicaoInvalida("A data final não pode ser antes da inicial.");
        }
        return postService.listarDaEmpresa(cnpj, desde, ate, UsuarioLogado.de(jwt));
    }
}
