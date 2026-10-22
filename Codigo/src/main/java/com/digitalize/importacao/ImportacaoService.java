package com.digitalize.importacao;

import com.digitalize.canal.Canal;
import com.digitalize.canal.CanalService;
import com.digitalize.comum.ApiException;
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

    /**
     * Registra a importação. Se o mesmo arquivo já foi importado para o mesmo
     * canal e período, só atualiza o fim do período e o status.
     */
    @Transactional
    public Importacao registrar(Long canalId, String arquivo, LocalDate inicio, LocalDate fim, StatusImportacao status) {
        if (fim.isBefore(inicio)) {
            throw ApiException.requisicaoInvalida("O fim do período não pode ser antes do início.");
        }

        Canal canal = canalService.buscar(canalId);
        Importacao importacao = importacoes.findByCanalIdAndArquivoOriginalAndPeriodoInicio(canalId, arquivo, inicio)
                .orElseGet(() -> new Importacao(canal, arquivo, inicio));
        importacao.setPeriodoFim(fim);
        importacao.setStatus(status);
        return importacoes.save(importacao);
    }
}
