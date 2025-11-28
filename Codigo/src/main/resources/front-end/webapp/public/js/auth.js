// Sistema de autenticação (Versão Corrigida para o Backend Java)
class AuthSystem {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    init() {
        // Verificar se há usuário logado no localStorage
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
            try {
                this.currentUser = JSON.parse(savedUser);
                this.updateUI();
            } catch (e) {
                console.error("Erro ao recuperar sessão", e);
                localStorage.removeItem('currentUser');
            }
        }
    }

    async login(email, senha) {
        try {
            // Usa a origem atual (seja localhost ou azurewebsites.net)
            const API_BASE_URL = window.location.origin;
            
            const response = await fetch(`${API_BASE_URL}/api/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, senha })
            });

            // Se o login falhar (401), o backend retorna { "error": "..." }
            // Se der certo (200), retorna o objeto Usuario { "id": 1, ... }
            const data = await response.json();

            // VERIFICAÇÃO CORRIGIDA: Se tem ID, o login funcionou
            if (response.ok && data.id) {
                this.currentUser = data;
                localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
                this.updateUI();
                return { success: true, message: 'Login realizado com sucesso!' };
            } else {
                return { success: false, message: data.error || 'Email ou senha incorretos' };
            }
        } catch (error) {
            console.error('Erro no login:', error);
            return { success: false, message: 'Erro de conexão com o servidor' };
        }
    }

    async register(userData) {
        try {
            const API_BASE_URL = window.location.origin;

            // Envia direto para o backend. O banco de dados já vai bloquear duplicados.
            const newUser = {
                nome: userData.nome,
                email: userData.email,
                senha: userData.senha,
                tipo: 'usuario',
                ativo: true
            };

            const response = await fetch(`${API_BASE_URL}/api/usuarios`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newUser)
            });

            if (response.ok) {
                return { success: true, message: 'Usuário cadastrado com sucesso! Faça login.' };
            } else {
                // Tenta ler o erro do Java
                try {
                    const err = await response.json();
                    return { success: false, message: err.error || 'Erro ao cadastrar usuário' };
                } catch (e) {
                    return { success: false, message: 'Erro ao cadastrar usuário' };
                }
            }
        } catch (error) {
            console.error('Erro no registro:', error);
            return { success: false, message: 'Erro de conexão' };
        }
    }

    logout() {
        this.currentUser = null;
        localStorage.removeItem('currentUser');
        this.updateUI();
        // Redirecionar para a página inicial
        window.location.href = 'index.html';
    }

    updateUI() {
        // Atualiza a interface (botões de login/logout/admin)
        const loginLink = document.getElementById('login-link');
        const userMenu = document.getElementById('user-menu');
        const userName = document.getElementById('user-name');
        const adminLink = document.querySelector('[data-page="admin"]');
        
        // Tenta achar links de admin no menu
        const adminHeaderLinks = document.querySelectorAll('a[href="admin.html"]');

        if (this.currentUser) {
            // LOGADO
            if (loginLink) loginLink.style.display = 'none';
            if (userMenu) {
                userMenu.classList.remove('hidden');
                // Alguns templates usam 'd-none' do Bootstrap
                userMenu.style.display = 'block'; 
                if (userName) userName.textContent = this.currentUser.nome;
            }

            // Se for admin, mostra botão de admin
            const ehAdmin = this.currentUser.tipo === 'admin' || this.currentUser.tipo === 'administrador';
            
            if (adminLink) adminLink.style.display = ehAdmin ? 'inline' : 'none';
            
            adminHeaderLinks.forEach(link => {
                link.style.display = ehAdmin ? 'inline' : 'none';
            });

        } else {
            // NÃO LOGADO
            if (loginLink) loginLink.style.display = 'inline';
            if (userMenu) {
                userMenu.classList.add('hidden');
                userMenu.style.display = 'none';
            }
            if (adminLink) adminLink.style.display = 'none';
            
            adminHeaderLinks.forEach(link => {
                link.style.display = 'none';
            });
        }
    }

    isLoggedIn() {
        return this.currentUser !== null;
    }

    isAdmin() {
        return this.currentUser && (this.currentUser.tipo === 'admin' || this.currentUser.tipo === 'administrador');
    }

    getCurrentUser() {
        return this.currentUser;
    }
}

// Instanciar o sistema
const authSystem = new AuthSystem();