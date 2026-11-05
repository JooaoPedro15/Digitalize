package com.digitalize.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByCanalIdAndPublicadoEmBetween(Long canalId, LocalDateTime inicio, LocalDateTime fim);

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
