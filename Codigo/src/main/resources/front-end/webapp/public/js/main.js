// JavaScript Principal Unificado

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
        this.init();
    }

    async init() {
        await this.loadData();
        this.setupEventListeners();
        this.renderCurrentPage();
        this.updateNavigation();
    }

    async fetchData(endpoint) {
        const response = await fetch(this.apiBase + endpoint);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
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
            console.error('Erro ao carregar dados:', error);
            this.showError('Erro ao carregar dados do servidor');
        }
    }

    showError(message) {
        console.error(message);
        // You can implement UI error display here
        alert(message);
    }

    setupEventListeners() {
        // Add event listeners for navigation and forms
        console.log('Setting up event listeners');
    }

    renderCurrentPage() {
        // Render the current page content
        console.log('Rendering page:', this.currentPage);
    }

    updateNavigation() {
        // Update navigation state
        console.log('Updating navigation');
    }

    async fetchData(endpoint) {
        try {
            const response = await fetch(`${this.apiBase}${endpoint}`);
            if (!response.ok) {
                throw new Error(`Erro ao buscar ${endpoint}: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Erro ao buscar ${endpoint}:`, error);
            return [];
        }
    }

    setupEventListeners() {
        // Navegação
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-page]')) {
                e.preventDefault();
                this.navigateTo(e.target.dataset.page);
            }
        });

        // Formulários
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

        // Verificar hash da URL na inicialização
        const hash = window.location.hash.substring(1);
        if (hash) {
            this.currentPage = hash;
        }

        // Escutar mudanças no hash
        window.addEventListener('hashchange', () => {
            const newPage = window.location.hash.substring(1) || 'home';
            this.currentPage = newPage;
            this.renderCurrentPage();
            this.updateNavigation();
        });
    }

    navigateTo(page) {
        // Verificar se é uma página externa (login ou admin)
        if (page === 'login') {
            window.location.href = 'login.html';
            return;
        }
        if (page === 'admin') {
            window.location.href = 'admin.html';
            return;
        }

        // Verificar autenticação para páginas restritas
        if (page === 'cadastro' && !this.isUserLoggedIn()) {
            alert('Você precisa estar logado para acessar esta página.');
            this.navigateTo('login');
            return;
        }

        this.currentPage = page;
        this.updateNavigation();
        this.renderCurrentPage();

        // Atualizar URL sem recarregar a página
        window.location.hash = page;
    }

    isUserLoggedIn() {
        return localStorage.getItem('currentUser') !== null;
    }

    getCurrentUser() {
        const user = localStorage.getItem('currentUser');
        return user ? JSON.parse(user) : null;
    }

    updateNavigation() {
        // Controlar visibilidade de links baseado na autenticação
        const user = this.getCurrentUser();
        const cadastroLinks = document.querySelectorAll('[data-page="cadastro"]');
        const minhaEmpresaLinks = document.querySelectorAll('[data-page="minha-empresa"]');
        const favoritosLinks = document.querySelectorAll('[data-page="favoritos"]');
        const adminLinks = document.querySelectorAll('a[href="admin.html"]');

        // Verificar se usuário tem empresa cadastrada para controlar visibilidade das abas
        if (user && user.tipo !== 'admin' && user.tipo !== 'administrador') {
            this.verificarEmpresaParaNavegacao(user, cadastroLinks, minhaEmpresaLinks);
        } else if (user && (user.tipo === 'admin' || user.tipo === 'administrador')) {
            // Para admins, sempre mostrar cadastro e esconder minha empresa
            cadastroLinks.forEach(link => {
                link.style.display = 'inline';
            });
            minhaEmpresaLinks.forEach(link => {
                link.style.display = 'none';
            });
        } else {
            // Para usuários não logados, esconder ambas as abas
            cadastroLinks.forEach(link => {
                link.style.display = 'none';
            });
            minhaEmpresaLinks.forEach(link => {
                link.style.display = 'none';
            });
        }

        // Controlar visibilidade do link "Admin" - mostrar apenas para administradores
        adminLinks.forEach(link => {
            if (user && (user.tipo === 'admin' || user.tipo === 'administrador')) {
                link.style.display = 'inline';
            } else {
                link.style.display = 'none';
            }
        });
    }

    async verificarEmpresaParaNavegacao(user, cadastroLinks, minhaEmpresaLinks) {
        try {
            const response = await fetch(`/api/empresas/usuario/${user.email}`);

            if (response.ok) {
                const empresas = await response.json();

                if (empresas.length > 0) {
                    // Usuário tem empresa - mostrar "Minha Empresa" e esconder "Cadastrar"
                    cadastroLinks.forEach(link => {
                        link.style.display = 'none';
                    });
                    minhaEmpresaLinks.forEach(link => {
                        link.style.display = 'inline';
                    });
                } else {
                    // Usuário não tem empresa - mostrar "Cadastrar" e esconder "Minha Empresa"
                    cadastroLinks.forEach(link => {
                        link.style.display = 'inline';
                    });
                    minhaEmpresaLinks.forEach(link => {
                        link.style.display = 'none';
                    });
                }
            } else {
                // Se der erro na consulta, assumir que não tem empresa
                cadastroLinks.forEach(link => {
                    link.style.display = 'inline';
                });
                minhaEmpresaLinks.forEach(link => {
                    link.style.display = 'none';
                });
            }
        } catch (error) {
            console.error('Erro ao verificar empresa para navegação:', error);
            // Em caso de erro, mostrar cadastro por segurança
            cadastroLinks.forEach(link => {
                link.style.display = 'inline';
            });
            minhaEmpresaLinks.forEach(link => {
                link.style.display = 'none';
            });
        }
    }

    renderCurrentPage() {
        const content = document.getElementById('main-content');
        if (!content) return;

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

    renderHomePage(container) {
        container.innerHTML = `
            <div class="section fade-in">
                <div class="hero-section">
                    <div class="container">
                        <h1>Bem-vindo ao Digitalize</h1>
                        <p>Plataforma unificada para gestão de empresas e serviços digitais</p>


                    </div>
                </div>

                <div class="container">
                    <div class="cards-grid mt-2">
                        <div class="card">
                            <h3>🏢 Empresas</h3>
                            <p>Visualize todas as empresas cadastradas na plataforma</p>
                            <a href="#" data-page="empresas" class="btn btn-primary">Ver Empresas</a>
                        </div>

                        <div class="card">
                            <h3>📝 Cadastrar</h3>
                            <p>Cadastre sua empresa na plataforma</p>
                            <a href="#" data-page="cadastro" class="btn btn-secondary">Cadastrar Empresa</a>
                        </div>

                        <div class="card">
                            <h3>🗺️ Mapa</h3>
                            <p>Visualize empresas no mapa interativo</p>
                            <a href="#" data-page="mapa" class="btn btn-success">Ver Mapa</a>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    renderEmpresasPage(container) {
        // Filtrar apenas empresas aprovadas
        const empresasAprovadas = this.data.empresas.filter(empresa => 
            empresa.status === 'aprovada' || empresa.status === 'aprovado'
        );

        const empresasHtml = empresasAprovadas.map(empresa => `
            <div class="card empresa-card">
                <div class="empresa-header">
                    <h3>${empresa.nomeFantasia || empresa.nome}</h3>
                    <span class="status-badge status-${empresa.status || 'pendente'}">${empresa.status || 'Pendente'}</span>
                </div>
                <div class="empresa-info">
                    <p><strong>Razão Social:</strong> ${empresa.razaoSocial || 'Não informado'}</p>
                    <p><strong>CNPJ:</strong> ${empresa.cnpj || 'Não informado'}</p>
                    <p><strong>Segmento:</strong> ${empresa.segmento || empresa.categoria || 'Não informado'}</p>
                    <p><strong>Cidade:</strong> ${empresa.endereco?.cidade || empresa.cidade || 'Não informado'}</p>
                    <p><strong>Responsável:</strong> ${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}</p>
                    <div class="empresa-actions">
                        <button class="favorito-btn" data-empresa-id="${empresa.id}">
                            ❤️
                        </button>
                    </div>
                </div>
            </div>
        `).join('');

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Empresas Cadastradas</h2>
                        <p>Total de ${empresasAprovadas.length} empresas aprovadas</p>
                    </div>

                    <div class="form-group">
                        <input type="text" id="search-input" placeholder="Pesquisar empresas..." class="form-control">
                    </div>

                    <div class="cards-grid" id="empresas-grid">
                        ${empresasHtml}
                    </div>

                    ${empresasAprovadas.length === 0 ? 
                        '<div class="empty-state"><p>Nenhuma empresa aprovada encontrada.</p></div>' : 
                        ''
                    }
                </div>
            </div>
        `;

        // Configurar botões de favoritos após renderizar
        setTimeout(() => {
            this.setupFavoritoButtons();
        }, 100);
    }

    renderCadastroPage(container) {
        // Verificar se usuário está logado
        const user = this.getCurrentUser();
        if (!user) {
            container.innerHTML = `
                <div class="section fade-in">
                    <div class="container text-center">
                        <h2>Acesso Restrito</h2>
                        <p>Você precisa estar logado para cadastrar empresas.</p>
                        <a href="login.html" class="btn btn-primary">Fazer Login</a>
                    </div>
                </div>
            `;
            return;
        }

        // Renderizar o formulário diretamente (a verificação de empresa existente agora é feita na navegação)

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Cadastrar Empresa</h2>
                        <p>Preencha os dados da sua empresa para cadastro na plataforma</p>
                    </div>

                    <div id="cadastro-message" class="message hidden"></div>

                    <form id="cadastro-form" class="form-container">
                        <div class="form-section">
                            <h3>Dados da Empresa</h3>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="nomeFantasia">Nome Fantasia *</label>
                                    <input type="text" id="nomeFantasia" name="nomeFantasia" required>
                                </div>

                                <div class="form-group">
                                    <label for="razaoSocial">Razão Social *</label>
                                    <input type="text" id="razaoSocial" name="razaoSocial" required>
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="cnpj">CNPJ *</label>
                                    <input type="text" id="cnpj" name="cnpj" required placeholder="00.000.000/0000-00">
                                </div>

                                <div class="form-group">
                                    <label for="segmento">Segmento *</label>
                                    <select id="segmento" name="segmento" required>
                                        <option value="">Selecione um segmento</option>
                                        <option value="Tecnologia">Tecnologia</option>
                                        <option value="Serviços">Serviços</option>
                                        <option value="Comércio">Comércio</option>
                                        <option value="Indústria">Indústria</option>
                                        <option value="Saúde">Saúde</option>
                                        <option value="Educação">Educação</option>
                                        <option value="Outros">Outros</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Endereço</h3>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="cep">CEP *</label>
                                    <input type="text" id="cep" name="cep" required placeholder="00000-000">
                                </div>

                                <div class="form-group">
                                    <label for="rua">Rua/Logradouro *</label>
                                    <input type="text" id="rua" name="rua" required>
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="cidade">Cidade *</label>
                                    <input type="text" id="cidade" name="cidade" required>
                                </div>

                                <div class="form-group">
                                    <label for="estado">Estado *</label>
                                    <select id="estado" name="estado" required>
                                        <option value="">Selecione um estado</option>
                                        <option value="AC">Acre</option>
                                        <option value="AL">Alagoas</option>
                                        <option value="AP">Amapá</option>
                                        <option value="AM">Amazonas</option>
                                        <option value="BA">Bahia</option>
                                        <option value="CE">Ceará</option>
                                        <option value="DF">Distrito Federal</option>
                                        <option value="ES">Espírito Santo</option>
                                        <option value="GO">Goiás</option>
                                        <option value="MA">Maranhão</option>
                                        <option value="MT">Mato Grosso</option>
                                        <option value="MS">Mato Grosso do Sul</option>
                                        <option value="MG">Minas Gerais</option>
                                        <option value="PA">Pará</option>
                                        <option value="PB">Paraíba</option>
                                        <option value="PR">Paraná</option>
                                        <option value="PE">Pernambuco</option>
                                        <option value="PI">Piauí</option>
                                        <option value="RJ">Rio de Janeiro</option>
                                        <option value="RN">Rio Grande do Norte</option>
                                        <option value="RS">Rio Grande do Sul</option>
                                        <option value="RO">Rondônia</option>
                                        <option value="RR">Roraima</option>
                                        <option value="SC">Santa Catarina</option>
                                        <option value="SP">São Paulo</option>
                                        <option value="SE">Sergipe</option>
                                        <option value="TO">Tocantins</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Responsável</h3>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="responsavelNome">Nome do Responsável *</label>
                                    <input type="text" id="responsavelNome" name="responsavelNome" required>
                                </div>

                                <div class="form-group">
                                    <label for="responsavelCargo">Cargo</label>
                                    <input type="text" id="responsavelCargo" name="responsavelCargo">
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="responsavelEmail">Email *</label>
                                    <input type="email" id="responsavelEmail" name="responsavelEmail" required>
                                </div>

                                <div class="form-group">
                                    <label for="responsavelTelefone">Telefone *</label>
                                    <input type="tel" id="responsavelTelefone" name="responsavelTelefone" required placeholder="(00) 00000-0000">
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Serviços de Interesse</h3>
                            <div class="checkbox-group">
                                <label><input type="checkbox" name="servicosInteresse" value="site"> Desenvolvimento de Site</label>
                                <label><input type="checkbox" name="servicosInteresse" value="marketing"> Marketing Digital</label>
                                <label><input type="checkbox" name="servicosInteresse" value="seo"> SEO</label>
                                <label><input type="checkbox" name="servicosInteresse" value="redes"> Redes Sociais</label>
                                <label><input type="checkbox" name="servicosInteresse" value="sistema"> Sistema Web</label>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Observações</h3>
                            <div class="form-group">
                                <textarea id="observacoes" name="observacoes" rows="4" placeholder="Observações adicionais..."></textarea>
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">Cadastrar Empresa</button>
                            <button type="reset" class="btn btn-secondary">Limpar Formulário</button>
                        </div>
                    </form>
                </div>
            </div>
        `;

        this.setupFormMasks();
    }

    renderFormularioCadastro(container) {
        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Cadastrar Empresa</h2>
                        <p>Preencha os dados da sua empresa para cadastro na plataforma</p>
                    </div>

                    <div id="cadastro-message" class="message hidden"></div>

                    <form id="cadastro-form" class="form-container">
                        <div class="form-section">
                            <h3>Dados da Empresa</h3>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="nomeFantasia">Nome Fantasia *</label>
                                    <input type="text" id="nomeFantasia" name="nomeFantasia" required>
                                </div>

                                <div class="form-group">
                                    <label for="razaoSocial">Razão Social *</label>
                                    <input type="text" id="razaoSocial" name="razaoSocial" required>
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="cnpj">CNPJ *</label>
                                    <input type="text" id="cnpj" name="cnpj" required placeholder="00.000.000/0000-00">
                                </div>

                                <div class="form-group">
                                    <label for="segmento">Segmento *</label>
                                    <select id="segmento" name="segmento" required>
                                        <option value="">Selecione um segmento</option>
                                        <option value="Tecnologia">Tecnologia</option>
                                        <option value="Serviços">Serviços</option>
                                        <option value="Comércio">Comércio</option>
                                        <option value="Indústria">Indústria</option>
                                        <option value="Saúde">Saúde</option>
                                        <option value="Educação">Educação</option>
                                        <option value="Outros">Outros</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Endereço</h3>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="cep">CEP *</label>
                                    <input type="text" id="cep" name="cep" required placeholder="00000-000">
                                </div>

                                <div class="form-group">
                                    <label for="rua">Rua/Logradouro *</label>
                                    <input type="text" id="rua" name="rua" required>
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="cidade">Cidade *</label>
                                    <input type="text" id="cidade" name="cidade" required>
                                </div>

                                <div class="form-group">
                                    <label for="estado">Estado *</label>
                                    <select id="estado" name="estado" required>
                                        <option value="">Selecione um estado</option>
                                        <option value="AC">Acre</option>
                                        <option value="AL">Alagoas</option>
                                        <option value="AP">Amapá</option>
                                        <option value="AM">Amazonas</option>
                                        <option value="BA">Bahia</option>
                                        <option value="CE">Ceará</option>
                                        <option value="DF">Distrito Federal</option>
                                        <option value="ES">Espírito Santo</option>
                                        <option value="GO">Goiás</option>
                                        <option value="MA">Maranhão</option>
                                        <option value="MT">Mato Grosso</option>
                                        <option value="MS">Mato Grosso do Sul</option>
                                        <option value="MG">Minas Gerais</option>
                                        <option value="PA">Pará</option>
                                        <option value="PB">Paraíba</option>
                                        <option value="PR">Paraná</option>
                                        <option value="PE">Pernambuco</option>
                                        <option value="PI">Piauí</option>
                                        <option value="RJ">Rio de Janeiro</option>
                                        <option value="RN">Rio Grande do Norte</option>
                                        <option value="RS">Rio Grande do Sul</option>
                                        <option value="RO">Rondônia</option>
                                        <option value="RR">Roraima</option>
                                        <option value="SC">Santa Catarina</option>
                                        <option value="SP">São Paulo</option>
                                        <option value="SE">Sergipe</option>
                                        <option value="TO">Tocantins</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Responsável</h3>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="responsavelNome">Nome do Responsável *</label>
                                    <input type="text" id="responsavelNome" name="responsavelNome" required>
                                </div>

                                <div class="form-group">
                                    <label for="responsavelCargo">Cargo</label>
                                    <input type="text" id="responsavelCargo" name="responsavelCargo">
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="responsavelEmail">Email *</label>
                                    <input type="email" id="responsavelEmail" name="responsavelEmail" required>
                                </div>

                                <div class="form-group">
                                    <label for="responsavelTelefone">Telefone *</label>
                                    <input type="tel" id="responsavelTelefone" name="responsavelTelefone" required placeholder="(00) 00000-0000">
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Serviços de Interesse</h3>
                            <div class="checkbox-group">
                                <label><input type="checkbox" name="servicosInteresse" value="site"> Desenvolvimento de Site</label>
                                <label><input type="checkbox" name="servicosInteresse" value="marketing"> Marketing Digital</label>
                                <label><input type="checkbox" name="servicosInteresse" value="seo"> SEO</label>
                                <label><input type="checkbox" name="servicosInteresse" value="redes"> Redes Sociais</label>
                                <label><input type="checkbox" name="servicosInteresse" value="sistema"> Sistema Web</label>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Observações</h3>
                            <div class="form-group">
                                <textarea id="observacoes" name="observacoes" rows="4" placeholder="Observações adicionais..."></textarea>
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">Cadastrar Empresa</button>
                            <button type="reset" class="btn btn-secondary">Limpar Formulário</button>
                        </div>
                    </form>
                </div>
            </div>
        `;

        this.setupFormMasks();
    }

    renderMinhaEmpresaPage(container) {
        // Verificar se usuário está logado e não é admin
        const user = this.getCurrentUser();
        if (!user) {
            container.innerHTML = `
                <div class="section fade-in">
                    <div class="container text-center">
                        <h2>Acesso Restrito</h2>
                        <p>Você precisa estar logado para ver sua empresa.</p>
                        <a href="login.html" class="btn btn-primary">Fazer Login</a>
                    </div>
                </div>
            `;
            return;
        }

        if (user.tipo === 'admin' || user.tipo === 'administrador') {
            container.innerHTML = `
                <div class="section fade-in">
                    <div class="container text-center">
                        <h2>Acesso Negado</h2>
                        <p>Administradores não possuem empresas cadastradas.</p>
                        <a href="#" data-page="home" class="btn btn-primary">Voltar ao Início</a>
                    </div>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Minha Empresa</h2>
                        <p>Dados da empresa cadastrada por você</p>
                    </div>

                    <div id="empresa-loading" class="text-center">
                        <p>Carregando dados da empresa...</p>
                    </div>

                    <div id="empresa-dados" class="hidden">
                        <!-- Os dados serão carregados aqui via AJAX -->
                    </div>
                </div>
            </div>
        `;

        this.loadEmpresaDoUsuario();
    }

    async loadEmpresaDoUsuario() {
        try {
            const user = this.getCurrentUser();
            if (!user) return;

            const response = await fetch(`/api/empresas/usuario/${user.email}`);
            const loadingDiv = document.getElementById('empresa-loading');
            const dadosDiv = document.getElementById('empresa-dados');

            if (response.ok) {
                const empresas = await response.json();

                if (empresas.length === 0) {
                    loadingDiv.innerHTML = `
                        <div class="text-center">
                            <h3>Nenhuma empresa cadastrada</h3>
                            <p>Você ainda não possui uma empresa cadastrada.</p>
                            <a href="#cadastro" data-page="cadastro" class="btn btn-primary">Cadastrar Minha Empresa</a>
                        </div>
                    `;
                    return;
                }

                // Pegar a empresa do usuário (deveria ser apenas uma)
                const empresa = empresas[0];

                dadosDiv.innerHTML = `
                    <div class="empresa-detalhes">
                        <div class="form-section">
                            <h3>Dados da Empresa</h3>
                            <div class="info-grid">
                                <div class="info-item">
                                    <label>Nome Fantasia:</label>
                                    <span>${empresa.nomeFantasia || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Razão Social:</label>
                                    <span>${empresa.razaoSocial || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>CNPJ:</label>
                                    <span>${empresa.cnpj || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Segmento:</label>
                                    <span>${empresa.segmento || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Status:</label>
                                    <span class="status-badge status-${empresa.status || 'pendente'}">${empresa.status || 'Pendente'}</span>
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Endereço</h3>
                            <div class="info-grid">
                                <div class="info-item">
                                    <label>CEP:</label>
                                    <span>${empresa.endereco?.cep || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Rua:</label>
                                    <span>${empresa.endereco?.rua || empresa.endereco?.logradouro || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Cidade:</label>
                                    <span>${empresa.endereco?.cidade || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Estado:</label>
                                    <span>${empresa.endereco?.estado || empresa.endereco?.uf || 'Não informado'}</span>
                                </div>
                            </div>
                        </div>

                        <div class="form-section">
                            <h3>Responsável</h3>
                            <div class="info-grid">
                                <div class="info-item">
                                    <label>Nome:</label>
                                    <span>${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Cargo:</label>
                                    <span>${empresa.responsavelCargo || empresa.responsavel?.cargo || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Email:</label>
                                    <span>${empresa.responsavelEmail || empresa.responsavel?.email || 'Não informado'}</span>
                                </div>
                                <div class="info-item">
                                    <label>Telefone:</label>
                                    <span>${empresa.responsavelTelefone || empresa.responsavel?.telefone || 'Não informado'}</span>
                                </div>
                            </div>
                        </div>

                        ${empresa.servicosInteresse && empresa.servicosInteresse.length > 0 ? `
                            <div class="form-section">
                                <h3>Serviços de Interesse</h3>
                                <div class="servicos-lista">
                                    ${empresa.servicosInteresse.map(servico => `<span class="badge">${servico}</span>`).join('')}
                                </div>
                            </div>
                        ` : ''}

                        ${empresa.observacoes ? `
                            <div class="form-section">
                                <h3>Observações</h3>
                                <p>${empresa.observacoes}</p>
                            </div>
                        ` : ''}

                        <div class="form-actions">
                            <button type="button" id="btn-editar" class="btn btn-primary me-2">
                                📝 Editar Informações
                            </button>
                            <button type="button" id="btn-excluir" class="btn btn-danger">
                                🗑️ Excluir Empresa
                            </button>
                            <p class="text-muted mt-3">Cada usuário pode cadastrar apenas uma empresa.</p>
                        </div>
                    </div>
                `;

                // Adicionar modais para edição e confirmação
                dadosDiv.innerHTML += `
                    <!-- Modal de confirmação de exclusão -->
                    <div class="modal" id="modalConfirmacao" style="display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5);">
                        <div class="modal-content" style="background-color: #fefefe; margin: 15% auto; padding: 20px; border: 1px solid #888; width: 400px; border-radius: 8px;">
                            <h3>Confirmar Exclusão</h3>
                            <p>Tem certeza que deseja excluir permanentemente os dados da sua empresa?</p>
                            <p style="color: red;"><strong>Esta ação não pode ser desfeita!</strong></p>
                            <div style="text-align: right; margin-top: 20px;">
                                <button type="button" class="btn btn-secondary me-2" onclick="document.getElementById('modalConfirmacao').style.display='none'">Cancelar</button>
                                <button type="button" id="confirmar-exclusao" class="btn btn-danger">Confirmar Exclusão</button>
                            </div>
                        </div>
                    </div>

                    <!-- Modal de edição -->
                    <div class="modal" id="modalEdicao" style="display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); overflow-y: auto;">
                        <div class="modal-content" style="background-color: #fefefe; margin: 5% auto; padding: 20px; border: 1px solid #888; width: 80%; max-width: 800px; border-radius: 8px;">
                            <h3>Editar Informações da Empresa</h3>
                            <form id="form-edicao">
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 15px;">
                                    <div>
                                        <label class="form-label">Razão Social *</label>
                                        <input type="text" class="form-control" id="edit-razaoSocial" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Nome Fantasia *</label>
                                        <input type="text" class="form-control" id="edit-nomeFantasia" required>
                                    </div>
                                    <div>
                                        <label class="form-label">CNPJ *</label>
                                        <input type="text" class="form-control" id="edit-cnpj" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Segmento *</label>
                                        <select class="form-control" id="edit-segmento" required>
                                            <option value="">Selecione um segmento</option>
                                            <option value="Tecnologia">Tecnologia</option>
                                            <option value="Serviços">Serviços</option>
                                            <option value="Comércio">Comércio</option>
                                            <option value="Indústria">Indústria</option>
                                            <option value="Saúde">Saúde</option>
                                            <option value="Educação">Educação</option>
                                            <option value="Outros">Outros</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label class="form-label">CEP *</label>
                                        <input type="text" class="form-control" id="edit-cep" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Rua *</label>
                                        <input type="text" class="form-control" id="edit-rua" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Cidade *</label>
                                        <input type="text" class="form-control" id="edit-cidade" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Estado *</label>
                                        <select class="form-control" id="edit-estado" required>
                                            <option value="">Selecione um estado</option>
                                            <option value="AC">Acre</option>
                                            <option value="AL">Alagoas</option>
                                            <option value="AP">Amapá</option>
                                            <option value="AM">Amazonas</option>
                                            <option value="BA">Bahia</option>
                                            <option value="CE">Ceará</option>
                                            <option value="DF">Distrito Federal</option>
                                            <option value="ES">Espírito Santo</option>
                                            <option value="GO">Goiás</option>
                                            <option value="MA">Maranhão</option>
                                            <option value="MT">Mato Grosso</option>
                                            <option value="MS">Mato Grosso do Sul</option>
                                            <option value="MG">Minas Gerais</option>
                                            <option value="PA">Pará</option>
                                            <option value="PB">Paraíba</option>
                                            <option value="PR">Paraná</option>
                                            <option value="PE">Pernambuco</option>
                                            <option value="PI">Piauí</option>
                                            <option value="RJ">Rio de Janeiro</option>
                                            <option value="RN">Rio Grande do Norte</option>
                                            <option value="RS">Rio Grande do Sul</option>
                                            <option value="RO">Rondônia</option>
                                            <option value="RR">Roraima</option>
                                            <option value="SC">Santa Catarina</option>
                                            <option value="SP">São Paulo</option>
                                            <option value="SE">Sergipe</option>
                                            <option value="TO">Tocantins</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label class="form-label">Nome do Responsável *</label>
                                        <input type="text" class="form-control" id="edit-responsavelNome" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Cargo</label>
                                        <input type="text" class="form-control" id="edit-responsavelCargo">
                                    </div>
                                    <div>
                                        <label class="form-label">Email *</label>
                                        <input type="email" class="form-control" id="edit-responsavelEmail" required>
                                    </div>
                                    <div>
                                        <label class="form-label">Telefone *</label>
                                        <input type="tel" class="form-control" id="edit-responsavelTelefone" required>
                                    </div>
                                </div>
                                
                                <div class="form-group mb-3">
                                    <label class="form-label">Serviços de Interesse</label>
                                    <div style="display: flex; flex-wrap: wrap; gap: 10px;">
                                        <label><input type="checkbox" name="servicosInteresse" value="site"> Desenvolvimento de Site</label>
                                        <label><input type="checkbox" name="servicosInteresse" value="marketing"> Marketing Digital</label>
                                        <label><input type="checkbox" name="servicosInteresse" value="seo"> SEO</label>
                                        <label><input type="checkbox" name="servicosInteresse" value="redes"> Redes Sociais</label>
                                        <label><input type="checkbox" name="servicosInteresse" value="sistema"> Sistema Web</label>
                                    </div>
                                </div>
                                
                                <div class="form-group mb-3">
                                    <label class="form-label">Observações</label>
                                    <textarea class="form-control" id="edit-observacoes" rows="3"></textarea>
                                </div>
                                
                                <div style="text-align: right;">
                                    <button type="button" class="btn btn-secondary me-2" onclick="document.getElementById('modalEdicao').style.display='none'">Cancelar</button>
                                    <button type="submit" class="btn btn-primary">Salvar Alterações</button>
                                </div>
                            </form>
                        </div>
                    </div>
                `;

                loadingDiv.classList.add('hidden');
                dadosDiv.classList.remove('hidden');

                // Configurar event listeners após renderizar
                this.setupMinhaEmpresaEventListeners(empresa);
            } else {
                loadingDiv.innerHTML = `
                    <div class="text-center">
                        <h3>Nenhuma empresa encontrada</h3>
                        <p>Vocêainda não possui uma empresa cadastrada.</p>
                        <a href="#cadastro" data-page="cadastro" class="btn btn-primary">Cadastrar Minha Empresa</a>
                    </div>
                `;
            }
        } catch (error) {
            console.error('Erro ao carregar empresa:', error);
            document.getElementById('empresa-loading').innerHTML = '<p>Erro ao carregar dados da empresa.</p>';
        }
    }

    setupMinhaEmpresaEventListeners(empresa) {
        const btnEditar = document.getElementById('btn-editar');
        const btnExcluir = document.getElementById('btn-excluir');
        const confirmarExclusao = document.getElementById('confirmar-exclusao');
        const formEdicao = document.getElementById('form-edicao');

        if (btnEditar) {
            btnEditar.addEventListener('click', () => {
                this.preencherModalEdicao(empresa);
                document.getElementById('modalEdicao').style.display = 'block';
            });
        }

        if (btnExcluir) {
            btnExcluir.addEventListener('click', () => {
                document.getElementById('modalConfirmacao').style.display = 'block';
            });
        }

        if (confirmarExclusao) {
            confirmarExclusao.addEventListener('click', () => {
                this.excluirEmpresa(empresa.id);
            });
        }

        if (formEdicao) {
            formEdicao.addEventListener('submit', (e) => {
                e.preventDefault();
                this.salvarEdicaoEmpresa(empresa.id);
            });
        }
    }

    preencherModalEdicao(empresa) {
        const endereco = empresa.endereco || {};
        
        document.getElementById('edit-razaoSocial').value = empresa.razaoSocial || '';
        document.getElementById('edit-nomeFantasia').value = empresa.nomeFantasia || '';
        document.getElementById('edit-cnpj').value = empresa.cnpj || '';
        document.getElementById('edit-segmento').value = empresa.segmento || '';
        document.getElementById('edit-cep').value = endereco.cep || '';
        document.getElementById('edit-rua').value = endereco.rua || endereco.logradouro || '';
        document.getElementById('edit-cidade').value = endereco.cidade || '';
        document.getElementById('edit-estado').value = endereco.estado || endereco.uf || '';
        document.getElementById('edit-responsavelNome').value = empresa.responsavelNome || empresa.responsavel?.nome || '';
        document.getElementById('edit-responsavelCargo').value = empresa.responsavelCargo || empresa.responsavel?.cargo || '';
        document.getElementById('edit-responsavelEmail').value = empresa.responsavelEmail || empresa.responsavel?.email || '';
        document.getElementById('edit-responsavelTelefone').value = empresa.responsavelTelefone || empresa.responsavel?.telefone || '';
        document.getElementById('edit-observacoes').value = empresa.observacoes || '';

        // Serviços de interesse
        document.querySelectorAll('input[name="servicosInteresse"]').forEach(checkbox => {
            checkbox.checked = false;
        });
        
        if (empresa.servicosInteresse) {
            empresa.servicosInteresse.forEach(servico => {
                const checkbox = document.querySelector(`input[name="servicosInteresse"][value="${servico}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }
    }

    async salvarEdicaoEmpresa(empresaId) {
        const dadosAtualizados = {
            razaoSocial: document.getElementById('edit-razaoSocial').value,
            nomeFantasia: document.getElementById('edit-nomeFantasia').value,
            cnpj: document.getElementById('edit-cnpj').value,
            segmento: document.getElementById('edit-segmento').value,
            endereco: {
                cep: document.getElementById('edit-cep').value,
                rua: document.getElementById('edit-rua').value,
                cidade: document.getElementById('edit-cidade').value,
                estado: document.getElementById('edit-estado').value
            },
            responsavelNome: document.getElementById('edit-responsavelNome').value,
            responsavelCargo: document.getElementById('edit-responsavelCargo').value,
            responsavelEmail: document.getElementById('edit-responsavelEmail').value,
            responsavelTelefone: document.getElementById('edit-responsavelTelefone').value,
            observacoes: document.getElementById('edit-observacoes').value,
            servicosInteresse: []
        };

        // Coletar serviços selecionados
        document.querySelectorAll('input[name="servicosInteresse"]:checked').forEach(checkbox => {
            dadosAtualizados.servicosInteresse.push(checkbox.value);
        });

        try {
            const response = await fetch(`/api/empresas/${empresaId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dadosAtualizados)
            });

            if (response.ok) {
                this.showMessage('Empresa atualizada com sucesso! O status foi alterado para "pendente" e aguarda nova aprovação.', 'success');
                document.getElementById('modalEdicao').style.display = 'none';
                
                // Recarregar dados da empresa
                await this.loadData();
                this.loadEmpresaDoUsuario();
            } else {
                const errorData = await response.json();
                this.showMessage(`Erro ao atualizar empresa: ${errorData.error || response.statusText}`, 'error');
            }
        } catch (error) {
            console.error('Erro ao atualizar empresa:', error);
            this.showMessage('Erro de conexão ao atualizar empresa.', 'error');
        }
    }

    async excluirEmpresa(empresaId) {
        try {
            const response = await fetch(`/api/empresas/${empresaId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showMessage('Empresa excluída com sucesso!', 'success');
                document.getElementById('modalConfirmacao').style.display = 'none';
                
                // Recarregar dados e redirecionar
                await this.loadData();
                this.updateNavigation();
                
                setTimeout(() => {
                    this.navigateTo('home');
                }, 2000);
            } else {
                const errorData = await response.json();
                this.showMessage(`Erro ao excluir empresa: ${errorData.error || response.statusText}`, 'error');
            }
        } catch (error) {
            console.error('Erro ao excluir empresa:', error);
            this.showMessage('Erro de conexão ao excluir empresa.', 'error');
        }
    }



    renderMapaPage(container) {
        // Filtrar empresas aprovadas e com localização
        const empresasVisiveis = this.data.empresas.filter(e => 
            (e.status === 'aprovada' || e.status === 'aprovado') && e.endereco?.cidade
        );

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Mapa de Empresas</h2>
                        <p>Visualize a localização das empresas cadastradas</p>
                    </div>

                    <div class="filtros-container">
                        <div class="filtros-row">
                            <div class="filtro-group">
                                <label for="filtro-segmento">Categoria:</label>
                                <select id="filtro-segmento">
                                    <option value="">Todas as categorias</option>
                                    <option value="Tecnologia">Tecnologia</option>
                                    <option value="Serviços">Serviços</option>
                                    <option value="Comércio">Comércio</option>
                                    <option value="Indústria">Indústria</option>
                                    <option value="Saúde">Saúde</option>
                                    <option value="Educação">Educação</option>
                                    <option value="Outros">Outros</option>
                                </select>
                            </div>
                            <div class="filtro-group">
                                <label for="filtro-distancia">Distância:</label>
                                <select id="filtro-distancia">
                                    <option value="">Todas as distâncias</option>
                                    <option value="5">Até 5 km</option>
                                    <option value="10">Até 10 km</option>
                                    <option value="25">Até 25 km</option>
                                    <option value="50">Até 50 km</option>
                                    <option value="100">Até 100 km</option>
                                </select>
                            </div>
                            <div class="filtro-group">
                                <button id="btn-localizacao" class="btn btn-primary">
                                    📍 Usar minha localização
                                </button>
                            </div>
                        </div>
                        <div id="localizacao-status" class="localizacao-info hidden"></div>
                    </div>

                    <div class="mapa-container">
                        <div id="mapa" style="height: 450px; width: 100%; border-radius: 8px;"></div>
                        <div class="mapa-info">
                            <span id="contador-empresas">Empresas no mapa: ${empresasVisiveis.length}</span>
                        </div>
                    </div>

                    <div class="empresas-lista mt-2">
                        <h3>Lista de Empresas</h3>
                        <div class="cards-grid" id="empresas-mapa-grid">
                            ${this.renderEmpresasCards(empresasVisiveis)}
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Inicializar mapa após renderização
        setTimeout(() => {
            this.initializeMapa(empresasVisiveis);
            this.setupMapaFiltros();
        }, 100);
    }

    async initializeMapa(empresas) {
        // Verificar se Leaflet está carregado
        if (typeof L === 'undefined') {
            console.log('Carregando biblioteca Leaflet...');
            await this.loadLeaflet();
        }

        const mapElement = document.getElementById('mapa');
        if (!mapElement) return;

        // Inicializar mapa centrado no Brasil
        this.mapa = L.map('mapa').setView([-14.235, -51.9253], 4);

        // Adicionar camada de tiles do OpenStreetMap
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.mapa);

        // Adicionar marcadores para empresas
        this.allMarkers = [];
        this.userLocationMarker = null;
        this.userLocation = null;

        for (const empresa of empresas) {
            try {
                const coordenadas = await this.obterCoordenadas(empresa);
                if (coordenadas) {
                    const marker = L.marker([coordenadas.lat, coordenadas.lng])
                        .addTo(this.mapa)
                        .bindPopup(`
                            <div class="popup-empresa">
                                <h4>${empresa.nomeFantasia || empresa.nome}</h4>
                                <p><strong>Segmento:</strong> ${empresa.segmento || empresa.categoria || 'Não informado'}</p>
                                <p><strong>Cidade:</strong> ${empresa.endereco?.cidade || empresa.cidade}</p>
                                <p><strong>Responsável:</strong> ${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}</p>
                                <button class="btn btn-sm btn-primary" onclick="digitalizeApp.verDetalhesEmpresa('${empresa.id}')">Ver Detalhes</button>
                            </div>
                        `);
                    
                    marker.empresaData = empresa;
                    marker.coordenadas = coordenadas;
                    this.allMarkers.push(marker);
                }
            } catch (error) {
                console.log(`Erro ao obter coordenadas para ${empresa.nomeFantasia}:`, error);
            }
        }

        // Ajustar vista para mostrar todos os marcadores
        if (this.allMarkers.length > 0) {
            const group = new L.featureGroup(this.allMarkers);
            this.mapa.fitBounds(group.getBounds().pad(0.1));
        }
    }

    async loadLeaflet() {
        return new Promise((resolve, reject) => {
            // Carregar CSS do Leaflet
            const linkCSS = document.createElement('link');
            linkCSS.rel = 'stylesheet';
            linkCSS.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
            document.head.appendChild(linkCSS);

            // Carregar JS do Leaflet
            const script = document.createElement('script');
            script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
            script.onload = () => resolve();
            script.onerror = () => reject(new Error('Erro ao carregar Leaflet'));
            document.head.appendChild(script);
        });
    }

    async obterCoordenadas(empresa) {
        // Cache de coordenadas para evitar muitas requisições
        if (!this.coordenadasCache) {
            this.coordenadasCache = new Map();
        }

        const cidade = empresa.endereco?.cidade || empresa.cidade;
        const estado = empresa.endereco?.estado || empresa.endereco?.uf || empresa.estado;
        
        if (!cidade) return null;

        const chaveCache = `${cidade}-${estado}`;
        if (this.coordenadasCache.has(chaveCache)) {
            return this.coordenadasCache.get(chaveCache);
        }

        try {
            // Usar Nominatim para geocodificação (serviço gratuito do OpenStreetMap)
            const query = encodeURIComponent(`${cidade}, ${estado}, Brazil`);
            const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${query}&limit=1`);
            const data = await response.json();

            if (data && data.length > 0) {
                const coordenadas = {
                    lat: parseFloat(data[0].lat),
                    lng: parseFloat(data[0].lon)
                };
                this.coordenadasCache.set(chaveCache, coordenadas);
                return coordenadas;
            }
        } catch (error) {
            console.error('Erro ao obter coordenadas:', error);
        }

        return null;
    }

    setupMapaFiltros() {
        const filtroSegmento = document.getElementById('filtro-segmento');
        const filtroDistancia = document.getElementById('filtro-distancia');
        const btnLocalizacao = document.getElementById('btn-localizacao');

        // Filtro por segmento
        if (filtroSegmento) {
            filtroSegmento.addEventListener('change', () => {
                this.aplicarFiltrosMapa();
            });
        }

        // Filtro por distância
        if (filtroDistancia) {
            filtroDistancia.addEventListener('change', () => {
                this.aplicarFiltrosMapa();
            });
        }

        // Botão de localização
        if (btnLocalizacao) {
            btnLocalizacao.addEventListener('click', () => {
                this.obterLocalizacaoUsuario();
            });
        }
    }

    async obterLocalizacaoUsuario() {
        const statusDiv = document.getElementById('localizacao-status');
        const btnLocalizacao = document.getElementById('btn-localizacao');

        if (!navigator.geolocation) {
            statusDiv.innerHTML = '<span class="error">Geolocalização não é suportada pelo seu navegador.</span>';
            statusDiv.classList.remove('hidden');
            return;
        }

        btnLocalizacao.disabled = true;
        btnLocalizacao.innerHTML = '⏳ Obtendo localização...';
        statusDiv.innerHTML = '<span class="info">Obtendo sua localização...</span>';
        statusDiv.classList.remove('hidden');

        navigator.geolocation.getCurrentPosition(
            (position) => {
                this.userLocation = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };

                // Remover marcador anterior se existir
                if (this.userLocationMarker) {
                    this.mapa.removeLayer(this.userLocationMarker);
                }

                // Adicionar marcador da localização do usuário
                this.userLocationMarker = L.marker([this.userLocation.lat, this.userLocation.lng], {
                    icon: L.divIcon({
                        className: 'user-location-marker',
                        html: '📍',
                        iconSize: [25, 25],
                        iconAnchor: [12, 12]
                    })
                }).addTo(this.mapa).bindPopup('Sua localização');

                btnLocalizacao.disabled = false;
                btnLocalizacao.innerHTML = '✅ Localização obtida';
                statusDiv.innerHTML = '<span class="success">Localização obtida com sucesso! Use o filtro de distância.</span>';

                // Aplicar filtros novamente com a nova localização
                this.aplicarFiltrosMapa();

                // Centralizar mapa na localização do usuário
                this.mapa.setView([this.userLocation.lat, this.userLocation.lng], 12);
            },
            (error) => {
                console.error('Erro ao obter localização:', error);
                btnLocalizacao.disabled = false;
                btnLocalizacao.innerHTML = '❌ Erro na localização';
                
                let errorMessage = 'Erro ao obter localização. ';
                switch(error.code) {
                    case error.PERMISSION_DENIED:
                        errorMessage += 'Permissão negada.';
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorMessage += 'Localização indisponível.';
                        break;
                    case error.TIMEOUT:
                        errorMessage += 'Tempo limite excedido.';
                        break;
                    default:
                        errorMessage += 'Erro desconhecido.';
                        break;
                }
                
                statusDiv.innerHTML = `<span class="error">${errorMessage}</span>`;
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 300000 // 5 minutos
            }
        );
    }

    calcularDistancia(lat1, lng1, lat2, lng2) {
        const R = 6371; // Raio da Terra em km
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLng = (lng2 - lng1) * Math.PI / 180;
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    aplicarFiltrosMapa() {
        const segmentoSelecionado = document.getElementById('filtro-segmento')?.value;
        const distanciaSelecionada = document.getElementById('filtro-distancia')?.value;
        
        let empresasFiltradas = [];
        let markersVisiveis = 0;

        this.allMarkers.forEach(marker => {
            const empresa = marker.empresaData;
            let mostrar = true;

            // Filtro por segmento
            if (segmentoSelecionado && 
                empresa.segmento !== segmentoSelecionado && 
                empresa.categoria !== segmentoSelecionado) {
                mostrar = false;
            }

            // Filtro por distância
            if (mostrar && distanciaSelecionada && this.userLocation) {
                const distancia = this.calcularDistancia(
                    this.userLocation.lat, this.userLocation.lng,
                    marker.coordenadas.lat, marker.coordenadas.lng
                );
                
                if (distancia > parseFloat(distanciaSelecionada)) {
                    mostrar = false;
                }
            }

            // Mostrar/esconder marcador
            if (mostrar) {
                marker.addTo(this.mapa);
                empresasFiltradas.push(empresa);
                markersVisiveis++;
            } else {
                this.mapa.removeLayer(marker);
            }
        });

        // Atualizar contador
        const contador = document.getElementById('contador-empresas');
        if (contador) {
            contador.textContent = `Empresas no mapa: ${markersVisiveis}`;
        }

        // Atualizar lista de empresas
        this.filtrarEmpresasNoMapa(empresasFiltradas);
    }

    filtrarEmpresasNoMapa(empresasFiltradas) {
        const grid = document.getElementById('empresas-mapa-grid');
        if (grid) {
            grid.innerHTML = this.renderEmpresasCards(empresasFiltradas);
            // Reconfigurar botões de favoritos
            setTimeout(() => {
                this.setupFavoritoButtons();
            }, 100);
        }
    }

    verDetalhesEmpresa(empresaId) {
        const empresa = this.data.empresas.find(e => e.id == empresaId);
        if (empresa) {
            alert(`Detalhes da empresa ${empresa.nomeFantasia || empresa.nome}:\n\nSegmento: ${empresa.segmento || 'Não informado'}\nCidade: ${empresa.endereco?.cidade || 'Não informado'}\nResponsável: ${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}`);
        }
    }



    renderEmpresasCards(empresas) {
        return empresas.map(empresa => `
            <div class="card empresa-card">
                <div class="empresa-header">
                    <div class="empresa-logo">
                        ${(empresa.nomeFantasia || empresa.nome || 'E').charAt(0).toUpperCase()}
                    </div>
                    <div class="empresa-title">
                        <h3>${empresa.nomeFantasia || empresa.nome}</h3>
                        <span class="empresa-segmento">${empresa.segmento || empresa.categoria || 'Não informado'}</span>
                    </div>
                    <span class="status-badge status-${empresa.status || 'pendente'}">${empresa.status || 'Pendente'}</span>
                </div>
                <div class="empresa-info">
                    <p><strong>Razão Social:</strong> ${empresa.razaoSocial || 'Não informado'}</p>
                    <p><strong>CNPJ:</strong> ${empresa.cnpj || 'Não informado'}</p>
                    <p><strong>Cidade:</strong> ${empresa.endereco?.cidade || empresa.cidade || 'Não informado'}</p>
                    <p><strong>Responsável:</strong> ${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}</p>
                </div>
            </div>
        `).join('');
    }

    async verificarEmpresaExistente(container, user) {
        try {
            const response = await fetch(`/api/empresas/usuario/${user.email}`);

            if (response.ok) {
                const empresas = await response.json();

                if (empresas.length > 0) {
                    // Mostrar os dados completos da empresa em vez do formulário
                    const empresa = empresas[0];

                    container.innerHTML = `
                        <div class="section fade-in">
                            <div class="container">
                                <div class="page-header text-center">
                                    <h2>Minha Empresa Cadastrada</h2>
                                    <p>Você já possui uma empresa cadastrada. Veja os detalhes abaixo.</p>
                                    <span class="status-badge status-${empresa.status || 'pendente'}">${empresa.status || 'Pendente'}</span>
                                </div>

                                <div class="empresa-detalhes">
                                    <div class="form-section">
                                        <h3>Dados da Empresa</h3>
                                        <div class="info-grid">
                                            <div class="info-item">
                                                <label>Nome Fantasia:</label>
                                                <span>${empresa.nomeFantasia || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Razão Social:</label>
                                                <span>${empresa.razaoSocial || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>CNPJ:</label>
                                                <span>${empresa.cnpj || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Segmento:</label>
                                                <span>${empresa.segmento || 'Não informado'}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="form-section">
                                        <h3>Endereço</h3>
                                        <div class="info-grid">
                                            <div class="info-item">
                                                <label>CEP:</label>
                                                <span>${empresa.endereco?.cep || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Rua:</label>
                                                <span>${empresa.endereco?.rua || empresa.endereco?.logradouro || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Cidade:</label>
                                                <span>${empresa.endereco?.cidade || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Estado:</label>
                                                <span>${empresa.endereco?.estado || empresa.endereco?.uf || 'Não informado'}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="form-section">
                                        <h3>Responsável</h3>
                                        <div class="info-grid">
                                            <div class="info-item">
                                                <label>Nome:</label>
                                                <span>${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Cargo:</label>
                                                <span>${empresa.responsavelCargo || empresa.responsavel?.cargo || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Email:</label>
                                                <span>${empresa.responsavelEmail || empresa.responsavel?.email || 'Não informado'}</span>
                                            </div>
                                            <div class="info-item">
                                                <label>Telefone:</label>
                                                <span>${empresa.responsavelTelefone || empresa.responsavel?.telefone || 'Não informado'}</span>
                                            </div>
                                        </div>
                                    </div>

                                    ${empresa.servicosInteresse && empresa.servicosInteresse.length > 0 ? `
                                        <div class="form-section">
                                            <h3>Serviços de Interesse</h3>
                                            <div class="servicos-lista">
                                                ${empresa.servicosInteresse.map(servico => `<span class="badge">${servico}</span>`).join('')}
                                            </div>
                                        </div>
                                    ` : ''}

                                    ${empresa.observacoes ? `
                                        <div class="form-section">
                                            <h3>Observações</h3>
                                            <p>${empresa.observacoes}</p>
                                        </div>
                                    ` : ''}

                                    <div class="form-actions text-center">
                                        <p class="text-muted mb-3">Cada usuário pode cadastrar apenas uma empresa.</p>
                                        <a href="#" data-page="home" class="btn btn-primary">Voltar ao Início</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                    return;
                }
            }

            // Se chegou aqui, o usuário não tem empresa, pode renderizar o formulário
            this.renderFormularioCadastro(container);

        } catch (error) {
            console.error('Erro ao verificar empresa existente:', error);
            this.renderFormularioCadastro(container);
        }
    }
    async handleCadastroSubmit(event) {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);
        const empresaData = {};

        // Processar dados do formulário
        formData.forEach((value, key) => {
            if (key === 'servicosInteresse') {
                if (!empresaData.servicosInteresse) {
                    empresaData.servicosInteresse = [];
                }
                empresaData.servicosInteresse.push(value);
            } else {
                empresaData[key] = value;
            }
        });

        // Estruturar dados de endereço
        empresaData.endereco = {
            cep: empresaData.cep,
            rua: empresaData.rua,
            cidade: empresaData.cidade,
            estado: empresaData.estado
        };
        delete empresaData.cep;
        delete empresaData.rua;
        delete empresaData.cidade;
        delete empresaData.estado;

        try {
            const response = await fetch('/admin/cadastro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(empresaData)
            });

            if (response.ok) {
                const result = await response.json();
                this.showMessage('Empresa cadastrada com sucesso! Redirecionando...', 'success');
                form.reset();

                // Recarregar dados
                await this.loadData();

                // Atualizar navegação para refletir mudanças
                this.updateNavigation();

                // Redirecionar imediatamente (ou página inicial se for admin)
                const user = this.getCurrentUser();
                setTimeout(() => {
                    if (user && (user.tipo === 'admin' || user.tipo === 'administrador')) {
                        this.navigateTo('home');
                    } else {
                        this.navigateTo('minha-empresa');
                    }
                }, 1500);
            } else {
                const errorData = await response.json();
                this.showMessage(`Erro ao cadastrar empresa: ${errorData.mensagem || response.statusText}`, 'error');
            }
        } catch (error) {
            console.error('Erro ao enviar formulário:', error);
            this.showMessage('Erro de conexão ao cadastrar empresa.', 'error');
        }
    }

    handleSearch(query) {
        // Filtrar apenas empresas aprovadas primeiro
        const empresasAprovadas = this.data.empresas.filter(empresa => 
            empresa.status === 'aprovada' || empresa.status === 'aprovado'
        );

        // Depois aplicar o filtro de pesquisa
        const filteredEmpresas = empresasAprovadas.filter(empresa => 
            (empresa.nomeFantasia || empresa.nome || '').toLowerCase().includes(query.toLowerCase()) ||
            (empresa.razaoSocial || '').toLowerCase().includes(query.toLowerCase()) ||
            (empresa.segmento || empresa.categoria || '').toLowerCase().includes(query.toLowerCase())
        );

        const empresasGrid = document.getElementById('empresas-grid');
        if (empresasGrid) {
            empresasGrid.innerHTML = this.renderEmpresasCards(filteredEmpresas);
        }
    }

    showMessage(message, type = 'info') {
        const msgBox = document.getElementById('cadastro-message');
        if (msgBox) {
            msgBox.textContent = message;
            msgBox.className = `message ${type}`;
            msgBox.classList.remove('hidden');
            setTimeout(() => {
                msgBox.classList.add('hidden');
            }, 5000);
        } else {
            // Fallback para alert se não houver elemento de mensagem
            alert(message);
        }
    }

    showError(message) {
        this.showMessage(message, 'error');
    }
    async renderCadastro(container) {
        const user = this.getCurrentUser();

        if (!user) {
            // Renderizar formulário mesmo sem login, mas mostrar aviso
            this.renderFormularioCadastroComAviso(container);
            return;
        }

        // Verificar se o usuário já tem uma empresa cadastrada
        this.verificarEmpresaExistente(container, user);
    }

    renderFormularioCadastroComAviso(container) {
        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="alert alert-info">
                        Você precisa estar logado para finalizar o cadastro da empresa.
                        <a href="login.html" class="alert-link">Faça login aqui</a>.
                    </div>
                    ${this.getFormularioCadastroHTML()}
                </div>
            </div>
        `;
        this.setupFormularioCadastro();
    }

    getFormularioCadastroHTML() {
        return `
            <div class="page-header">
                <h2>Cadastrar Empresa</h2>
                <p>Preencha os dados da sua empresa para cadastro na plataforma</p>
            </div>

            <div id="cadastro-message" class="message hidden"></div>

            <form id="cadastro-form" class="form-container">
                <div class="form-section">
                    <h3>Dados da Empresa</h3>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="nomeFantasia">Nome Fantasia *</label>
                            <input type="text" id="nomeFantasia" name="nomeFantasia" required>
                        </div>

                        <div class="form-group">
                            <label for="razaoSocial">Razão Social *</label>
                            <input type="text" id="razaoSocial" name="razaoSocial" required>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="cnpj">CNPJ *</label>
                            <input type="text" id="cnpj" name="cnpj" required placeholder="00.000.000/0000-00">
                        </div>

                        <div class="form-group">
                            <label for="segmento">Segmento *</label>
                            <select id="segmento" name="segmento" required>
                                <option value="">Selecione um segmento</option>
                                <option value="Tecnologia">Tecnologia</option>
                                <option value="Serviços">Serviços</option>
                                <option value="Comércio">Comércio</option>
                                <option value="Indústria">Indústria</option>
                                <option value="Saúde">Saúde</option>
                                <option value="Educação">Educação</option>
                                <option value="Outros">Outros</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <h3>Endereço</h3>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="cep">CEP *</label>
                            <input type="text" id="cep" name="cep" required placeholder="00000-000">
                        </div>

                        <div class="form-group">
                            <label for="rua">Rua/Logradouro *</label>
                            <input type="text" id="rua" name="rua" required>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="cidade">Cidade *</label>
                            <input type="text" id="cidade" name="cidade" required>
                        </div>

                        <div class="form-group">
                            <label for="estado">Estado *</label>
                            <select id="estado" name="estado" required>
                                <option value="">Selecione um estado</option>
                                <option value="AC">Acre</option>
                                <option value="AL">Alagoas</option>
                                <option value="AP">Amapá</option>
                                <option value="AM">Amazonas</option>
                                <option value="BA">Bahia</option>
                                <option value="CE">Ceará</option>
                                <option value="DF">Distrito Federal</option>
                                <option value="ES">Espírito Santo</option>
                                <option value="GO">Goiás</option>
                                <option value="MA">Maranhão</option>
                                <option value="MT">Mato Grosso</option>
                                <option value="MS">Mato Grosso do Sul</option>
                                <option value="MG">Minas Gerais</option>
                                <option value="PA">Pará</option>
                                <option value="PB">Paraíba</option>
                                <option value="PR">Paraná</option>
                                <option value="PE">Pernambuco</option>
                                <option value="PI">Piauí</option>
                                <option value="RJ">Rio de Janeiro</option>
                                <option value="RN">Rio Grande do Norte</option>
                                <option value="RS">Rio Grande do Sul</option>
                                <option value="RO">Rondônia</option>
                                <option value="RR">Roraima</option>
                                <option value="SC">Santa Catarina</option>
                                <option value="SP">São Paulo</option>
                                <option value="SE">Sergipe</option>
                                <option value="TO">Tocantins</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <h3>Responsável</h3>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="responsavelNome">Nome do Responsável *</label>
                            <input type="text" id="responsavelNome" name="responsavelNome" required>
                        </div>

                        <div class="form-group">
                            <label for="responsavelCargo">Cargo</label>
                            <input type="text" id="responsavelCargo" name="responsavelCargo">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="responsavelEmail">Email *</label>
                            <input type="email" id="responsavelEmail" name="responsavelEmail" required>
                        </div>

                        <div class="form-group">
                            <label for="responsavelTelefone">Telefone *</label>
                            <input type="tel" id="responsavelTelefone" name="responsavelTelefone" required placeholder="(00) 00000-0000">
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <h3>Serviços de Interesse</h3>
                    <div class="checkbox-group">
                        <label><input type="checkbox" name="servicosInteresse" value="site"> Desenvolvimento de Site</label>
                        <label><input type="checkbox" name="servicosInteresse" value="marketing"> Marketing Digital</label>
                        <label><input type="checkbox" name="servicosInteresse" value="seo"> SEO</label>
                        <label><input type="checkbox" name="servicosInteresse" value="redes"> Redes Sociais</label>
                        <label><input type="checkbox" name="servicosInteresse" value="sistema"> Sistema Web</label>
                    </div>
                </div>

                <div class="form-section">
                    <h3>Observações</h3>
                    <div class="form-group">
                        <textarea id="observacoes" name="observacoes" rows="4" placeholder="Observações adicionais..."></textarea>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Cadastrar Empresa</button>
                    <button type="reset" class="btn btn-secondary">Limpar Formulário</button>
                </div>
            </form>
        `;
    }

    setupFormularioCadastro() {
        const form = document.getElementById('cadastro-form');
        if (form) {
            form.addEventListener('submit', (e) => this.handleCadastroSubmit(e));
            this.setupFormMasks();
        }
    }



    handleSearch(query) {
        // Filtrar apenas empresas aprovadas primeiro
        const empresasAprovadas = this.data.empresas.filter(empresa => 
            empresa.status === 'aprovada' || empresa.status === 'aprovado'
        );

        // Depois aplicar o filtro de pesquisa
        const filteredEmpresas = empresasAprovadas.filter(empresa => 
            (empresa.nomeFantasia || empresa.nome || '').toLowerCase().includes(query.toLowerCase()) ||
            (empresa.razaoSocial || '').toLowerCase().includes(query.toLowerCase()) ||
            (empresa.segmento || empresa.categoria || '').toLowerCase().includes(query.toLowerCase())
        );

        const empresasGrid = document.getElementById('empresas-grid');
        if (empresasGrid) {
            empresasGrid.innerHTML = this.renderEmpresasCards(filteredEmpresas);
        }
    }

    showMessage(message, type = 'info') {
        const msgBox = document.getElementById('cadastro-message');
        if (msgBox) {
            msgBox.textContent = message;
            msgBox.className = `message ${type}`;
            msgBox.classList.remove('hidden');
            setTimeout(() => {
                msgBox.classList.add('hidden');
            }, 5000);
        } else {
            // Fallback para alert se não houver elemento de mensagem
            alert(message);
        }
    }

    showError(message) {
        this.showMessage(message, 'error');
    }

    setupFormMasks() {
        // Máscara para CNPJ
        const cnpjInput = document.getElementById('cnpj');
        if (cnpjInput) {
            cnpjInput.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');
                value = value.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2}).*/, '$1.$2.$3/$4-$5');
                e.target.value = value;
            });
        }

        // Máscara para CEP
        const cepInput = document.getElementById('cep');
        if (cepInput) {
            cepInput.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');
                value = value.replace(/^(\d{5})(\d{3}).*/, '$1-$2');
                e.target.value = value;
            });
        }

        // Máscara para telefone
        const telefoneInput = document.getElementById('responsavelTelefone');
        if (telefoneInput) {
            telefoneInput.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');
                value = value.replace(/^(\d{2})(\d{5})(\d{4}).*/, '($1) $2-$3');
                e.target.value = value;
            });
        }
    }

    renderAvaliacoesPage(container) {
        const user = this.getCurrentUser();
        
        if (!user) {
            container.innerHTML = `
                <div class="section fade-in">
                    <div class="container text-center">
                        <h2>Acesso Restrito</h2>
                        <p>Você precisa estar logado para ver avaliações.</p>
                        <a href="login.html" class="btn btn-primary">Fazer Login</a>
                    </div>
                </div>
            `;
            return;
        }

        const avaliacoes = this.data.avaliacoes || [];

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Avaliações</h2>
                        <p>Avaliações e comentários sobre empresas</p>
                    </div>

                    <div class="avaliacoes-container">
                        ${avaliacoes.length > 0 ? 
                            avaliacoes.map(avaliacao => `
                                <div class="card avaliacao-card">
                                    <div class="avaliacao-header">
                                        <h4>${avaliacao.empresaNome || 'Empresa não identificada'}</h4>
                                        <div class="rating">
                                            ${'★'.repeat(avaliacao.nota || 0)}${'☆'.repeat(5 - (avaliacao.nota || 0))}
                                        </div>
                                    </div>
                                    <div class="avaliacao-content">
                                        <p>${avaliacao.comentario || 'Sem comentário'}</p>
                                        <small class="text-muted">Por: ${avaliacao.usuarioNome || 'Anônimo'} - ${new Date(avaliacao.data).toLocaleDateString()}</small>
                                    </div>
                                </div>
                            `).join('') : 
                            '<div class="empty-state"><p>Nenhuma avaliação encontrada.</p></div>'
                        }
                    </div>

                    <div class="form-section mt-3">
                        <h3>Deixe sua avaliação</h3>
                        <form id="avaliacao-form" class="form-container">
                            <div class="form-group">
                                <label for="empresa-select">Empresa *</label>
                                <select id="empresa-select" name="empresaId" required>
                                    <option value="">Selecione uma empresa</option>
                                    ${this.data.empresas.filter(e => e.status === 'aprovada' || e.status === 'aprovado').map(empresa => 
                                        `<option value="${empresa.id}">${empresa.nomeFantasia || empresa.nome}</option>`
                                    ).join('')}
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="nota">Nota *</label>
                                <select id="nota" name="nota" required>
                                    <option value="">Selecione uma nota</option>
                                    <option value="1">1 - Muito Ruim</option>
                                    <option value="2">2 - Ruim</option>
                                    <option value="3">3 - Regular</option>
                                    <option value="4">4 - Bom</option>
                                    <option value="5">5 - Excelente</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="comentario">Comentário</label>
                                <textarea id="comentario" name="comentario" rows="4" placeholder="Descreva sua experiência..."></textarea>
                            </div>
                            <div class="form-actions">
                                <button type="submit" class="btn btn-primary">Enviar Avaliação</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        `;

        // Adicionar event listener para o formulário
        const form = document.getElementById('avaliacao-form');
        if (form) {
            form.addEventListener('submit', (e) => this.handleAvaliacaoSubmit(e));
        }
    }

    renderFavoritosPage(container) {
        const user = this.getCurrentUser();
        
        if (!user) {
            container.innerHTML = `
                <div class="section fade-in">
                    <div class="container text-center">
                        <h2>Acesso Restrito</h2>
                        <p>Você precisa estar logado para ver seus favoritos.</p>
                        <a href="login.html" class="btn btn-primary">Fazer Login</a>
                    </div>
                </div>
            `;
            return;
        }

        // Buscar favoritos do usuário do localStorage
        const favoritos = JSON.parse(localStorage.getItem(`favoritos_${user.email}`) || '[]');
        console.log('Favoritos do usuário:', favoritos);
        console.log('Todas as empresas:', this.data.empresas);
        
        // Filtrar empresas favoritas (comparando IDs como string)
        const empresasFavoritas = this.data.empresas.filter(empresa => {
            const empresaAprovada = empresa.status === 'aprovada' || empresa.status === 'aprovado';
            const isFavoritada = favoritos.some(favId => String(favId) === String(empresa.id));
            return empresaAprovada && isFavoritada;
        });

        console.log('Empresas favoritas encontradas:', empresasFavoritas);

        container.innerHTML = `
            <div class="section fade-in">
                <div class="container">
                    <div class="page-header">
                        <h2>Empresas Favoritas</h2>
                        <p>Suas empresas favoritas (${empresasFavoritas.length} empresas)</p>
                    </div>

                    <div class="cards-grid">
                        ${empresasFavoritas.length > 0 ? 
                            empresasFavoritas.map(empresa => `
                                <div class="card empresa-card">
                                    <div class="empresa-header">
                                        <h3>${empresa.nomeFantasia || empresa.nome}</h3>
                                        <button class="btn btn-sm btn-danger remove-favorito" data-empresa-id="${empresa.id}">
                                            ❤️ Remover
                                        </button>
                                    </div>
                                    <div class="empresa-info">
                                        <p><strong>Razão Social:</strong> ${empresa.razaoSocial || 'Não informado'}</p>
                                        <p><strong>CNPJ:</strong> ${empresa.cnpj || 'Não informado'}</p>
                                        <p><strong>Segmento:</strong> ${empresa.segmento || empresa.categoria || 'Não informado'}</p>
                                        <p><strong>Cidade:</strong> ${empresa.endereco?.cidade || empresa.cidade || 'Não informado'}</p>
                                        <p><strong>Responsável:</strong> ${empresa.responsavelNome || empresa.responsavel?.nome || 'Não informado'}</p>
                                    </div>
                                </div>
                            `).join('') : 
                            `<div class="empty-state">
                                <p>Você ainda não tem empresas favoritas.</p>
                                <a href="#empresas" data-page="empresas" class="btn btn-primary">Ver Empresas</a>
                            </div>`
                        }
                    </div>
                </div>
            </div>
        `;

        // Adicionar event listeners para remover favoritos
        document.querySelectorAll('.remove-favorito').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const empresaId = e.currentTarget.dataset.empresaId;
                this.removerFavorito(empresaId);
            });
        });
    }

    async handleAvaliacaoSubmit(event) {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);
        const user = this.getCurrentUser();

        if (!user) {
            alert('Você precisa estar logado para enviar uma avaliação.');
            return;
        }

        const avaliacaoData = {
            empresaId: formData.get('empresaId'),
            nota: parseInt(formData.get('nota')),
            comentario: formData.get('comentario'),
            usuarioEmail: user.email,
            usuarioNome: user.nome,
            data: new Date().toISOString()
        };

        // Encontrar nome da empresa
        const empresa = this.data.empresas.find(e => e.id == avaliacaoData.empresaId);
        if (empresa) {
            avaliacaoData.empresaNome = empresa.nomeFantasia || empresa.nome;
        }

        try {
            const response = await fetch('/api/avaliacoes', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(avaliacaoData)
            });

            if (response.ok) {
                alert('Avaliação enviada com sucesso!');
                form.reset();
                // Recarregar dados e página
                await this.loadData();
                this.renderAvaliacoesPage(document.getElementById('main-content'));
            } else {
                alert('Erro ao enviar avaliação. Tente novamente.');
            }
        } catch (error) {
            console.error('Erro ao enviar avaliação:', error);
            alert('Erro de conexão ao enviar avaliação.');
        }
    }

    adicionarFavorito(empresaId) {
        const user = this.getCurrentUser();
        if (!user) {
            alert('Você precisa estar logado para adicionar favoritos.');
            return;
        }

        // Converter IDs para string para garantir comparação correta
        const empresaIdStr = String(empresaId);
        const favoritos = JSON.parse(localStorage.getItem(`favoritos_${user.email}`) || '[]');
        
        // Verificar se já existe (comparando como string)
        const jaExiste = favoritos.some(id => String(id) === empresaIdStr);
        
        if (!jaExiste) {
            favoritos.push(empresaIdStr);
            localStorage.setItem(`favoritos_${user.email}`, JSON.stringify(favoritos));
            
            console.log('Empresa adicionada aos favoritos:', empresaIdStr);
            this.showMessage('Empresa adicionada aos favoritos!', 'success');
        } else {
            this.showMessage('Esta empresa já está nos seus favoritos.', 'info');
        }
    }

    removerFavorito(empresaId) {
        const user = this.getCurrentUser();
        if (!user) return;

        // Converter ID para string para garantir comparação correta
        const empresaIdStr = String(empresaId);
        const favoritos = JSON.parse(localStorage.getItem(`favoritos_${user.email}`) || '[]');
        const novosFavoritos = favoritos.filter(id => String(id) !== empresaIdStr);
        
        localStorage.setItem(`favoritos_${user.email}`, JSON.stringify(novosFavoritos));
        
        console.log('Empresa removida dos favoritos:', empresaIdStr);
        this.showMessage('Empresa removida dos favoritos!', 'success');
        
        // Se estivermos na página de favoritos, recarregar
        if (this.currentPage === 'favoritos') {
            this.renderFavoritosPage(document.getElementById('main-content'));
        }
    }

    setupFavoritoButtons() {
        const user = this.getCurrentUser();
        if (!user) return;

        const favoritos = JSON.parse(localStorage.getItem(`favoritos_${user.email}`) || '[]');
        console.log('Favoritos atuais:', favoritos);
        
        document.querySelectorAll('.favorito-btn').forEach(btn => {
            const empresaId = btn.dataset.empresaId;
            const empresaIdStr = String(empresaId);
            
            // Verificar se já está nos favoritos (comparando como string)
            const isFavoritado = favoritos.some(id => String(id) === empresaIdStr);
            
            // Aplicar classe apropriada
            if (isFavoritado) {
                btn.classList.add('favoritado');
                console.log('Empresa favoritada:', empresaIdStr);
            } else {
                btn.classList.remove('favoritado');
            }
            
            // Remover listeners antigos
            btn.removeEventListener('click', btn._favoritoHandler);
            
            // Criar novo handler
            btn._favoritoHandler = (e) => {
                e.preventDefault();
                e.stopPropagation();
                
                const empresaId = e.currentTarget.dataset.empresaId;
                const empresaIdStr = String(empresaId);
                const favoritosAtuais = JSON.parse(localStorage.getItem(`favoritos_${user.email}`) || '[]');
                const jaFavoritado = favoritosAtuais.some(id => String(id) === empresaIdStr);
                
                if (jaFavoritado) {
                    this.removerFavorito(empresaId);
                } else {
                    this.adicionarFavorito(empresaId);
                }
                
                // Atualizar aparência do botão
                setTimeout(() => {
                    this.setupFavoritoButtons();
                }, 200);
            };
            
            // Adicionar novo listener
            btn.addEventListener('click', btn._favoritoHandler);
        });
    }
}

// Espera o carregamento da página antes de fazer requisições
document.addEventListener("DOMContentLoaded", () => {

    console.log("Front-end carregado. Testando integração com o backend...");

    // Faz uma requisição GET para o backend (Spark Java)
    fetch("http://localhost:4567/canais")
        .then(res => res.json())              // Converte a resposta em JSON
        .then(data => {
            console.log("Dados recebidos do backend:", data); // Exibe no console

            // Exemplo simples: mostra os canais na página principal
            const main = document.getElementById("main-content");
            main.innerHTML = "<h2>Lista de Canais</h2>";

            data.forEach(canal => {
                main.innerHTML += `
                    <div class="canal-item">
                        <p><strong>Plataforma:</strong> ${canal.plataforma}</p>
                        <p><strong>Identificador:</strong> ${canal.canal_identificador}</p>
                        <hr>
                    </div>
                `;
            });
        })
        .catch(err => {
            console.error("Erro ao buscar canais:", err);
            alert("Não foi possível conectar ao servidor.");
        });
});