const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

// Ensure required directories exist
const requiredDirs = ['data', 'sprint02'];
requiredDirs.forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
    console.log(`Created directory: ${dir}`);
  }
});

const app = express();
const PORT = 5000;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'frontend/public')));

// Arquivos de dados
const UNIFIED_DB_FILE = path.join(__dirname, 'data/unified_db.json');
const SPRINT02_EMPRESAS_FILE = path.join(__dirname, 'sprint02/empresas.json');
const APROVADOS_DB_FILE = path.join(__dirname, 'data/aprovados_db.json');
const REJEITADOS_DB_FILE = path.join(__dirname, 'data/rejeitados_db.json');
const PENDENTES_DB_FILE = path.join(__dirname, 'data/pendentes_db.json');

// Função para carregar base de dados unificada
function loadUnifiedDB() {
  if (fs.existsSync(UNIFIED_DB_FILE)) {
    const content = fs.readFileSync(UNIFIED_DB_FILE, 'utf8');
    return JSON.parse(content || '{"empresas":[],"produtos":[],"usuarios":[],"parceiros":[],"avaliacoes":[]}');
  }
  return {"empresas":[],"produtos":[],"usuarios":[],"parceiros":[],"avaliacoes":[]};
}

// Função para salvar base de dados unificada
function saveUnifiedDB(data) {
  fs.writeFileSync(UNIFIED_DB_FILE, JSON.stringify(data, null, 2));
}

// Função para carregar empresas do sprint02
function loadSprint02Empresas() {
  if (fs.existsSync(SPRINT02_EMPRESAS_FILE)) {
    const content = fs.readFileSync(SPRINT02_EMPRESAS_FILE, 'utf8');
    return JSON.parse(content || '[]');
  }
  return [];
}

// Função para salvar empresas no sprint02
function saveSprint02Empresas(empresas) {
  fs.writeFileSync(SPRINT02_EMPRESAS_FILE, JSON.stringify(empresas, null, 2));
}

// Funções para carregar arquivos por status
function loadAprovados() {
  if (fs.existsSync(APROVADOS_DB_FILE)) {
    const content = fs.readFileSync(APROVADOS_DB_FILE, 'utf8');
    return JSON.parse(content || '[]');
  }
  return [];
}

function loadRejeitados() {
  if (fs.existsSync(REJEITADOS_DB_FILE)) {
    const content = fs.readFileSync(REJEITADOS_DB_FILE, 'utf8');
    return JSON.parse(content || '[]');
  }
  return [];
}

function loadPendentes() {
  if (fs.existsSync(PENDENTES_DB_FILE)) {
    const content = fs.readFileSync(PENDENTES_DB_FILE, 'utf8');
    return JSON.parse(content || '[]');
  }
  return [];
}

// Funções para salvar arquivos por status
function saveAprovados(empresas) {
  fs.writeFileSync(APROVADOS_DB_FILE, JSON.stringify(empresas, null, 2));
}

function saveRejeitados(empresas) {
  fs.writeFileSync(REJEITADOS_DB_FILE, JSON.stringify(empresas, null, 2));
}

function savePendentes(empresas) {
  fs.writeFileSync(PENDENTES_DB_FILE, JSON.stringify(empresas, null, 2));
}

// Função para sincronizar arquivos por status
function syncStatusFiles() {
  const db = loadUnifiedDB();
  const sprint02Empresas = loadSprint02Empresas();

  // Criar um mapa para evitar duplicatas por ID
  const empresasMap = new Map();

  // Adicionar empresas da base unificada
  db.empresas.forEach(empresa => {
    empresasMap.set(empresa.id.toString(), empresa);
  });

  // Adicionar empresas do sprint02, sem sobrescrever se já existir
  sprint02Empresas.forEach(empresa => {
    if (!empresasMap.has(empresa.id.toString())) {
      empresasMap.set(empresa.id.toString(), empresa);
    }
  });

  // Converter mapa para array
  const todasEmpresas = Array.from(empresasMap.values());

  // Separar por status
  const aprovadas = todasEmpresas.filter(e => e.status === 'aprovada' || e.status === 'aprovado');
  const rejeitadas = todasEmpresas.filter(e => e.status === 'rejeitada' || e.status === 'rejeitado');
  const pendentes = todasEmpresas.filter(e => e.status === 'pendente' || !e.status);

  // Salvar arquivos separados
  saveAprovados(aprovadas);
  saveRejeitados(rejeitadas);
  savePendentes(pendentes);

  console.log(`Arquivos sincronizados - Aprovadas: ${aprovadas.length}, Rejeitadas: ${rejeitadas.length}, Pendentes: ${pendentes.length}`);
}

