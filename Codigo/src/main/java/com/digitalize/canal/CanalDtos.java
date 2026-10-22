package com.digitalize.canal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public final class CanalDtos {

    private CanalDtos() {
    }

    public record CanalRequest(
            @JsonAlias("empresa_cnpj") String empresaCnpj,
            @Size(max = 20, message = "Plataforma muito longa.") String plataforma,
            @JsonAlias({"canal_identificador", "identificador"})
            @Size(max = 80, message = "Identificador muito longo.") String canalIdentificador) {
    }

    public record CanalResponse(Long canalId, String empresaCnpj, String plataforma, String canalIdentificador) {

        public static CanalResponse de(Canal canal) {
            return new CanalResponse(
                    canal.getId(),
                    canal.getEmpresa().getCnpj(),
                    canal.getPlataforma(),
                    canal.getIdentificador());
        }
    }

    public record CanalCriadoResponse(@JsonProperty("canal_id") Long canalId) {
    }
}
