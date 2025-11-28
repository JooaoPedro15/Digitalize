// JavaScript Principal Unificado - Digitalize

class DigitalizeApp {
    constructor() {
        this.apiBase = ''; // Vazio pois estamos no mesmo domínio
        this.currentPage = 'home';
        this.data = {
            empresas: [],
            produtos: [],
            usuarios: [],
            parceiros: [],
            avaliacoes: []
        };
        
        // Cache para o mapa
        this.coordenadasCache = new Map();
        
        this.init();
    }

    async init() {
        await this.loadData();
        this.setupEventListeners();
        
        // Verificar hash inicial
        const hash = window.location.hash.substring(1);
        if (hash) {
            this.currentPage = hash;
        }
        
        this.renderCurrentPage();
        this.updateNavigation();
    }

    async fetchData(endpoint) {
        try {
            const response = await fetch(`${this.apiBase}${endpoint}`);
            if (!response.ok) {
                // Se der 404 ou erro, retorna array vazio para não quebrar a tela
                console.warn(`Aviso: ${endpoint} retornou ${response.status}`);
                return [];
            }
            return await response.json();
        } catch (error) {
            console.error(`Erro ao buscar ${endpoint}:`, error);
            return [];
        }
    }

    async loadData() {
        try {
            // Carrega tudo em paralelo
            const [empresas, produtos, usuarios, parceiros, avaliacoes] = await Promise.all([
                this.fetchData('/api/empresas'),
                this.fetchData('/api/produtos'),
                this.fetchData('/api/usuarios'),
                this.fetchData('/api/parceiros'),
                this.fetchData('/api/avaliacoes')
            ]);

            this.data = { empresas, produtos, usuarios, parceiros, avaliacoes };
            console.log('Dados carregados:', this.data);
        } catch (error) {
            console.error('Erro crítico ao carregar dados:', error);
            this.showError('Erro ao conectar com o servidor.');
        }
    }

    setupEventListeners() {
        // Navegação por cliques
        document.addEventListener('click', (e) => {
            // Verifica se clicou em um link de navegação ou num filho dele
            const link = e.target.closest('[data-page]');
            if (link) {
                e.preventDefault();
                this.navigateTo(link.dataset.page);
            }
        });

        // Formulário de Cadastro (Event Delegation)
        document.addEventListener('submit', (e) => {
            if (e.target.matches('#cadastro-form')) {
                e.preventDefault();
                this.handleCadastroSubmit(e);
            }
        });

        // Filtros e pesquisa
        document.addEventListener('input', (e) => {
            if (e.target.matches('#search-input')) {
                this.handleSearch(e.target.value);
            }
        });

        // Escutar mudanças no hash da URL (botão voltar do navegador)
        window.addEventListener('hashchange', () => {
            const newPage = window.location.hash.substring(1) || 'home';
            this.navigateTo(newPage, false); // false = não atualizar hash de novo
        });
    }

    navigateTo(page, updateHash = true) {
        // Verificar páginas externas
        if (page === 'login') {
            window.location.href = 'login.html';
            return;
        }
        if (page === 'admin') {
            window.location.href = 'admin.html';
            return;
        }

        // Verificar autenticação para páginas restritas
        if ((page === 'cadastro' || page === 'minha-empresa' || page === 'favoritos') && !this.isUserLoggedIn()) {
            alert('Você precisa estar logado para acessar esta página.');
            window.location.href = 'login.html';
            return;
        }

        this.currentPage = page;
        this.updateNavigation();
        this.renderCurrentPage();

        if (updateHash) {
            window.location.hash = page;
        }
        
        // Rolar para o topo
        window.scrollTo(0, 0);
    }

    isUserLoggedIn() {
        return localStorage.getItem('currentUser') !== null;
    }

    getCurrentUser() {
        const user = localStorage.getItem('currentUser');
        try {
            return user ? JSON.parse(user) : null;
        } catch (e) {
            return null;
        }
    }

    updateNavigation() {
        const user = this.getCurrentUser();
        const cadastroLinks = document.querySelectorAll('[data-page="cadastro"]');
        const minhaEmpresaLinks = document.querySelectorAll('[data-page="minha-empresa"]');
        const adminLinks = document.querySelectorAll('a[href="admin.html"]');
        
        // Esconder tudo por padrão
        cadastroLinks.forEach(l => l.style.display = 'none');
        minhaEmpresaLinks.forEach(l => l.style.display = 'none');
        adminLinks.forEach(l => l.style.display = 'none');

        if (user) {
            const isAdmin = user.tipo === 'admin' || user.tipo === 'administrador';
            
            if (isAdmin) {
                // Admin vê botão Admin e Cadastro
                adminLinks.forEach(l => l.style.display = 'inline');
                cadastroLinks.forEach(l => l.style.display = 'inline');
            } else {
                // Usuário comum: verificamos se já tem empresa
                this.verificarEmpresaParaNavegacao(user, cadastroLinks, minhaEmpresaLinks);
            }
        }
    }

