package com.digitalize.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByCanalIdAndPublicadoEmAndLegenda(Long canalId, LocalDateTime publicadoEm, String legenda);

    List<Post> findByCanalIdAndPublicadoEmGreaterThanEqualAndPublicadoEmLessThanOrderByPublicadoEmAsc(
            Long canalId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            select p from Post p
            where p.canal.empresa.cnpj = :cnpj
              and p.publicadoEm >= :inicio
              and p.publicadoEm < :fim
            order by p.publicadoEm desc
            """)
    List<Post> listarDaEmpresaNoPeriodo(String cnpj, LocalDateTime inicio, LocalDateTime fim);

    @Modifying
    @Query("delete from Post p where p.canal.id = :canalId")
    void deleteByCanalId(Long canalId);
}
