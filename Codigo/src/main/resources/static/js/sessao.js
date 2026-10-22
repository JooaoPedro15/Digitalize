/*
 * Sessão do usuário no front-end.
 *
 * Guarda o token JWT recebido no login e o envia no cabeçalho Authorization
 * de todas as chamadas fetch para a própria API. Se o token vencer ou for
 * recusado (401), a sessão é encerrada e o usuário volta para o login.
 *
 * Deve ser o primeiro script de cada página.
 */
(function () {
    const CHAVE_TOKEN = 'token';
    const CHAVE_USUARIO = 'currentUser';
    const PAGINAS_PUBLICAS = ['/', '/index.html', '/login.html'];

    function ler(chave) {
        try {
            return localStorage.getItem(chave);
        } catch (e) {
            return null;
        }
    }

    function tokenExpirado(token) {
        try {
            const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            const conteudo = JSON.parse(atob(base64));
            return !conteudo.exp || conteudo.exp * 1000 <= Date.now();
        } catch (e) {
            return true;
        }
    }

    function salvar(respostaLogin) {
        localStorage.setItem(CHAVE_TOKEN, respostaLogin.token);
        localStorage.setItem(CHAVE_USUARIO, JSON.stringify(respostaLogin.usuario));
    }

    function encerrar() {
        localStorage.removeItem(CHAVE_TOKEN);
        localStorage.removeItem(CHAVE_USUARIO);
    }

    function irParaLogin() {
        if (!PAGINAS_PUBLICAS.includes(window.location.pathname)) {
            window.location.href = 'login.html?sessao=expirada';
        }
    }

    // Sessão vencida, ou usuário salvo sem token (login feito antes desta versão): descarta.
    const tokenAtual = ler(CHAVE_TOKEN);
    if ((tokenAtual && tokenExpirado(tokenAtual)) || (!tokenAtual && ler(CHAVE_USUARIO))) {
        encerrar();
    }

    const fetchOriginal = window.fetch.bind(window);

    window.fetch = async function (recurso, opcoes = {}) {
        const endereco = new URL(recurso instanceof Request ? recurso.url : String(recurso), window.location.href);
        const mesmaOrigem = endereco.origin === window.location.origin;
        const token = ler(CHAVE_TOKEN);

        if (mesmaOrigem && token) {
            const cabecalhos = new Headers(opcoes.headers || (recurso instanceof Request ? recurso.headers : undefined));
            if (!cabecalhos.has('Authorization')) {
                cabecalhos.set('Authorization', 'Bearer ' + token);
            }
            opcoes = { ...opcoes, headers: cabecalhos };
        }

        const resposta = await fetchOriginal(recurso, opcoes);

        if (mesmaOrigem && token && resposta.status === 401) {
            encerrar();
            irParaLogin();
        }
        return resposta;
    };

    window.Sessao = { salvar, encerrar };
})();
