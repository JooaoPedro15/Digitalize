package com.digitalize.empresa;

import com.digitalize.comum.Mensagem;
import com.digitalize.empresa.EmpresaDtos.CadastroResponse;
import com.digitalize.empresa.EmpresaDtos.EmpresaRequest;
import com.digitalize.empresa.EmpresaDtos.EmpresaResponse;
import com.digitalize.empresa.EmpresaDtos.StatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    /** Cadastro feito pelo próprio empreendedor: entra como pendente. */
    @PostMapping("/api/empresas")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastroResponse cadastrar(@Valid @RequestBody EmpresaRequest request) {
        return CadastroResponse.de(empresaService.cadastrar(request, StatusEmpresa.PENDENTE));
    }

    /** Cadastro feito pelo painel administrativo: já entra aprovada. */
    @PostMapping("/admin/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastroResponse cadastrarComoAdmin(@Valid @RequestBody EmpresaRequest request) {
        return CadastroResponse.de(empresaService.cadastrar(request, StatusEmpresa.APROVADA));
    }

    @GetMapping("/api/empresas")
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/api/empresas/usuario/{email}")
    public List<EmpresaResponse> listarPorResponsavel(@PathVariable String email) {
        return empresaService.listarPorResponsavel(email);
    }

    @GetMapping("/api/empresas/{cnpj}")
    public EmpresaResponse detalhar(@PathVariable String cnpj) {
        return empresaService.detalhar(cnpj);
    }

    @PutMapping("/api/empresas/{cnpj}")
    public Mensagem atualizar(@PathVariable String cnpj, @Valid @RequestBody EmpresaRequest request) {
        empresaService.atualizar(cnpj, request);
        return Mensagem.sucesso("Empresa atualizada.");
    }

    @PutMapping("/api/empresas/{cnpj}/status")
    public Mensagem alterarStatus(@PathVariable String cnpj, @RequestBody StatusRequest request) {
        empresaService.alterarStatus(cnpj, request.status());
        return Mensagem.sucesso("Status atualizado.");
    }

    @DeleteMapping("/api/empresas/{cnpj}")
    public Mensagem remover(@PathVariable String cnpj) {
        empresaService.remover(cnpj);
        return Mensagem.sucesso("Empresa removida.");
    }
}
