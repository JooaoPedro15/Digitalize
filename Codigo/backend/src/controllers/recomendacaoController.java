package src.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.models.Recomendacao;
import src.repositories.RecomendacaoRepository;

import java.util.List;
import java.util.Optional;

/**
 * RecomendacaoController
 * Controlador responsável pelas operações CRUD relacionadas à entidade Recomendacao.
 * Permite listar, criar, atualizar e remover recomendações vinculadas a empresas ou canais.
 */
@RestController
@RequestMapping("/recomendacoes")
public class RecomendacaoController {

    // Injeta o repositório de Recomendacao, responsável por acessar o banco de dados
    @Autowired
    private RecomendacaoRepository recomendacaoRepository;

    /**
     * GET /recomendacoes
     * Retorna a lista de todas as recomendações cadastradas.
     */
    @GetMapping
    public ResponseEntity<List<Recomendacao>> listarRecomendacoes() {
        List<Recomendacao> recomendacoes = recomendacaoRepository.findAll();
        return ResponseEntity.ok(recomendacoes);
    }

    /**
     * GET /recomendacoes/{id}
     * Retorna uma recomendação específica com base no ID informado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Recomendacao> recomendacaoOpt = recomendacaoRepository.findById(id);

        if (recomendacaoOpt.isPresent()) {
            return ResponseEntity.ok(recomendacaoOpt.get());
        } else {
            return ResponseEntity.status(404).body("Recomendação não encontrada.");
        }
    }

    /**
     * POST /recomendacoes
     * Cria uma nova recomendação com base nos dados enviados no corpo da requisição.
     */
    @PostMapping
    public ResponseEntity<Recomendacao> criarRecomendacao(@RequestBody Recomendacao recomendacao) {
        Recomendacao novaRecomendacao = recomendacaoRepository.save(recomendacao);
        return ResponseEntity.ok(novaRecomendacao);
    }

    /**
     * PUT /recomendacoes/{id}
     * Atualiza os dados de uma recomendação existente com base no ID informado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarRecomendacao(@PathVariable Long id, @RequestBody Recomendacao recomendacaoAtualizada) {
        Optional<Recomendacao> recomendacaoOpt = recomendacaoRepository.findById(id);

        if (recomendacaoOpt.isPresent()) {
            Recomendacao recomendacao = recomendacaoOpt.get();
            recomendacao.setTipo(recomendacaoAtualizada.getTipo());
            recomendacao.setDetalhes(recomendacaoAtualizada.getDetalhes());
            recomendacao.setEmpresa(recomendacaoAtualizada.getEmpresa());
            recomendacao.setCanal(recomendacaoAtualizada.getCanal());

            recomendacaoRepository.save(recomendacao);
            return ResponseEntity.ok("Recomendação atualizada com sucesso.");
        } else {
            return ResponseEntity.status(404).body("Recomendação não encontrada.");
        }
    }

    /**
     * DELETE /recomendacoes/{id}
     * Remove uma recomendação existente com base no ID informado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarRecomendacao(@PathVariable Long id) {
        if (recomendacaoRepository.existsById(id)) {
            recomendacaoRepository.deleteById(id);
            return ResponseEntity.ok("Recomendação removida com sucesso.");
        } else {
            return ResponseEntity.status(404).body("Recomendação não encontrada.");
        }
    }
}
