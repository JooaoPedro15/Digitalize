package com.digitalize.canal;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CanalRepository extends JpaRepository<Canal, Long> {

    @EntityGraph(attributePaths = "empresa")
    List<Canal> findByEmpresaCnpjOrderByIdAsc(String cnpj);

    Optional<Canal> findByEmpresaIdAndPlataformaAndIdentificador(Long empresaId, String plataforma, String identificador);
}