// Middleware de autenticação simples
function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ error: 'Token de autenticação necessário' });
  }
  // Aqui você pode implementar validação de token mais robusta
  next();
}

// === ROTAS DA API UNIFICADA ===

// Rota principal
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'frontend/public/index.html'));
});

// Rota para todas as empresas da base unificada
app.get('/api/empresas', (req, res) => {
  const db = loadUnifiedDB();
  const sprint02Empresas = loadSprint02Empresas();

  // Criar um mapa para evitar duplicatas por ID
  const empresasMap = new Map();

  // Adicionar empresas da base unificada
  db.empresas.forEach(empresa => {
    empresasMap.set(empresa.id.toString(), empresa);
  });

  // Adicionar empresas do sprint02, sem sobrescrever se já existir
  sprint02Empresas.forEach(empresa => {
    if (!empresasMap.has(empresa.id.toString())) {
      empresasMap.set(empresa.id.toString(), empresa);
    }
  });

  // Converter mapa para array
  const todasEmpresas = Array.from(empresasMap.values());
  res.json(todasEmpresas);
});

// Rotas para empresas por status
app.get('/api/empresas/aprovadas', (req, res) => {
  const aprovadas = loadAprovados();
  res.json(aprovadas);
});

app.get('/api/empresas/rejeitadas', (req, res) => {
  const rejeitadas = loadRejeitados();
  res.json(rejeitadas);
});

app.get('/api/empresas/pendentes', (req, res) => {
  const pendentes = loadPendentes();
  res.json(pendentes);
});

// Rota para outras entidades
app.get('/api/produtos', (req, res) => {
  const db = loadUnifiedDB();
  res.json(db.produtos);
});

app.get('/api/usuarios', (req, res) => {
  const db = loadUnifiedDB();
  res.json(db.usuarios);
});

app.get('/api/parceiros', (req, res) => {
  const db = loadUnifiedDB();
  res.json(db.parceiros);
});

app.get('/api/avaliacoes', (req, res) => {
  const db = loadUnifiedDB();
  res.json(db.avaliacoes);
});

// === ROTAS DO SPRINT02 (COMPATIBILIDADE) ===

// Cadastrar nova empresa (sprint02 format)
app.post('/admin/cadastro', (req, res) => {
  const novaEmpresa = req.body;

  // Adiciona um ID único baseado no timestamp
  novaEmpresa.id = Date.now();
  novaEmpresa.status = 'pendente';
  novaEmpresa.dataCadastro = new Date().toISOString();

  // Salvar apenas no formato sprint02 para evitar duplicação
  const empresasSprint02 = loadSprint02Empresas();
  empresasSprint02.push(novaEmpresa);
  saveSprint02Empresas(empresasSprint02);

  // Sincronizar arquivos por status
  syncStatusFiles();

  console.log(" -> Nova empresa cadastrada:", novaEmpresa);
  res.status(200).json({ mensagem: "Cadastro salvo com sucesso!", id: novaEmpresa.id });
});

// Rota para consultar todas empresas (sprint02)
app.get('/admin/empresas', (req, res) => {
  const empresas = loadSprint02Empresas();
  res.json(empresas);
});

// Rota para obter empresas por usuário (sprint02 - mantida para compatibilidade)
app.get('/admin/ultima-empresa', (req, res) => {
  const email = req.query.email;
  const empresas = loadSprint02Empresas();

  if (empresas.length === 0) {
    return res.status(404).json({ mensagem: "Nenhuma empresa cadastrada" });
  }

  let empresaEncontrada;

  if (email) {
    // Buscar por email do responsável
    empresaEncontrada = empresas.find(e => e.responsavelEmail === email);
  } else {
    // Fallback: última empresa cadastrada
    empresaEncontrada = [...empresas].sort((a, b) => b.id - a.id)[0];
  }

  if (!empresaEncontrada) {
    return res.status(404).json({ mensagem: "Nenhuma empresa encontrada para este usuário" });
  }

  res.json(empresaEncontrada);
});

