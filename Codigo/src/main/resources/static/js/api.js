// Cliente da API: envia o token, converte JSON e transforma erros em mensagens.

import { encerrarSessao, tokenAtual } from "./sessao.js";

export class ErroApi extends Error {
  constructor(status, mensagem) {
    super(mensagem);
    this.status = status;
  }
}

export async function api(caminho, { metodo = "GET", corpo } = {}) {
  const cabecalhos = { Accept: "application/json" };
  if (corpo !== undefined) cabecalhos["Content-Type"] = "application/json";

  const token = tokenAtual();
  if (token) cabecalhos.Authorization = `Bearer ${token}`;

  let resposta;
  try {
    resposta = await fetch(caminho, {
      method: metodo,
      headers: cabecalhos,
      body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
    });
  } catch {
    throw new ErroApi(0, "Não foi possível falar com o servidor. Confira a conexão e tente de novo.");
  }

  const dados = await resposta.json().catch(() => null);

  // Token recusado (vencido ou inválido): encerra a sessão e pede login de novo
  if (resposta.status === 401 && token) {
    encerrarSessao();
    location.replace("entrar.html?sessao=expirada");
  }

  if (!resposta.ok) {
    throw new ErroApi(resposta.status, dados?.mensagem ?? "Não foi possível concluir a ação. Tente de novo.");
  }
  return dados;
}
