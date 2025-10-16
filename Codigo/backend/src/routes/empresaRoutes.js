// Importa o Express para criar as rotas
const express = require('express');
// Cria um “roteador” do Express
const router = express.Router();

// Importa o controller da empresa, que contém a lógica de cada operação
const empresaController = require('../controllers/empresaController');

// Rotas de Empresa

// Rota para listar todas as empresas
// GET /empresa
router.get('/', empresaController.listarEmpresas);

// Rota para buscar uma empresa específica pelo CNPJ
// GET /empresa/:cnpj
router.get('/:cnpj', empresaController.buscarEmpresa);

// Rota para criar uma nova empresa
// POST /empresa
router.post('/', empresaController.criarEmpresa);

// Rota para atualizar uma empresa existente pelo CNPJ
// PUT /empresa/:cnpj
router.put('/:cnpj', empresaController.atualizarEmpresa);

// Rota para deletar uma empresa pelo CNPJ
// DELETE /empresa/:cnpj
router.delete('/:cnpj', empresaController.deletarEmpresa);

// Exporta o roteador para que possa ser usado no app principal
module.exports = router;
