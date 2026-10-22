package com.digitalize.canal;

import com.digitalize.canal.CanalDtos.CanalRequest;
import com.digitalize.canal.CanalDtos.CanalResponse;
import com.digitalize.comum.ApiException;
import com.digitalize.comum.Cnpj;
import com.digitalize.empresa.Empresa;
import com.digitalize.empresa.EmpresaService;
import com.digitalize.seguranca.UsuarioLogado;
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
    public Canal criar(CanalRequest request, UsuarioLogado solicitante) {
        Empresa empresa = empresaService.buscarComAcesso(Cnpj.validar(request.empresaCnpj()), solicitante);
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
    public List<CanalResponse> listarPorEmpresa(String cnpj, UsuarioLogado solicitante) {
        Empresa empresa = empresaService.buscarComAcesso(cnpj, solicitante);
        return canais.findByEmpresaCnpjOrderByIdAsc(empresa.getCnpj()).stream()
                .map(CanalResponse::de)
                .toList();
    }

    /** Busca o canal conferindo se quem pediu é responsável pela empresa dona dele (ou admin). */
    @Transactional(readOnly = true)
    public Canal buscarComAcesso(Long id, UsuarioLogado solicitante) {
        Canal canal = canais.findById(id)
                .orElseThrow(() -> ApiException.naoEncontrado("Canal não encontrado."));
        solicitante.exigirAcesso(canal.getEmpresa());
        return canal;
    }

    private static String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }
}
