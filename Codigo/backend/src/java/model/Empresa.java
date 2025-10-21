package model;

/*
MODELO: Empresa

Representa cada empresa cadastrada no sistema.

Campos:
- cnpj: número único da empresa (como o CPF, mas para empresas)
- nome_fantasia: nome que a empresa usa no dia a dia
- razao_social: nome legal da empresa
- segmento: área de atuação, ex: "Tecnologia", "Alimentos"
- endereco: endereço completo da empresa
- status: se a empresa está ativa ou inativa

Relacionamentos:
- Uma empresa pode ter vários canais de mídia social
- Uma empresa pode ter várias recomendações
*/

const Empresa = sequelize.define('Empresa', {
    // Número único de identificação da empresa
    cnpj: {
        type: DataTypes.CHAR(14), // 14 números, como definido no banco
        primaryKey: true,          // identificador único
        allowNull: false,          // não pode ficar vazio
        unique: true,              // não pode existir duas empresas com o mesmo CNPJ
        validate: {
            is: /^[0-9]{14}$/      // valida que só contém 14 números
        }
    },

    // Nome fantasia usado no dia a dia
    nome_fantasia: {
        type: DataTypes.STRING(120), 
        allowNull: false
    },

    // Razão social legal
    razao_social: {
        type: DataTypes.STRING(120),
        allowNull: false
    },

    // Segmento/área de atuação da empresa
    segmento: {
        type: DataTypes.STRING(40)
    },

    // Endereço completo da empresa
    endereco: {
        type: DataTypes.STRING(200)
    },

    // Status da empresa: ATIVA ou INATIVA
    status: {
        type: DataTypes.ENUM('ATIVA','INATIVA'), // só pode ser um desses dois
        allowNull: false,
        defaultValue: 'ATIVA'
    }

}, {
    tableName: 'empresa',      // nome da tabela no banco
    schema: 'midiasocial',     // schema usado no banco
    timestamps: true           // cria automaticamente campos createdAt e updatedAt
});

// RELACIONAMENTOS

// Função para associar Empresa a outros modelos
Empresa.associate = (models) => {
    // Uma empresa pode ter vários canais
    Empresa.hasMany(models.Canal, {
        foreignKey: 'empresa_cnpj', // campo do Canal que referencia a empresa
        as: 'canais'
        // onDelete CASCADE: se a empresa for deletada, todos os canais dela também
    });

    // Uma empresa pode ter várias recomendações
    Empresa.hasMany(models.Recomendacao, {
        foreignKey: 'empresa_cnpj', 
        as: 'recomendacoes'
        // onDelete CASCADE: se a empresa for deletada, todas as recomendações dela também
    });
};

// Exporta o modelo para ser usado em outros arquivos
module.exports = Empresa;