// Rota para obter uma empresa específica por ID (sprint02)
app.get('/admin/empresa/:id', (req, res) => {
  const empresas = loadSprint02Empresas();
  const empresa = empresas.find(e => e.id == req.params.id);

  if (!empresa) {
    return res.status(404).json({ mensagem: "Empresa não encontrada" });
  }

  res.json(empresa);
});

// === ROTAS DA API NOVA ===

// Cadastrar empresa (formato novo)
app.post('/api/empresas', (req, res) => {
  const novaEmpresa = req.body;
  const db = loadUnifiedDB();

  // Gerar ID único
  const novoId = Date.now().toString();
  novaEmpresa.id = novoId;
  novaEmpresa.status = 'pendente';
  novaEmpresa.dataCadastro = new Date().toISOString();

  db.empresas.push(novaEmpresa);
  saveUnifiedDB(db);

  // Sincronizar arquivos por status
  syncStatusFiles();

  res.status(201).json(novaEmpresa);
});

// Aprovar/Negar empresa
app.put('/api/empresas/:id/status', (req, res) => {
  const { id } = req.params;
  const { status } = req.body;

  if (!['aprovada', 'rejeitada'].includes(status)) {
    return res.status(400).json({ error: 'Status inválido' });
  }

  // Atualizar na base unificada
  const db = loadUnifiedDB();
  const empresaIndex = db.empresas.findIndex(e => e.id === id);

  if (empresaIndex !== -1) {
    db.empresas[empresaIndex].status = status;
    saveUnifiedDB(db);
  }

  // Atualizar no sprint02
  const empresasSprint02 = loadSprint02Empresas();
  const sprint02Index = empresasSprint02.findIndex(e => e.id == id);

  if (sprint02Index !== -1) {
    empresasSprint02[sprint02Index].status = status;
    saveSprint02Empresas(empresasSprint02);
  }

  // Atualizar arquivos de status
  syncStatusFiles();

  res.json({ message: `Empresa ${status} com sucesso` });
});

// Buscar empresas por usuário
app.get('/api/empresas/usuario/:email', (req, res) => {
  const { email } = req.params;
  const db = loadUnifiedDB();
  const sprint02Empresas = loadSprint02Empresas();

  // Criar um mapa para evitar duplicatas por ID
  const empresasMap = new Map();

  // Buscar nas duas bases
  const empresasUnificadas = db.empresas.filter(e => 
    e.responsavel?.email === email || e.responsavelEmail === email
  );

  const empresasSprint02Filtradas = sprint02Empresas.filter(e => 
    e.responsavelEmail === email
  );

  // Adicionar empresas evitando duplicatas
  empresasUnificadas.forEach(empresa => {
    empresasMap.set(empresa.id.toString(), empresa);
  });

  empresasSprint02Filtradas.forEach(empresa => {
    if (!empresasMap.has(empresa.id.toString())) {
      empresasMap.set(empresa.id.toString(), empresa);
    }
  });

  const todasEmpresas = Array.from(empresasMap.values());
  res.json(todasEmpresas);
});

// Rota de login simples
app.post('/api/login', (req, res) => {
  const { email, senha } = req.body;
  const db = loadUnifiedDB();

  const usuario = db.usuarios.find(u => u.email === email && u.senha === senha);

  if (usuario) {
    // Remover senha da resposta
    const { senha: _, ...usuarioSeguro } = usuario;
    res.json({
      success: true,
      message: 'Login realizado com sucesso',
      usuario: usuarioSeguro,
      token: 'fake-jwt-token-' + usuario.id
    });
  } else {
    res.status(401).json({
      success: false,
      error: 'Email ou senha incorretos'
    });
  }
});

// Cadastrar usuário
app.post('/api/usuarios', (req, res) => {
  const novoUsuario = req.body;
  const db = loadUnifiedDB();

  // Verificar se email já existe
  const emailExiste = db.usuarios.find(u => u.email === novoUsuario.email);
  if (emailExiste) {
    return res.status(400).json({ error: 'Email já cadastrado' });
  }

  // Gerar novo ID
  const novoId = Math.max(...db.usuarios.map(u => u.id), 0) + 1;
  novoUsuario.id = novoId;
  novoUsuario.ativo = true;
  novoUsuario.dataCadastro = new Date().toISOString();
  novoUsuario.favoritos = []; // Inicializar favoritos

  db.usuarios.push(novoUsuario);
  saveUnifiedDB(db);

  res.status(201).json({ message: 'Usuário cadastrado com sucesso' });
});

