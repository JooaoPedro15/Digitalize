// Importa o modelo Recomendacao para interagir com o banco de dados
const Recomendacao = require('../models/Recomendacao');

// CRUD de Recomendações

// Listar todas as recomendações
const listarRecomendacoes = async (req, res) => {
    try {
        // Busca todas as recomendações no banco
        const recomendacoes = await Recomendacao.findAll();
        // Retorna os dados em formato JSON
        res.json(recomendacoes);
    } catch (error) {
        // Em caso de erro, retorna status 500 com mensagem
        res.status(500).json({ mensagem: 'Erro ao listar recomendações', erro: error.message });
    }
};

// Buscar uma recomendação pelo ID
const buscarRecomendacao = async (req, res) => {
    try {
        const { id } = req.params;
        // Busca recomendação pelo ID
        const recomendacao = await Recomendacao.findOne({ where: { id } });
        if (!recomendacao) {
            // Se não encontrar, retorna 404
            return res.status(404).json({ mensagem: 'Recomendação não encontrada' });
        }
        res.json(recomendacao);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao buscar recomendação', erro: error.message });
    }
};

// Criar uma nova recomendação
const criarRecomendacao = async (req, res) => {
    try {
        const { titulo, conteudo, cnpj_empresa } = req.body;

        // Cria e salva a nova recomendação no banco
        const novaRecomendacao = await Recomendacao.create({
            titulo,
            conteudo,
            cnpj_empresa  // Associa a recomendação à empresa correspondente
        });

        res.status(201).json(novaRecomendacao);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao criar recomendação', erro: error.message });
    }
};

// Atualizar uma recomendação existente
const atualizarRecomendacao = async (req, res) => {
    try {
        const { id } = req.params;
        const { titulo, conteudo } = req.body;

        // Busca a recomendação pelo ID
        const recomendacao = await Recomendacao.findOne({ where: { id } });
        if (!recomendacao) {
            return res.status(404).json({ mensagem: 'Recomendação não encontrada' });
        }

        // Atualiza os campos informados
        await recomendacao.update({ titulo, conteudo });

        res.json(recomendacao);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao atualizar recomendação', erro: error.message });
    }
};

// Deletar uma recomendação
const deletarRecomendacao = async (req, res) => {
    try {
        const { id } = req.params;

        // Busca a recomendação pelo ID
        const recomendacao = await Recomendacao.findOne({ where: { id } });
        if (!recomendacao) {
            return res.status(404).json({ mensagem: 'Recomendação não encontrada' });
        }

        // Remove a recomendação do banco
        await recomendacao.destroy();
        res.json({ mensagem: 'Recomendação deletada com sucesso' });
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao deletar recomendação', erro: error.message });
    }
};

// Exporta todas as funções para uso nas rotas
module.exports = {
    listarRecomendacoes,
    buscarRecomendacao,
    criarRecomendacao,
    atualizarRecomendacao,
    deletarRecomendacao
};
