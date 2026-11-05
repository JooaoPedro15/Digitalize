package com.digitalize.importacao;

import com.digitalize.importacao.ImportacaoDtos.ImportacaoResumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ImportacaoRepository extends JpaRepository<Importacao, Long> {

    Optional<Importacao> findByCanalIdAndArquivoOriginalAndPeriodoInicio(Long canalId, String arquivoOriginal, LocalDate periodoInicio);

    @Query("""
            select new com.digitalize.importacao.ImportacaoDtos$ImportacaoResumo(
                i.id, i.arquivoOriginal, i.periodoInicio, i.periodoFim, i.status, count(p), i.criadoEm)
            from Importacao i
            left join Post p on p.importacao = i
            where i.canal.empresa.id = :empresaId
            group by i.id, i.arquivoOriginal, i.periodoInicio, i.periodoFim, i.status, i.criadoEm
            order by i.criadoEm desc
            """)
    List<ImportacaoResumo> resumirDaEmpresa(Long empresaId);

    @Modifying
    @Query("delete from Importacao i where i.canal.id = :canalId")
    void deleteByCanalId(Long canalId);
}
