package com.digitalize.empresa;

import com.digitalize.comum.ApiException;
import com.digitalize.comum.Cnpj;
import com.digitalize.empresa.EmpresaDtos.EmpresaRequest;
import com.digitalize.empresa.EmpresaDtos.EmpresaResponse;
import com.digitalize.usuario.Usuario;
import com.digitalize.usuario.UsuarioRepository;
import com.digitalize.usuario.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresas;
    private final UsuarioRepository usuarios;

    public EmpresaService(EmpresaRepository empresas, UsuarioRepository usuarios) {
        this.empresas = empresas;
        this.usuarios = usuarios;
    }

    @Transactional
    public Empresa cadastrar(EmpresaRequest request, StatusEmpresa status) {
        String cnpj = Cnpj.validar(request.cnpj());
        if (empresas.existsByCnpj(cnpj)) {
            throw ApiException.conflito("Já existe uma empresa cadastrada com esse CNPJ.");
        }

        Empresa empresa = new Empresa(cnpj);
        preencher(empresa, request);
        empresa.setStatus(status);
        return empresas.save(empresa);
    }

    /** Edição volta a empresa para PENDENTE, para o admin revisar os dados novos. */
    @Transactional
    public void atualizar(String cnpj, EmpresaRequest request) {
        Empresa empresa = buscar(cnpj);
        preencher(empresa, request);
        empresa.setStatus(StatusEmpresa.PENDENTE);
    }

    @Transactional
    public void alterarStatus(String cnpj, String status) {
        StatusEmpresa novoStatus = StatusEmpresa.de(status)
                .orElseThrow(() -> ApiException.requisicaoInvalida("Status inválido."));
        buscar(cnpj).setStatus(novoStatus);
    }

    @Transactional
    public void remover(String cnpj) {
        empresas.delete(buscar(cnpj));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse detalhar(String cnpj) {
        return EmpresaResponse.de(buscar(cnpj));
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresas.findAllByOrderByNomeFantasiaAsc().stream().map(EmpresaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarPorResponsavel(String email) {
        return empresas.findByUsuarioEmailOrderByNomeFantasiaAsc(UsuarioService.normalizarEmail(email))
                .stream().map(EmpresaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public Empresa buscar(String cnpj) {
        return empresas.findByCnpj(Cnpj.somenteDigitos(cnpj))
                .orElseThrow(() -> ApiException.naoEncontrado("Empresa não encontrada."));
    }

    private void preencher(Empresa empresa, EmpresaRequest request) {
        empresa.setNomeFantasia(request.nomeFantasia().trim());
        empresa.setRazaoSocial(request.razaoSocial().trim());
        empresa.setSegmento(textoOuNulo(request.segmento()));
        empresa.setEndereco(textoOuNulo(request.endereco()));
        empresa.setEmailContato(textoOuNulo(UsuarioService.normalizarEmail(request.emailContato())));
        empresa.setUsuario(responsavel(request.responsavelEmail()));
    }

    private Usuario responsavel(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return usuarios.findByEmail(UsuarioService.normalizarEmail(email))
                .orElseThrow(() -> ApiException.requisicaoInvalida(
                        "O email do responsável precisa ser de um usuário cadastrado."));
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