// === ROTAS PARA AVALIAÇÕES ===

// Obter avaliações de uma empresa
app.get('/api/avaliacoes/empresa/:empresaId', (req, res) => {
  const { empresaId } = req.params;
  const db = loadUnifiedDB();

  const avaliacoes = db.avaliacoes.filter(a => a.empresaId === empresaId);
  res.json(avaliacoes);
});

// Criar nova avaliação
app.post('/api/avaliacoes', (req, res) => {
  const novaAvaliacao = req.body;
  const db = loadUnifiedDB();

  // Gerar ID único
  const novoId = Date.now().toString();
  novaAvaliacao.id = novoId;
  novaAvaliacao.dataAvaliacao = new Date().toISOString();

  db.avaliacoes.push(novaAvaliacao);
  saveUnifiedDB(db);

  res.status(201).json(novaAvaliacao);
});

// === ROTAS PARA FAVORITOS ===

// Obter favoritos do usuário
app.get('/api/favoritos/:usuarioEmail', (req, res) => {
  const { usuarioEmail } = req.params;
  const db = loadUnifiedDB();

  const usuario = db.usuarios.find(u => u.email === usuarioEmail);
  if (!usuario) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }

  // Obter dados completos das empresas favoritas
  const favoritos = (usuario.favoritos || []).map(empresaId => {
    return db.empresas.find(e => e.id === empresaId) || 
           loadSprint02Empresas().find(e => e.id == empresaId);
  }).filter(Boolean);

  res.json(favoritos);
});

// Adicionar empresa aos favoritos
app.post('/api/favoritos/:usuarioEmail/:empresaId', (req, res) => {
  const { usuarioEmail, empresaId } = req.params;
  const db = loadUnifiedDB();

  const usuario = db.usuarios.find(u => u.email === usuarioEmail);
  if (!usuario) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }

  if (!usuario.favoritos) {
    usuario.favoritos = [];
  }

  if (!usuario.favoritos.includes(empresaId)) {
    usuario.favoritos.push(empresaId);
    saveUnifiedDB(db);
    res.json({ message: 'Empresa adicionada aos favoritos' });
  } else {
    res.status(400).json({ error: 'Empresa já está nos favoritos' });
  }
});

// Remover empresa dos favoritos
app.delete('/api/favoritos/:usuarioEmail/:empresaId', (req, res) => {
  const { usuarioEmail, empresaId } = req.params;
  const db = loadUnifiedDB();

  const usuario = db.usuarios.find(u => u.email === usuarioEmail);
  if (!usuario) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }

  if (usuario.favoritos) {
    usuario.favoritos = usuario.favoritos.filter(id => id !== empresaId);
    saveUnifiedDB(db);
    res.json({ message: 'Empresa removida dos favoritos' });
  } else {
    res.status(400).json({ error: 'Empresa não está nos favoritos' });
  }
});

// Função para escrever empresas no arquivo
function writeEmpresas(empresas) {
    fs.writeFileSync(SPRINT02_EMPRESAS_FILE, JSON.stringify(empresas, null, 2));
}

// Função para ler empresas do arquivo
function readEmpresas() {
    if (fs.existsSync(SPRINT02_EMPRESAS_FILE)) {
        const content = fs.readFileSync(SPRINT02_EMPRESAS_FILE, 'utf8');
        return JSON.parse(content || '[]');
    }
    return [];
}

// Função para atualizar arquivos de status
function updateStatusFiles() {
    try {
        const empresas = readEmpresas();

        // Separar por status
        const aprovadas = empresas.filter(e => e.status === 'aprovada' || e.status === 'aprovado');
        const pendentes = empresas.filter(e => e.status === 'pendente');
        const rejeitadas = empresas.filter(e => e.status === 'rejeitada' || e.status === 'rejeitado');

        // Atualizar arquivos
        saveAprovados(aprovadas);
        savePendentes(pendentes);
        saveRejeitados(rejeitadas);

        // Atualizar unified_db.json
        const db = loadUnifiedDB();
        db.empresas = empresas;
        saveUnifiedDB(db);

        console.log('Arquivos de status atualizados com sucesso');
    } catch (error) {
        console.error('Erro ao atualizar arquivos de status:', error);
    }
}

