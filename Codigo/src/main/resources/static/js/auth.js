class AuthSystem {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    init() {
        try {
            const savedUser = localStorage.getItem('currentUser');
            this.currentUser = savedUser ? JSON.parse(savedUser) : null;
        } catch (error) {
            localStorage.removeItem('currentUser');
            this.currentUser = null;
        }

        this.updateUI();
    }

    async login(email, senha) {
        try {
            const response = await fetch(`${window.location.origin}/api/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, senha })
            });

            const data = await this.readJson(response);
            const usuario = data.usuario || data;

            if (response.ok && usuario && usuario.email) {
                this.currentUser = usuario;
                localStorage.setItem('currentUser', JSON.stringify(usuario));
                this.updateUI();
                return { success: true, message: data.message || 'Login realizado com sucesso' };
            }

            return { success: false, message: data.error || 'Email ou senha incorretos' };
        } catch (error) {
            console.error('Erro no login:', error);
            return { success: false, message: 'Erro de conexao com o servidor' };
        }
    }

    async register(userData) {
        try {
            const response = await fetch(`${window.location.origin}/api/usuarios`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    nome: userData.nome,
                    email: userData.email,
                    senha: userData.senha
                })
            });

            const data = await this.readJson(response);
            if (response.ok) {
                return { success: true, message: data.message || 'Usuario cadastrado com sucesso' };
            }

            return { success: false, message: data.error || 'Erro ao cadastrar usuario' };
        } catch (error) {
            console.error('Erro no registro:', error);
            return { success: false, message: 'Erro de conexao com o servidor' };
        }
    }

    async readJson(response) {
        try {
            return await response.json();
        } catch (error) {
            return {};
        }
    }

    logout() {
        this.currentUser = null;
        localStorage.removeItem('currentUser');
        this.updateUI();
        window.location.href = '/';
    }

    updateUI() {
        const loginLink = document.getElementById('login-link');
        const userMenu = document.getElementById('user-menu');
        const userName = document.getElementById('user-name');
        const adminLink = document.querySelector('[data-page="admin"]');
        const cadastroLink = document.querySelector('[data-page="cadastro"]');
        const adminHeaderLinks = document.querySelectorAll('a[href="admin.html"]');

        if (this.currentUser) {
            if (loginLink) loginLink.style.display = 'none';
            if (userMenu) userMenu.classList.remove('hidden');
            if (userName) userName.textContent = this.currentUser.nome || this.currentUser.email;
            if (adminLink) adminLink.style.display = this.isAdmin() ? 'inline' : 'none';
            if (cadastroLink) cadastroLink.style.display = 'inline';
            adminHeaderLinks.forEach(link => {
                link.style.display = this.isAdmin() ? 'inline' : 'none';
            });
            return;
        }

        if (loginLink) loginLink.style.display = 'inline';
        if (userMenu) userMenu.classList.add('hidden');
        if (adminLink) adminLink.style.display = 'none';
        if (cadastroLink) cadastroLink.style.display = 'none';
        adminHeaderLinks.forEach(link => {
            link.style.display = 'none';
        });
    }

    isLoggedIn() {
        return this.currentUser !== null;
    }

    isAdmin() {
        return this.currentUser && (
            this.currentUser.tipo === 'admin' ||
            this.currentUser.tipo === 'administrador'
        );
    }

    getCurrentUser() {
        return this.currentUser;
    }
}

const authSystem = new AuthSystem();

if (typeof window !== 'undefined') {
    window.authSystem = authSystem;
}

function loginUser(email, senha) {
    return authSystem.login(email, senha);
}

function addUser(nome, login, senha, email) {
    return authSystem.register({ nome, email, senha });
}

document.addEventListener('DOMContentLoaded', function() {
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            authSystem.logout();
        });
    }

    if (window.location.pathname.includes('admin') && !authSystem.isAdmin()) {
        alert('Acesso negado. Apenas administradores podem acessar esta pagina.');
        window.location.href = '/';
    }
});
