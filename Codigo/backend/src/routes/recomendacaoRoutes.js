// Importa o Express para criar rotas
const express = require('express');
// Cria um roteador do Express
const router = express.Router();

// Importa o controller de recomendações, que contém toda a lógica de CRUD
const recomendacaoController = require('../controllers/recomendacaoController');

// Rotas de Recomendação

// Listar todas as recomendações
// GET /recomendacao
router.get('/', recomendacaoController.listarRecomendacoes);

// Buscar uma recomendação específica pelo ID
// GET /recomendacao/:id
router.get('/:id', recomendacaoController.buscarRecomendacao);

// Criar uma nova recomendação
// POST /recomendacao
router.post('/', recomendacaoController.criarRecomendacao);

// Atualizar uma recomendação existente pelo ID
// PUT /recomendacao/:id
router.put('/:id', recomendacaoController.atualizarRecomendacao);

// Deletar uma recomendação pelo ID
// DELETE /recomendacao/:id
router.delete('/:id', recomendacaoController.deletarRecomendacao);

// Exporta o roteador para uso no app principal
module.exports = router;
