package com.digitalize.post;

import com.digitalize.empresa.Empresa;
import com.digitalize.empresa.EmpresaService;
import com.digitalize.post.PostDtos.PostResponse;
import com.digitalize.seguranca.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PostService {

    private final PostRepository posts;
    private final EmpresaService empresaService;

    public PostService(PostRepository posts, EmpresaService empresaService) {
        this.posts = posts;
        this.empresaService = empresaService;
    }

    /** Posts da empresa entre as datas (inclusive), do mais recente para o mais antigo. */
    @Transactional(readOnly = true)
    public List<PostResponse> listarDaEmpresa(String cnpj, LocalDate inicio, LocalDate fim, UsuarioLogado solicitante) {
        Empresa empresa = empresaService.buscarComAcesso(cnpj, solicitante);
        return posts.listarDaEmpresaNoPeriodo(empresa.getCnpj(), inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())
                .stream()
                .map(PostResponse::de)
                .toList();
    }
}
