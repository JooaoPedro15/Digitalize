package com.digitalize.importacao;

import com.digitalize.canal.Canal;
import com.digitalize.canal.CanalService;
import com.digitalize.comum.ApiException;
import com.digitalize.seguranca.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ImportacaoService {

    private final ImportacaoRepository importacoes;
    private final CanalService canalService;

    public ImportacaoService(ImportacaoRepository importacoes, CanalService canalService) {
        this.importacoes = importacoes;
        this.canalService = canalService;
    }

    @Transactional
    public Importacao registrar(UsuarioLogado solicitante, Long canalId, String arquivo,
                               LocalDate inicio, LocalDate fim, StatusImportacao status) {
        return registrar(canalService.buscarComAcesso(canalId, solicitante), arquivo, inicio, fim, status);
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
}
