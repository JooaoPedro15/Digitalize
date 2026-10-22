package com.digitalize.empresa;

import com.digitalize.comum.ApiException;
import com.digitalize.comum.Cnpj;
import com.digitalize.empresa.EmpresaDtos.EmpresaPublicaResponse;
import com.digitalize.empresa.EmpresaDtos.EmpresaRequest;
import com.digitalize.empresa.EmpresaDtos.EmpresaResponse;
import com.digitalize.seguranca.UsuarioLogado;
import com.digitalize.usuario.Usuario;
import com.digitalize.usuario.UsuarioRepository;
import com.digitalize.usuario.UsuarioService;
import org.springframework.http.HttpStatus;
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

    /** Cadastro feito pelo empreendedor: ele mesmo é o responsável e a empresa aguarda aprovação. */
    @Transactional
    public Empresa cadastrar(EmpresaRequest request, UsuarioLogado solicitante) {
        Usuario responsavel = usuarios.getReferenceById(solicitante.id());
        return criar(request, responsavel, StatusEmpresa.PENDENTE);
    }

    /** Cadastro pelo painel administrativo: já entra aprovada; o responsável é opcional. */
    @Transactional
    public Empresa cadastrarComoAdmin(EmpresaRequest request) {
        return criar(request, responsavelPorEmail(request.responsavelEmail()), StatusEmpresa.APROVADA);
    }

    /** Edição volta a empresa para PENDENTE, para o admin revisar os dados novos. */
    @Transactional
    public void atualizar(String cnpj, EmpresaRequest request, UsuarioLogado solicitante) {
        Empresa empresa = buscarComAcesso(cnpj, solicitante);
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
    public EmpresaResponse detalhar(String cnpj, UsuarioLogado solicitante) {
        return EmpresaResponse.de(buscarComAcesso(cnpj, solicitante));
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresas.findAllByOrderByNomeFantasiaAsc().stream().map(EmpresaResponse::de).toList();
    }

    /** Vitrine pública: só empresas aprovadas, sem dados do responsável. */
    @Transactional(readOnly = true)
    public List<EmpresaPublicaResponse> listarPublicas() {
        return empresas.findByStatusOrderByNomeFantasiaAsc(StatusEmpresa.APROVADA).stream()
                .map(EmpresaPublicaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarPorResponsavel(String email, UsuarioLogado solicitante) {
        String emailNormalizado = UsuarioService.normalizarEmail(email);
        if (!solicitante.admin() && !emailNormalizado.equals(solicitante.email())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Você só pode ver as suas empresas.");
        }
        return empresas.findByUsuarioEmailOrderByNomeFantasiaAsc(emailNormalizado)
                .stream().map(EmpresaResponse::de).toList();
    }

    /** Busca a empresa conferindo se quem pediu é o responsável por ela (ou admin). */
    @Transactional(readOnly = true)
    public Empresa buscarComAcesso(String cnpj, UsuarioLogado solicitante) {
        Empresa empresa = buscar(cnpj);
        solicitante.exigirAcesso(empresa);
        return empresa;
    }

    @Transactional(readOnly = true)
    public Empresa buscar(String cnpj) {
        return empresas.findByCnpj(Cnpj.somenteDigitos(cnpj))
                .orElseThrow(() -> ApiException.naoEncontrado("Empresa não encontrada."));
    }

    private Empresa criar(EmpresaRequest request, Usuario responsavel, StatusEmpresa status) {
        String cnpj = Cnpj.validar(request.cnpj());
        if (empresas.existsByCnpj(cnpj)) {
            throw ApiException.conflito("Já existe uma empresa cadastrada com esse CNPJ.");
        }

        Empresa empresa = new Empresa(cnpj);
        preencher(empresa, request);
        empresa.setUsuario(responsavel);
        empresa.setStatus(status);
        return empresas.save(empresa);
    }

    private void preencher(Empresa empresa, EmpresaRequest request) {
        empresa.setNomeFantasia(request.nomeFantasia().trim());
        empresa.setRazaoSocial(request.razaoSocial().trim());
        empresa.setSegmento(textoOuNulo(request.segmento()));
        empresa.setEndereco(textoOuNulo(request.endereco()));
        empresa.setEmailContato(textoOuNulo(UsuarioService.normalizarEmail(request.emailContato())));
    }

    private Usuario responsavelPorEmail(String email) {
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
