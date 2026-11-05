package com.digitalize.importacao;

import com.digitalize.canal.Canal;
import com.digitalize.canal.CanalService;
import com.digitalize.comum.ApiException;
import com.digitalize.empresa.Empresa;
import com.digitalize.empresa.EmpresaService;
import com.digitalize.importacao.ImportacaoDtos.ImportacaoRequest;
import com.digitalize.importacao.ImportacaoDtos.ImportacaoResumo;
import com.digitalize.importacao.ImportacaoDtos.PostImportado;
import com.digitalize.importacao.ImportacaoDtos.ResultadoImportacao;
import com.digitalize.post.Post;
import com.digitalize.post.PostRepository;
import com.digitalize.seguranca.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportacaoService {

    private final ImportacaoRepository importacoes;
    private final PostRepository posts;
    private final CanalService canalService;
    private final EmpresaService empresaService;

    public ImportacaoService(ImportacaoRepository importacoes, PostRepository posts,
                             CanalService canalService, EmpresaService empresaService) {
        this.importacoes = importacoes;
        this.posts = posts;
        this.canalService = canalService;
        this.empresaService = empresaService;
    }

    /**
     * Importa todos os posts de um CSV numa única transação: ou entra tudo, ou nada.
     * Posts que já existem (mesmo horário e legenda) só têm as métricas atualizadas,
     * então importar o mesmo arquivo de novo não duplica dados.
     */
    @Transactional
    public ResultadoImportacao importar(String cnpj, ImportacaoRequest request, UsuarioLogado solicitante) {
        Empresa empresa = empresaService.buscarComAcesso(cnpj, solicitante);
        Canal canal = canalService.garantirCanalPadrao(empresa);

        LocalDateTime primeiro = request.posts().stream().map(PostImportado::publicadoEm)
                .min(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime ultimo = request.posts().stream().map(PostImportado::publicadoEm)
                .max(Comparator.naturalOrder()).orElseThrow();

        Importacao importacao = registrar(canal, request.arquivo().trim(),
                primeiro.toLocalDate(), ultimo.toLocalDate(), StatusImportacao.PROCESSADA);

        // Carrega de uma vez os posts que já existem no período, para não consultar o banco post a post
        Map<String, Post> existentes = new HashMap<>();
        posts.findByCanalIdAndPublicadoEmBetween(canal.getId(), primeiro, ultimo)
                .forEach(post -> existentes.put(chave(post.getPublicadoEm(), post.getLegenda()), post));

        int novos = 0;
        List<Post> paraSalvar = new ArrayList<>();
        for (PostImportado dados : request.posts()) {
            String legenda = dados.legenda() == null ? "" : dados.legenda().trim();
            Post post = existentes.get(chave(dados.publicadoEm(), legenda));
            if (post == null) {
                post = new Post(canal, dados.publicadoEm(), legenda);
                existentes.put(chave(dados.publicadoEm(), legenda), post);
                novos++;
            }
            post.atualizarMetricas(
                    valor(dados.duracaoSegundos()), valor(dados.alcance()), valor(dados.visualizacoes()),
                    valor(dados.curtidas()), valor(dados.compartilhamentos()), valor(dados.comentarios()),
                    valor(dados.salvamentos()));
            post.setImportacao(importacao);
            paraSalvar.add(post);
        }
        posts.saveAll(paraSalvar);

        int totalUnicos = (int) paraSalvar.stream().distinct().count();
        return new ResultadoImportacao(importacao.getId(), importacao.getPeriodoInicio(),
                importacao.getPeriodoFim(), novos, totalUnicos - novos);
    }

    @Transactional(readOnly = true)
    public List<ImportacaoResumo> listar(String cnpj, UsuarioLogado solicitante) {
        Empresa empresa = empresaService.buscarComAcesso(cnpj, solicitante);
        return importacoes.resumirDaEmpresa(empresa.getId());
    }

    /**
     * Registra a importação. Se o mesmo arquivo já foi importado para o mesmo
     * canal e período, só atualiza o fim do período e o status.
     */
    @Transactional
    public Importacao registrar(Canal canal, String arquivo, LocalDate inicio, LocalDate fim, StatusImportacao status) {
        if (fim.isBefore(inicio)) {
            throw ApiException.requisicaoInvalida("O fim do período não pode ser antes do início.");
        }

        Importacao importacao = importacoes.findByCanalIdAndArquivoOriginalAndPeriodoInicio(canal.getId(), arquivo, inicio)
                .orElseGet(() -> new Importacao(canal, arquivo, inicio));
        importacao.setPeriodoFim(fim);
        importacao.setStatus(status);
        return importacoes.save(importacao);
    }

    private static String chave(LocalDateTime publicadoEm, String legenda) {
        return publicadoEm + "|" + legenda;
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }
}
