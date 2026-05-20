// JavaScript Principal Unificado - Digitalize

class DigitalizeApp {
    constructor() {
        this.apiBase = ''; 
        this.currentPage = 'home';
        this.data = {
            empresas: [],
            produtos: [],
            usuarios: [],
            parceiros: [],
            avaliacoes: []
        };
        
        // Cache para não ficar chamando a API de mapa toda hora pro mesmo endereço
        this.coordenadasCache = new Map();
        this.map = null; // Referência para o mapa Leaflet
        
        this.init();
    }

    async init() {
        await this.loadData();
        this.setupEventListeners();
        
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
        document.addEventListener('click', (e) => {
            const link = e.target.closest('[data-page]');
            if (link) {
                e.preventDefault();
                this.navigateTo(link.dataset.page);
            }
        });

        document.addEventListener('submit', (e) => {
            if (e.target.matches('#cadastro-form')) {
                e.preventDefault();
                this.handleCadastroSubmit(e);
            }
        });

        document.addEventListener('input', (e) => {
            if (e.target.matches('#search-input')) {
                this.handleSearch(e.target.value);
            }
        });

        window.addEventListener('hashchange', () => {
            const newPage = window.location.hash.substring(1) || 'home';
            this.navigateTo(newPage, false);
        });
    }

    navigateTo(page, updateHash = true) {
        if (page === 'login') { window.location.href = 'login.html'; return; }
        if (page === 'admin') { window.location.href = 'admin.html'; return; }

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
        
        cadastroLinks.forEach(l => l.style.display = 'none');
        minhaEmpresaLinks.forEach(l => l.style.display = 'none');
        adminLinks.forEach(l => l.style.display = 'none');

        if (user) {
            const isAdmin = user.tipo === 'admin' || user.tipo === 'administrador';
            if (isAdmin) {
                adminLinks.forEach(l => l.style.display = 'inline');
                cadastroLinks.forEach(l => l.style.display = 'inline');
            } else {
                this.verificarEmpresaParaNavegacao(user, cadastroLinks, minhaEmpresaLinks);
            }
        }
    }

