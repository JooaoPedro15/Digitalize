package com.digitalize.empresa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    @EntityGraph(attributePaths = "usuario")
    List<Empresa> findAllByOrderByNomeFantasiaAsc();

    @EntityGraph(attributePaths = "usuario")
    List<Empresa> findByUsuarioEmailOrderByNomeFantasiaAsc(String email);

    List<Empresa> findByStatusOrderByNomeFantasiaAsc(StatusEmpresa status);
}
