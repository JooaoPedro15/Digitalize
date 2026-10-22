package com.digitalize.post;

import com.digitalize.post.PostDtos.PostRequest;
import com.digitalize.post.PostDtos.PostResponse;
import com.digitalize.seguranca.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Boolean> salvar(@Valid @RequestBody PostRequest request, @AuthenticationPrincipal Jwt jwt) {
        postService.salvar(request, UsuarioLogado.de(jwt));
        return Map.of("ok", true);
    }

    @GetMapping("/posts/{canalId}")
    public List<PostResponse> listar(
            @PathVariable Long canalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal Jwt jwt) {
        LocalDate hoje = LocalDate.now();
        return postService.listar(canalId, start != null ? start : hoje, end != null ? end : hoje, UsuarioLogado.de(jwt));
    }
}