    async verificarEmpresaParaNavegacao(user, cadastroLinks, minhaEmpresaLinks) {
        try {
            // Tenta achar empresa do usuário na lista local ou busca no server
            let temEmpresa = this.data.empresas.some(e => e.responsavelEmail === user.email);
            
            // Se não achou na lista local (pode estar desatualizada), busca no server
            if (!temEmpresa) {
                const response = await fetch(`/api/empresas/usuario/${user.email}`);
                if (response.ok) {
                    const empresas = await response.json();
                    temEmpresa = empresas.length > 0;
                }
            }

            if (temEmpresa) {
                minhaEmpresaLinks.forEach(l => l.style.display = 'inline');
            } else {
                cadastroLinks.forEach(l => l.style.display = 'inline');
            }
        } catch (error) {
            // Em caso de erro, libera cadastro
            cadastroLinks.forEach(l => l.style.display = 'inline');
        }
    }

    renderCurrentPage() {
        const content = document.getElementById('main-content');
        if (!content) return;

        // Limpa o conteúdo antes de renderizar
        content.innerHTML = '';

        switch (this.currentPage) {
            case 'home':
                this.renderHomePage(content);
                break;
            case 'empresas':
                this.renderEmpresasPage(content);
                break;
            case 'cadastro':
                this.renderCadastroPage(content);
                break;
            case 'mapa':
                this.renderMapaPage(content);
                break;
            case 'minha-empresa':
                this.renderMinhaEmpresaPage(content);
                break;
            case 'avaliacoes':
                this.renderAvaliacoesPage(content);
                break;
            case 'favoritos':
                this.renderFavoritosPage(content);
                break;
            default:
                this.renderHomePage(content);
        }
    }

    // --- RENDERIZADORES DE PÁGINA ---

    renderHomePage(container) {
        container.innerHTML = `
            <div class="section fade-in">
                <div class="hero-section text-center py-5 bg-light mb-4 rounded">
                    <div class="container">
                        <h1 class="display-4">Bem-vindo ao Digitalize</h1>
                        <p class="lead">Plataforma unificada para gestão de empresas e serviços digitais</p>
                    </div>
                </div>

                <div class="container">
                    <div class="cards-grid">
                        <div class="card p-4">
                            <h3>🏢 Empresas</h3>
                            <p>Visualize todas as empresas cadastradas na plataforma</p>
                            <button data-page="empresas" class="btn btn-primary w-100">Ver Empresas</button>
                        </div>

                        <div class="card p-4">
                            <h3>📝 Cadastrar</h3>
                            <p>Cadastre sua empresa na plataforma</p>
                            <button data-page="cadastro" class="btn btn-secondary w-100">Cadastrar Empresa</button>
                        </div>

                        <div class="card p-4">
                            <h3>🗺️ Mapa</h3>
                            <p>Visualize empresas no mapa interativo</p>
                            <button data-page="mapa" class="btn btn-success w-100">Ver Mapa</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    renderEmpresasPage(container) {
        // Filtrar empresas (aceita aprovada ou aprovado)
        // OBS: Se quiser testar vendo TODAS, remova o .filter temporariamente
        const empresasVisiveis = this.data.empresas.filter(empresa => 
            !empresa.status || empresa.status.toLowerCase().includes('aprovad') || empresa.status === 'ATIVA'
        );

        const listaHtml = this.renderEmpresasCards(empresasVisiveis);

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header mb-4">
                        <h2>Empresas Cadastradas</h2>
                        <p>Total de ${empresasVisiveis.length} empresas disponíveis</p>
                    </div>

                    <div class="form-group mb-4">
                        <input type="text" id="search-input" placeholder="Pesquisar por nome ou segmento..." class="form-control">
                    </div>

                    <div class="cards-grid" id="empresas-grid">
                        ${listaHtml}
                    </div>

                    ${empresasVisiveis.length === 0 ? 
                        '<div class="alert alert-warning">Nenhuma empresa encontrada.</div>' : ''}
                </div>
            </div>
        `;

        this.setupFavoritoButtons();
    }