    async verificarEmpresaParaNavegacao(user, cadastroLinks, minhaEmpresaLinks) {
        try {
            let temEmpresa = this.data.empresas.some(e => (e.responsavelEmail || e.responsavel_email) === user.email);
            if (!temEmpresa) {
                const response = await fetch(`/api/empresas/usuario/${encodeURIComponent(user.email)}`);
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
            cadastroLinks.forEach(l => l.style.display = 'inline');
        }
    }

    renderCurrentPage() {
        const content = document.getElementById('main-content');
        if (!content) return;
        content.innerHTML = '';

        switch (this.currentPage) {
            case 'home': this.renderHomePage(content); break;
            case 'empresas': this.renderEmpresasPage(content); break;
            case 'cadastro': this.renderCadastroPage(content); break;
            case 'mapa': this.renderMapaPage(content); break; // AQUI VAI O MAPA
            case 'minha-empresa': this.renderMinhaEmpresaPage(content); break;
            case 'avaliacoes': this.renderAvaliacoesPage(content); break;
            case 'favoritos': this.renderFavoritosPage(content); break;
            default: this.renderHomePage(content);
        }
    }

    // --- FUNÇÃO PARA CONVERTER ENDEREÇO EM COORDENADAS ---
    async obterCoordenadas(empresa) {
        let queryEndereco = "";

        if (typeof empresa.endereco === 'string') {
            queryEndereco = empresa.endereco;
        } else if (empresa.endereco && typeof empresa.endereco === 'object') {
            const cidade = empresa.endereco.cidade || empresa.cidade;
            const estado = empresa.endereco.estado || empresa.endereco.uf || empresa.estado;
            if (cidade) queryEndereco = `${cidade}, ${estado || ''}, Brazil`;
        }

        if (!queryEndereco || queryEndereco.length < 3) return null;

        if (this.coordenadasCache.has(queryEndereco)) {
            return this.coordenadasCache.get(queryEndereco);
        }

        try {
            // Usa OpenStreetMap (Nominatim) para geocoding
            const query = encodeURIComponent(queryEndereco);
            const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${query}&limit=1`);
            const data = await response.json();

            if (data && data.length > 0) {
                const coords = { lat: parseFloat(data[0].lat), lng: parseFloat(data[0].lon) };
                this.coordenadasCache.set(queryEndereco, coords);
                return coords;
            }
        } catch (error) {
            console.error('Erro ao geocodificar:', queryEndereco);
        }
        return null;
    }

    // --- RENDERIZADORES ---

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
        const empresasVisiveis = this.data.empresas.filter(empresa => 
            !empresa.status || empresa.status.toLowerCase().includes('aprovad') || empresa.status === 'ATIVA'
        );
        
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
                        ${this.renderEmpresasCards(empresasVisiveis)}
                    </div>
                </div>
            </div>
        `;
        this.setupFavoritoButtons();
    }

    renderEmpresasCards(empresas) {
        return empresas.map(empresa => {
            const nome = empresa.nomeFantasia || empresa.nome_fantasia || empresa.nome || 'Sem Nome';
            const razao = empresa.razaoSocial || empresa.razao_social || 'Não informado';
            const cnpj = empresa.cnpj || 'Não informado';
            const segmento = empresa.segmento || 'Não informado';
            
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

    // --- RENDERIZAÇÃO DO MAPA (CORRIGIDA) ---
    async renderMapaPage(container) {
        // Cria o container do mapa
        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <h2>Mapa de Empresas</h2>
                    <p class="mb-3">Visualize a localização das empresas cadastradas.</p>
                    <div id="map" style="height: 500px; width: 100%; border-radius: 8px; z-index: 1;"></div>
                </div>
            </div>
        `;

        // Espera o DOM atualizar
        setTimeout(async () => {
            // Inicializa o mapa (Centro no Brasil)
            if (this.map) {
                this.map.remove(); // Limpa mapa anterior se existir
            }
            
            // Certifique-se que o CSS do Leaflet está no index.html
            if (typeof L === 'undefined') {
                container.innerHTML += '<div class="alert alert-danger">Erro: Biblioteca Leaflet não encontrada.</div>';
                return;
            }

            this.map = L.map('map').setView([-15.793889, -47.882778], 4);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(this.map);

            // Adicionar marcadores
            const empresasAprovadas = this.data.empresas.filter(e => 
                !e.status || e.status.toLowerCase().includes('aprovad') || e.status === 'ATIVA'
            );

            let marcadoresAdicionados = 0;

            for (const empresa of empresasAprovadas) {
                const coords = await this.obterCoordenadas(empresa);
                if (coords) {
                    const nome = empresa.nomeFantasia || empresa.nome_fantasia || empresa.nome || 'Empresa';
                    const segmento = empresa.segmento || '';
                    
                    L.marker([coords.lat, coords.lng])
                        .addTo(this.map)
                        .bindPopup(`<b>${nome}</b><br>${segmento}<br>${empresa.endereco}`);
                    
                    marcadoresAdicionados++;
                }
            }

            if (marcadoresAdicionados === 0) {
                console.log("Nenhuma empresa com endereço válido encontrada para o mapa.");
            }
        }, 100);
    }

    renderCadastroPage(container) {
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

    async handleCadastroSubmit(event) {
        const form = event.target;
        const formData = new FormData(form);
        const data = {};
        formData.forEach((value, key) => data[key] = value);

        // FORMATAÇÃO DO ENDEREÇO COMO STRING ÚNICA (CORREÇÃO)
        const enderecoCompleto = `${data.rua}, ${data.cidade} - ${data.estado}. CEP: ${data.cep}`;
        
        const empresaParaEnviar = {
            nomeFantasia: data.nomeFantasia,
            razaoSocial: data.razaoSocial,
            cnpj: data.cnpj.replace(/\D/g, ''), 
            segmento: data.segmento,
            endereco: enderecoCompleto, 
            responsavelNome: data.responsavelNome,
            responsavelEmail: data.responsavelEmail,
            emailContato: data.responsavelEmail,
            responsavelTelefone: data.responsavelTelefone,
            observacoes: data.observacoes
        };

        try {
            const btn = form.querySelector('button[type="submit"]');
            btn.innerText = "Enviando...";
            btn.disabled = true;

            const response = await fetch('/api/empresas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(empresaParaEnviar)
            });

            if (response.ok) {
                alert('Empresa cadastrada com sucesso! Ela ficara pendente ate a aprovacao no painel administrativo.');
                form.reset();
                await this.loadData();
                window.location.href = 'minha-empresa.html';
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
            (e.nomeFantasia || e.nome_fantasia || '').toLowerCase().includes(termo) ||
            (e.segmento || '').toLowerCase().includes(termo)
        );
        const grid = document.getElementById('empresas-grid');
        if(grid) {
            grid.innerHTML = this.renderEmpresasCards(filtradas);
            this.setupFavoritoButtons();
        }
    }

    setupFormMasks() {
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

    renderMinhaEmpresaPage(container) { container.innerHTML = '<div class="container mt-5"><h2>Minha Empresa</h2><p>Detalhes aqui.</p></div>'; }
    renderAvaliacoesPage(container) { container.innerHTML = '<div class="container mt-5"><h2>Avaliações</h2><p>Lista de avaliações.</p></div>'; }
    renderFavoritosPage(container) { container.innerHTML = '<div class="container mt-5"><h2>Favoritos</h2><p>Seus favoritos.</p></div>'; }

    showMessage(msg, type) { alert(msg); }
    showError(msg) { alert(msg); }
}

document.addEventListener("DOMContentLoaded", () => {
    window.digitalizeApp = new DigitalizeApp();
});
