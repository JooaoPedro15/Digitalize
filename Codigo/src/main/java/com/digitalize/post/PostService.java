package com.digitalize.post;

import com.digitalize.canal.Canal;
import com.digitalize.canal.CanalService;
import com.digitalize.importacao.ImportacaoRepository;
import com.digitalize.post.PostDtos.PostRequest;
import com.digitalize.post.PostDtos.PostResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PostService {

    private final PostRepository posts;
    private final ImportacaoRepository importacoes;
    private final CanalService canalService;

    public PostService(PostRepository posts, ImportacaoRepository importacoes, CanalService canalService) {
        this.posts = posts;
        this.importacoes = importacoes;
        this.canalService = canalService;
    }

    /**
     * Salva o post ou, se ele já existir (mesmo canal, horário e legenda),
     * só atualiza as métricas. Assim reimportar o mesmo CSV não duplica dados.
     */
    @Transactional
    public void salvar(PostRequest request) {
        Canal canal = canalService.buscar(request.canalId());
        String legenda = request.legenda() == null ? "" : request.legenda();

        Post post = posts.findByCanalIdAndPublicadoEmAndLegenda(canal.getId(), request.dataHora(), legenda)
                .orElseGet(() -> new Post(canal, request.dataHora(), legenda));

        post.atualizarMetricas(
                valor(request.duracao()),
                valor(request.alcance()),
                valor(request.views()),
                valor(request.likes()),
                valor(request.shares()),
                valor(request.comentarios()),
                valor(request.saves()));

        if (request.arquivoImportacao() != null && request.inicioImportacao() != null) {
            importacoes.findByCanalIdAndArquivoOriginalAndPeriodoInicio(
                            canal.getId(), request.arquivoImportacao().trim(), request.inicioImportacao())
                    .ifPresent(post::setImportacao);
        }

        posts.save(post);
    }

    /** Lista os posts do canal entre as datas (inclusive). */
    @Transactional(readOnly = true)
    public List<PostResponse> listar(Long canalId, LocalDate inicio, LocalDate fim) {
        return posts.findByCanalIdAndPublicadoEmGreaterThanEqualAndPublicadoEmLessThanOrderByPublicadoEmAsc(
                        canalId, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())
                .stream()
                .map(PostResponse::de)
                .toList();
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }
}
