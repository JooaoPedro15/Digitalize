// Sistema de autenticação
class AuthSystem {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    init() {
        // Verificar se há usuário logado no localStorage
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
            this.currentUser = JSON.parse(savedUser);
            this.updateUI();
        }
    }

    async login(email, senha) {
        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, senha })
            });

            const data = await response.json();

            if (data.success) {
                this.currentUser = data.usuario;
                localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
                this.updateUI();
                return { success: true, message: data.message };
            } else {
                return { success: false, message: data.error };
            }
        } catch (error) {
            console.error('Erro no login:', error);
            return { success: false, message: 'Erro de conexão' };
        }
    }

    async register(userData) {
        try {
            // Primeiro, verificar se o usuário já existe
            const existingUsers = await fetch('/api/usuarios');
            const users = await existingUsers.json();
            
            const userExists = users.find(u => u.email === userData.email);
            if (userExists) {
                return { success: false, message: 'Email já cadastrado' };
            }

            // Criar novo usuário
            const newUser = {
                id: Math.max(...users.map(u => u.id), 0) + 1,
                nome: userData.nome,
                email: userData.email,
                senha: userData.senha,
                tipo: 'usuario', // Por padrão, novos usuários são do tipo 'usuario'
                ativo: true,
                dataCadastro: new Date().toISOString()
            };

            // Simular salvamento (em um sistema real, isso seria feito no backend)
            const response = await fetch('/api/usuarios', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newUser)
            });

            if (response.ok) {
                return { success: true, message: 'Usuário cadastrado com sucesso' };
            } else {
                return { success: false, message: 'Erro ao cadastrar usuário' };
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
        window.location.href = '/';
    }

    updateUI() {
        const loginLink = document.getElementById('login-link');
        const userMenu = document.getElementById('user-menu');
        const userName = document.getElementById('user-name');
        const adminLink = document.querySelector('[data-page="admin"]');
        const cadastroLink = document.querySelector('[data-page="cadastro"]');

        if (this.currentUser) {
            // Usuário logado
            if (loginLink) loginLink.style.display = 'none';
            if (userMenu) {
                userMenu.classList.remove('hidden');
                if (userName) userName.textContent = this.currentUser.nome;
            }

            // Controle de acesso para administradores
            if (adminLink) {
                adminLink.style.display = this.currentUser.tipo === 'admin' ? 'inline' : 'none';
            }

            // Mostrar página Cadastro para qualquer usuário logado
            if (cadastroLink) {
                cadastroLink.style.display = 'inline';
            }

            // Controlar visibilidade do link Admin no cabeçalho
            const adminHeaderLinks = document.querySelectorAll('a[href="admin.html"]');
            adminHeaderLinks.forEach(link => {
                link.style.display = (this.currentUser.tipo === 'admin' || this.currentUser.tipo === 'administrador') ? 'inline' : 'none';
            });
        } else {
            // Usuário não logado
            if (loginLink) loginLink.style.display = 'inline';
            if (userMenu) userMenu.classList.add('hidden');
            if (adminLink) adminLink.style.display = 'none';
            if (cadastroLink) cadastroLink.style.display = 'none';

            // Ocultar link Admin no cabeçalho quando não logado
            const adminHeaderLinks = document.querySelectorAll('a[href="admin.html"]');
            adminHeaderLinks.forEach(link => {
                link.style.display = 'none';
            });
        }
    }

    isLoggedIn() {
        return this.currentUser !== null;
    }

    isAdmin() {
        return this.currentUser && this.currentUser.tipo === 'admin';
    }

    getCurrentUser() {
        return this.currentUser;
    }
}

// Instanciar o sistema de autenticação
const authSystem = new AuthSystem();

// Funções globais para compatibilidade com o código existente
function loginUser(email, senha) {
    return authSystem.login(email, senha);
}

function addUser(nome, login, senha, email) {
    return authSystem.register({ nome, email, senha });
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Logout button
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            authSystem.logout();
        });
    }

    // Verificar acesso a páginas restritas
    const currentPage = window.location.pathname;
    if (currentPage.includes('admin') && !authSystem.isAdmin()) {
        alert('Acesso negado. Apenas administradores podem acessar esta página.');
        window.location.href = '/';
    }
});

