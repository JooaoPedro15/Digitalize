package com.digitalize.canal;

import com.digitalize.empresa.Empresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Por enquanto cada empresa tem um canal padrão, criado na primeira importação.
 * O modelo já permite vários canais (um por rede social) no futuro.
 */
@Service
public class CanalService {

    static final String PLATAFORMA_PADRAO = "GENERICA";
    static final String IDENTIFICADOR_PADRAO = "canal-principal";

    private final CanalRepository canais;

    public CanalService(CanalRepository canais) {
        this.canais = canais;
    }

    @Transactional
    public Canal garantirCanalPadrao(Empresa empresa) {
        return canais.findByEmpresaIdAndPlataformaAndIdentificador(empresa.getId(), PLATAFORMA_PADRAO, IDENTIFICADOR_PADRAO)
                .orElseGet(() -> canais.save(new Canal(empresa, PLATAFORMA_PADRAO, IDENTIFICADOR_PADRAO)));
    }
}
