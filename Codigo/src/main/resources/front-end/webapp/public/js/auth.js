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
            // Obtém a origem da página (protocolo + domínio + porta) para montar
            // a base da API. Isso garante que a requisição seja enviada ao mesmo
            // servidor que está servindo o front‑end, independentemente da porta.
            const API_BASE_URL = window.location.origin;
            const response = await fetch(`${API_BASE_URL}/api/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, senha })
            });

            // Decodifica a resposta como JSON. Se não for possível converter, a
            // exceção será capturada pelo bloco catch abaixo.
            const data = await response.json();

            const usuario = data.usuario || data;

            if (response.ok && usuario && usuario.email) {
                this.currentUser = usuario;
                localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
                this.updateUI();
                return { success: true, message: data.message || 'Login realizado com sucesso' };
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
            // Usa a mesma origem da página para construir a URL base. Isso mantém
            // as chamadas de cadastro consistentes com o host e a porta do front‑end.
            const API_BASE_URL = window.location.origin;

            // Primeiro, verificar se o usuário já existe
            const existingUsersResponse = await fetch(`${API_BASE_URL}/api/usuarios`);
            const users = await existingUsersResponse.json();

            const userExists = users.find(u => u.email === userData.email);
            if (userExists) {
                return { success: false, message: 'Email já cadastrado' };
            }

            // Criar novo usuário. A lógica de geração de ID e hash da senha deve
            // permanecer no back‑end para manter a segurança; no entanto, para
            // compatibilidade com a API atual que armazena a senha em texto puro,
            // continuamos enviando os dados sem alterações. O Spark irá processar
            // e salvar a senha de forma segura.
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
                return { success: true, message: 'Usuário cadastrado com sucesso' };
            } else {
                // Tenta ler a mensagem de erro retornada pelo backend, caso exista
                try {
                    const err = await response.json();
                    return { success: false, message: err.error || 'Erro ao cadastrar usuário' };
                } catch (err) {
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