    renderEmpresasCards(empresas) {
        return empresas.map(empresa => {
            // Normalização de campos (Backend Java vs JSON)
            const nome = empresa.nomeFantasia || empresa.nome_fantasia || empresa.nome || 'Sem Nome';
            const razao = empresa.razaoSocial || empresa.razao_social || 'Não informado';
            const cnpj = empresa.cnpj || 'Não informado';
            const segmento = empresa.segmento || 'Não informado';
            
            // O endereço agora deve vir como STRING do Java, mas tratamos caso venha objeto
            let enderecoStr = 'Não informado';
            if (typeof empresa.endereco === 'string') {
                enderecoStr = empresa.endereco;
            } else if (empresa.endereco && typeof empresa.endereco === 'object') {
                enderecoStr = empresa.endereco.cidade || 'Cidade não informada';
            }

            const status = empresa.status || 'pendente';

            return `
            <div class="card empresa-card">
                <div class="empresa-header d-flex justify-content-between align-items-start mb-3">
                    <div>
                        <h3 class="h5 mb-1">${nome}</h3>
                        <span class="badge bg-secondary">${segmento}</span>
                    </div>
                    <span class="badge ${status === 'aprovada' ? 'bg-success' : 'bg-warning'}">${status}</span>
                </div>
                <div class="empresa-info">
                    <p class="mb-1"><strong>Razão Social:</strong> ${razao}</p>
                    <p class="mb-1"><strong>CNPJ:</strong> ${cnpj}</p>
                    <p class="mb-1"><strong>Endereço:</strong> ${enderecoStr}</p>
                    <p class="mb-3"><strong>Email:</strong> ${empresa.email_contato || empresa.emailContato || 'Não informado'}</p>
                    
                    <button class="btn btn-outline-danger btn-sm favorito-btn w-100" data-empresa-id="${empresa.id || empresa.cnpj}">
                        ❤️ Favoritar
                    </button>
                </div>
            </div>
            `;
        }).join('');
    }

    renderCadastroPage(container) {
        // O formulário de cadastro usa a mesma estrutura que você já tinha
        // Apenas recriei a estrutura HTML limpa aqui
        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header mb-4">
                        <h2>Cadastrar Empresa</h2>
                    </div>

                    <div id="cadastro-message" class="alert d-none"></div>

                    <form id="cadastro-form" class="card p-4">
                        <h4 class="mb-3">Dados da Empresa</h4>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label class="form-label">Nome Fantasia *</label>
                                <input type="text" name="nomeFantasia" class="form-control" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Razão Social *</label>
                                <input type="text" name="razaoSocial" class="form-control" required>
                            </div>
                        </div>

                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label class="form-label">CNPJ *</label>
                                <input type="text" id="cnpj" name="cnpj" class="form-control" required placeholder="00.000.000/0000-00">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Segmento *</label>
                                <select name="segmento" class="form-select" required>
                                    <option value="">Selecione...</option>
                                    <option value="Tecnologia">Tecnologia</option>
                                    <option value="Serviços">Serviços</option>
                                    <option value="Comércio">Comércio</option>
                                    <option value="Saúde">Saúde</option>
                                    <option value="Educação">Educação</option>
                                </select>
                            </div>
                        </div>

                        <h4 class="mb-3 mt-4">Endereço</h4>
                        <div class="row mb-3">
                            <div class="col-md-3">
                                <label class="form-label">CEP *</label>
                                <input type="text" id="cep" name="cep" class="form-control" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Rua *</label>
                                <input type="text" name="rua" class="form-control" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Cidade *</label>
                                <input type="text" name="cidade" class="form-control" required>
                            </div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-md-4">
                                <label class="form-label">Estado *</label>
                                <select name="estado" class="form-select" required>
                                    <option value="">UF</option>
                                    <option value="SP">SP</option>
                                    <option value="MG">MG</option>
                                    <option value="RJ">RJ</option>
                                    <option value="BA">BA</option>
                                    </select>
                            </div>
                        </div>

                        <h4 class="mb-3 mt-4">Contato</h4>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label class="form-label">Nome Responsável *</label>
                                <input type="text" name="responsavelNome" class="form-control" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Email Responsável *</label>
                                <input type="email" name="responsavelEmail" class="form-control" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Telefone *</label>
                            <input type="tel" id="responsavelTelefone" name="responsavelTelefone" class="form-control" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Observações (IA)</label>
                            <textarea name="observacoes" class="form-control" rows="3" placeholder="Gere uma descrição com IA..."></textarea>
                        </div>

                        <div class="mt-4">
                            <button type="submit" class="btn btn-primary">Cadastrar Empresa</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        
        this.setupFormMasks();
    }

