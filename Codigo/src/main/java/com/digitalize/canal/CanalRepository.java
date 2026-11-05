package com.digitalize.canal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CanalRepository extends JpaRepository<Canal, Long> {

    Optional<Canal> findByEmpresaIdAndPlataformaAndIdentificador(Long empresaId, String plataforma, String identificador);
}
