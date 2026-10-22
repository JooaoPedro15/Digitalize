package com.digitalize.importacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ImportacaoRepository extends JpaRepository<Importacao, Long> {

    Optional<Importacao> findByCanalIdAndArquivoOriginalAndPeriodoInicio(Long canalId, String arquivoOriginal, LocalDate periodoInicio);

    @Modifying
    @Query("delete from Importacao i where i.canal.id = :canalId")
    void deleteByCanalId(Long canalId);
}
