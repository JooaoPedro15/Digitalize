// Sessão do usuário: o token JWT recebido no login fica no localStorage.

const CHAVE_TOKEN = "digitalize.token";
const CHAVE_USUARIO = "digitalize.usuario";

function ler(chave) {
  try {
    return localStorage.getItem(chave);
  } catch {
    return null;
  }
}

function expirado(token) {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const { exp } = JSON.parse(atob(base64));
    return !exp || exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export function salvarSessao({ token, usuario }) {
  localStorage.setItem(CHAVE_TOKEN, token);
  localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
}

export function encerrarSessao() {
  try {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
  } catch {
    // sem acesso ao localStorage: não há sessão para encerrar
  }
}

/** Token válido, ou null (e a sessão vencida é descartada). */
export function tokenAtual() {
  const token = ler(CHAVE_TOKEN);
  if (token && expirado(token)) {
    encerrarSessao();
    return null;
  }
  return token;
}

export function usuarioAtual() {
  if (!tokenAtual()) return null;
  try {
    return JSON.parse(ler(CHAVE_USUARIO));
  } catch {
    return null;
  }
}

export function ehAdmin(usuario = usuarioAtual()) {
  return usuario?.tipo === "admin";
}

/**
 * Garante que há alguém logado antes de mostrar uma página do painel.
 * Sem login, manda para a tela de entrar e volta para cá depois.
 */
export function exigirLogin({ admin = false } = {}) {
  const usuario = usuarioAtual();
  if (!usuario) {
    const voltar = encodeURIComponent(location.pathname.replace(/^\//, "") + location.search);
    location.replace(`entrar.html?voltar=${voltar}`);
    return null;
  }
  if (admin && !ehAdmin(usuario)) {
    location.replace("painel.html");
    return null;
  }
  return usuario;
}
