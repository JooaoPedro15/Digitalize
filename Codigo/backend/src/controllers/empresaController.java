// Importa o modelo Empresa para poder interagir com o banco de dados
const Empresa = require('../models/Empresa');

// CRUD de Empresas

// Listar todas as empresas
const listarEmpresas = async (req, res) => {
    try {
        // Busca todas as empresas no banco
        const empresas = await Empresa.findAll();
        // Retorna o resultado em JSON
        res.json(empresas);
    } catch (error) {
        // Em caso de erro, retorna status 500 e a mensagem
        res.status(500).json({ mensagem: 'Erro ao listar empresas', erro: error.message });
    }
};

// Buscar uma empresa pelo CNPJ
const buscarEmpresa = async (req, res) => {
    try {
        const { cnpj } = req.params;
        // Busca empresa pelo CNPJ
        const empresa = await Empresa.findOne({ where: { cnpj } });
        if (!empresa) {
            // Se não encontrar, retorna 404
            return res.status(404).json({ mensagem: 'Empresa não encontrada' });
        }
        res.json(empresa);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao buscar empresa', erro: error.message });
    }
};

// Criar uma nova empresa
const criarEmpresa = async (req, res) => {
    try {
        const { cnpj, nome, razaoSocial, segmento, endereco, status } = req.body;

        // Verifica se já existe empresa com o mesmo CNPJ
        const existente = await Empresa.findOne({ where: { cnpj } });
        if (existente) {
            return res.status(400).json({ mensagem: 'Empresa já cadastrada' });
        }

        // Cria e salva a nova empresa no banco
        const novaEmpresa = await Empresa.create({
            cnpj,
            nome,
            razaoSocial,
            segmento,
            endereco,
            status
        });

        res.status(201).json(novaEmpresa);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao criar empresa', erro: error.message });
    }
};

// Atualizar uma empresa existente
const atualizarEmpresa = async (req, res) => {
    try {
        const { cnpj } = req.params;
        const { nome, razaoSocial, segmento, endereco, status } = req.body;

        // Busca a empresa pelo CNPJ
        const empresa = await Empresa.findOne({ where: { cnpj } });
        if (!empresa) {
            return res.status(404).json({ mensagem: 'Empresa não encontrada' });
        }

        // Atualiza os campos informados
        await empresa.update({ nome, razaoSocial, segmento, endereco, status });

        res.json(empresa);
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao atualizar empresa', erro: error.message });
    }
};

// Deletar uma empresa
const deletarEmpresa = async (req, res) => {
    try {
        const { cnpj } = req.params;

        // Busca a empresa pelo CNPJ
        const empresa = await Empresa.findOne({ where: { cnpj } });
        if (!empresa) {
            return res.status(404).json({ mensagem: 'Empresa não encontrada' });
        }

        // Deleta a empresa do banco
        await empresa.destroy();
        res.json({ mensagem: 'Empresa deletada com sucesso' });
    } catch (error) {
        res.status(500).json({ mensagem: 'Erro ao deletar empresa', erro: error.message });
    }
};

// Exporta todas as funções para serem usadas nas rotas
module.exports = {
    listarEmpresas,
    buscarEmpresa,
    criarEmpresa,
    atualizarEmpresa,
    deletarEmpresa
};
