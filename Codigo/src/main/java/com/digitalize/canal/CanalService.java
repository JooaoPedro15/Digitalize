package com.digitalize.canal;

import com.digitalize.canal.CanalDtos.CanalRequest;
import com.digitalize.canal.CanalDtos.CanalResponse;
import com.digitalize.comum.ApiException;
import com.digitalize.comum.Cnpj;
import com.digitalize.empresa.Empresa;
import com.digitalize.empresa.EmpresaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CanalService {

    static final String PLATAFORMA_PADRAO = "GENERICA";
    static final String IDENTIFICADOR_PADRAO = "canal-principal";

    private final CanalRepository canais;
    private final EmpresaService empresaService;

    public CanalService(CanalRepository canais, EmpresaService empresaService) {
        this.canais = canais;
        this.empresaService = empresaService;
    }

    @Transactional
    public Canal criar(CanalRequest request) {
        Empresa empresa = empresaService.buscar(Cnpj.validar(request.empresaCnpj()));
        String plataforma = textoOuPadrao(request.plataforma(), PLATAFORMA_PADRAO);
        String identificador = textoOuPadrao(request.canalIdentificador(), IDENTIFICADOR_PADRAO);

        if (canais.findByEmpresaIdAndPlataformaAndIdentificador(empresa.getId(), plataforma, identificador).isPresent()) {
            throw ApiException.conflito("Esse canal já está cadastrado para a empresa.");
        }
        return canais.save(new Canal(empresa, plataforma, identificador));
    }

    /** Retorna o canal padrão da empresa, criando-o se ainda não existir. */
    @Transactional
    public Canal garantirCanalPadrao(Empresa empresa) {
        return canais.findByEmpresaIdAndPlataformaAndIdentificador(empresa.getId(), PLATAFORMA_PADRAO, IDENTIFICADOR_PADRAO)
                .orElseGet(() -> canais.save(new Canal(empresa, PLATAFORMA_PADRAO, IDENTIFICADOR_PADRAO)));
    }

    @Transactional(readOnly = true)
    public List<CanalResponse> listarPorEmpresa(String cnpj) {
        return canais.findByEmpresaCnpjOrderByIdAsc(Cnpj.somenteDigitos(cnpj)).stream()
                .map(CanalResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public Canal buscar(Long id) {
        return canais.findById(id)
                .orElseThrow(() -> ApiException.naoEncontrado("Canal não encontrado."));
    }

    private static String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }
}