    // --- LOGICA DE ENVIO DO CADASTRO (A CORREÇÃO PRINCIPAL) ---
    async handleCadastroSubmit(event) {
        const form = event.target;
        const formData = new FormData(form);
        const data = {};

        // Converter FormData para objeto
        formData.forEach((value, key) => data[key] = value);

        // CORREÇÃO CRÍTICA: Montar endereço como STRING única
        // O Java espera String no campo endereco, não JSON.
        const enderecoCompleto = `${data.rua}, ${data.cidade} - ${data.estado}. CEP: ${data.cep}`;
        
        // Objeto final para enviar
        const empresaParaEnviar = {
            nomeFantasia: data.nomeFantasia,
            razaoSocial: data.razaoSocial,
            cnpj: data.cnpj.replace(/\D/g, ''), // Remove pontos e traços para o banco
            segmento: data.segmento,
            endereco: enderecoCompleto, // Envia a string formatada
            responsavelNome: data.responsavelNome,
            responsavelEmail: data.responsavelEmail,
            emailContato: data.responsavelEmail, // Reutiliza email
            responsavelTelefone: data.responsavelTelefone,
            observacoes: data.observacoes
        };

        try {
            const btn = form.querySelector('button[type="submit"]');
            const txtOriginal = btn.innerText;
            btn.innerText = "Enviando...";
            btn.disabled = true;

            const response = await fetch('/admin/cadastro', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(empresaParaEnviar)
            });

            if (response.ok) {
                alert('Empresa cadastrada e aprovada com sucesso!');
                form.reset();
                await this.loadData(); // Recarrega para aparecer na lista
                this.navigateTo('empresas');
            } else {
                const err = await response.json();
                alert('Erro: ' + (err.mensagem || 'Falha ao salvar'));
            }
        } catch (error) {
            console.error(error);
            alert('Erro de conexão ao cadastrar.');
        } finally {
            const btn = form.querySelector('button[type="submit"]');
            btn.innerText = "Cadastrar Empresa";
            btn.disabled = false;
        }
    }

    handleSearch(query) {
        const termo = query.toLowerCase();
        const filtradas = this.data.empresas.filter(e => 
            (e.nomeFantasia || '').toLowerCase().includes(termo) ||
            (e.segmento || '').toLowerCase().includes(termo)
        );
        
        const grid = document.getElementById('empresas-grid');
        if(grid) {
            grid.innerHTML = this.renderEmpresasCards(filtradas);
            this.setupFavoritoButtons();
        }
    }

    // --- MÉTODOS AUXILIARES ---

    setupFormMasks() {
        // Exemplo simples de máscara
        const cnpj = document.getElementById('cnpj');
        if(cnpj) {
            cnpj.addEventListener('input', e => {
                let v = e.target.value.replace(/\D/g, '');
                if (v.length > 14) v = v.slice(0, 14);
                e.target.value = v.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2}).*/, '$1.$2.$3/$4-$5');
            });
        }
    }

    setupFavoritoButtons() {
        document.querySelectorAll('.favorito-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                alert('Adicionado aos favoritos (Simulação)');
                btn.classList.toggle('btn-danger');
                btn.classList.toggle('btn-outline-danger');
            });
        });
    }

    renderMapaPage(container) {
        container.innerHTML = '<div class="container mt-5"><h2>Mapa</h2><p>Funcionalidade de mapa (Leaflet) deve ser carregada aqui.</p></div>';
        // Aqui você pode reativar seu código do Leaflet se quiser
    }

    renderMinhaEmpresaPage(container) {
        container.innerHTML = '<div class="container mt-5"><h2>Minha Empresa</h2><p>Detalhes da sua empresa apareceriam aqui.</p></div>';
    }

    renderAvaliacoesPage(container) {
        container.innerHTML = '<div class="container mt-5"><h2>Avaliações</h2><p>Lista de avaliações aqui.</p></div>';
    }

    renderFavoritosPage(container) {
        container.innerHTML = '<div class="container mt-5"><h2>Favoritos</h2><p>Seus favoritos aqui.</p></div>';
    }

    showMessage(msg, type) {
        const box = document.getElementById('cadastro-message');
        if(box) {
            box.innerText = msg;
            box.className = `alert alert-${type === 'error' ? 'danger' : 'success'} d-block`;
            setTimeout(() => box.className = 'alert d-none', 5000);
        } else {
            alert(msg);
        }
    }

    showError(msg) {
        this.showMessage(msg, 'error');
    }
}

// Inicialização
document.addEventListener("DOMContentLoaded", () => {
    console.log("Iniciando DigitalizeApp Unificado...");
    window.digitalizeApp = new DigitalizeApp();
});