// Endpoint para buscar empresas por email do responsável
app.get('/api/empresas/usuario/:email', (req, res) => {
    const { email } = req.params;
    const db = loadUnifiedDB();
    const sprint02Empresas = loadSprint02Empresas();

    // Criar um mapa para evitar duplicatas por ID
    const empresasMap = new Map();

    // Buscar nas duas bases
    const empresasUnificadas = db.empresas.filter(e => 
      e.responsavel?.email === email || e.responsavelEmail === email
    );
  
    const empresasSprint02Filtradas = sprint02Empresas.filter(e => 
      e.responsavelEmail === email
    );
  
    // Adicionar empresas evitando duplicatas
    empresasUnificadas.forEach(empresa => {
      empresasMap.set(empresa.id.toString(), empresa);
    });
  
    empresasSprint02Filtradas.forEach(empresa => {
      if (!empresasMap.has(empresa.id.toString())) {
        empresasMap.set(empresa.id.toString(), empresa);
      }
    });
  
    const todasEmpresas = Array.from(empresasMap.values());
    res.json(todasEmpresas);
});

// Endpoint para atualizar empresa
app.put('/api/empresas/:id', (req, res) => {
    const { id } = req.params;
    const dadosAtualizados = req.body;
  
    // Garante que o status seja pendente
    dadosAtualizados.status = 'pendente';
  
    try {
        // Atualizar na base unificada
        const db = loadUnifiedDB();
        const empresaIndex = db.empresas.findIndex(e => e.id === id);
  
        if (empresaIndex === -1) {
            return res.status(404).json({ error: 'Empresa não encontrada' });
        }
  
        // Preservar ID e data de cadastro
        dadosAtualizados.id = db.empresas[empresaIndex].id;
        dadosAtualizados.dataCadastro = db.empresas[empresaIndex].dataCadastro;
        dadosAtualizados.dataAtualizacao = new Date().toISOString();
  
        db.empresas[empresaIndex] = { ...db.empresas[empresaIndex], ...dadosAtualizados };
        saveUnifiedDB(db);
  
        // Atualizar no sprint02
        const empresasSprint02 = loadSprint02Empresas();
        const sprint02Index = empresasSprint02.findIndex(e => e.id == id);
  
        if (sprint02Index !== -1) {
            empresasSprint02[sprint02Index] = { ...empresasSprint02[sprint02Index], ...dadosAtualizados };
            saveSprint02Empresas(empresasSprint02);
        }
  
        // Atualizar arquivos de status
        syncStatusFiles();
  
        res.json({ message: 'Empresa atualizada com sucesso', empresa: db.empresas[empresaIndex] });
    } catch (error) {
        console.error('Erro ao atualizar empresa:', error);
        res.status(500).json({ error: 'Erro interno do servidor' });
    }
});

// Endpoint para excluir empresa
app.delete('/api/empresas/:id', (req, res) => {
    const { id } = req.params;
  
    try {
        // Remover da base unificada
        const db = loadUnifiedDB();
        const empresaIndex = db.empresas.findIndex(e => e.id === id);
  
        if (empresaIndex !== -1) {
            db.empresas.splice(empresaIndex, 1);
            saveUnifiedDB(db);
        }
  
        // Remover do sprint02
        const empresasSprint02 = loadSprint02Empresas();
        const sprint02Index = empresasSprint02.findIndex(e => e.id == id);
  
        if (sprint02Index !== -1) {
            empresasSprint02.splice(sprint02Index, 1);
            saveSprint02Empresas(empresasSprint02);
        }
  
        // Atualizar arquivos de status
        syncStatusFiles();
  
        res.json({ message: 'Empresa excluída com sucesso' });
    } catch (error) {
        console.error('Erro ao excluir empresa:', error);
        res.status(500).json({ error: 'Erro interno do servidor' });
    }
});

// Inicializar servidor
app.listen(PORT, '0.0.0.0', () => {
  const db = loadUnifiedDB();

  // Sincronizar arquivos por status na inicialização
  syncStatusFiles();

  console.log('Base de dados unificada carregada com sucesso!');
  console.log(`Servidor rodando na porta ${PORT}`);
  console.log('Acesse através do webview do Replit');
  console.log('Dados carregados:');
  console.log(`- Empresas: ${db.empresas.length}`);
  console.log(`- Produtos: ${db.produtos.length}`);
  console.log(`- Usuários: ${db.usuarios.length}`);
  console.log(`- Parceiros: ${db.parceiros.length}`);
  console.log(`- Avaliações: ${db.avaliacoes.length}`);
});