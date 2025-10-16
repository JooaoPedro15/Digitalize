// Importa o modelo Canal para interagir com o banco de dados
const Canal = require('../models/Canal');

// CRUD de Canais

// Listar todos os canais
const listarCanais = async (req, res) => {
    try {
        // Busca todos os canais no banco
        const canais = await Canal.findAll();
        // Retorna os canais em formato JSON
        res.json(canais);
    } catch (error) {
        // Se ocorrer erro, retorna status 500 com mensagem
        res.status(500).json({ mensagem: 'Erro ao listar canais', erro: error.message });
    }
};

// Buscar um canal pelo ID
const buscarCanal = async (req, res) => {
    try {
        const { id } = req.params;
        // Busca canal pelo ID
        const canal = await Canal.findOne({ where: { id } });
        if (!canal) {
            // Se não encontrar, retorna 404
            return res.status(404).json({ mensagem: 'Canal não encontrado' });
        }
        res.json(canal);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao buscar canal', erro: error.message });
    }
};

// Criar um novo canal
const criarCanal = async (req, res) => {
    try {
        const { nome, tipo, views, likes, alcance, cnpj_empresa } = req.body;

        // Cria e salva o novo canal no banco
        const novoCanal = await Canal.create({
            nome,
            tipo,           // tipo de canal, ex: Instagram, YouTube
            views,
            likes,
            alcance,
            cnpj_empresa    // associa o canal a uma empresa existente
        });

        res.status(201).json(novoCanal);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao criar canal', erro: error.message });
    }
};

// Atualizar um canal existente
const atualizarCanal = async (req, res) => {
    try {
        const { id } = req.params;
        const { nome, tipo, views, likes, alcance } = req.body;

        // Busca o canal pelo ID
        const canal = await Canal.findOne({ where: { id } });
        if (!canal) {
            return res.status(404).json({ mensagem: 'Canal não encontrado' });
        }

        // Atualiza os campos informados
        await canal.update({ nome, tipo, views, likes, alcance });

        res.json(canal);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao atualizar canal', erro: error.message });
    }
};

// Deletar um canal
const deletarCanal = async (req, res) => {
    try {
        const { id } = req.params;

        // Busca o canal pelo ID
        const canal = await Canal.findOne({ where: { id } });
        if (!canal) {
            return res.status(404).json({ mensagem: 'Canal não encontrado' });
        }

        // Remove o canal do banco
        await canal.destroy();
        res.json({ mensagem: 'Canal deletado com sucesso' });
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao deletar canal', erro: error.message });
    }
};

// Exporta todas as funções para uso nas rotas
module.exports = {
    listarCanais,
    buscarCanal,
    criarCanal,
    atualizarCanal,
    deletarCanal
};
