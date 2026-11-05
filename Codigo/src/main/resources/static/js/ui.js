// Utilidades de interface usadas por todas as páginas.

const ESCAPES = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" };

/** Neutraliza HTML vindo do banco ou de arquivos (legendas, nomes...). */
export function escapeHtml(valor) {
  return String(valor ?? "").replace(/[&<>"']/g, (c) => ESCAPES[c]);
}

const numeroBr = new Intl.NumberFormat("pt-BR");

export function formatarNumero(valor) {
  return numeroBr.format(Math.round(Number(valor) || 0));
}

/** "2026-10-01" → "01/10/2026" */
export function formatarData(iso) {
  if (!iso) return "";
  const [ano, mes, dia] = String(iso).slice(0, 10).split("-");
  return `${dia}/${mes}/${ano}`;
}

/** "12345678000100" → "12.345.678/0001-00" */
export function formatarCnpj(cnpj) {
  const d = String(cnpj ?? "").replace(/\D/g, "");
  if (d.length !== 14) return d;
  return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-${d.slice(12)}`;
}

export function primeiroNome(nome) {
  return String(nome ?? "").trim().split(/\s+/)[0] || "";
}

const ROTULOS_STATUS = { pendente: "Aguardando aprovação", aprovada: "Aprovada", rejeitada: "Rejeitada" };

export function seloStatus(status) {
  const valor = String(status ?? "pendente").toLowerCase();
  return `<span class="selo selo-${escapeHtml(valor)}">${escapeHtml(ROTULOS_STATUS[valor] ?? valor)}</span>`;
}

/** Mostra (ou esconde, com texto vazio) uma mensagem num elemento .aviso */
export function mostrarAviso(elemento, texto, tipo = "erro") {
  elemento.className = `aviso aviso-${tipo}`;
  elemento.textContent = texto;
  elemento.hidden = !texto;
  if (texto && tipo === "erro") elemento.setAttribute("role", "alert");
}

/** Desabilita o botão e troca o texto enquanto a ação roda. */
export async function comCarregamento(botao, textoCarregando, acao) {
  const original = botao.textContent;
  botao.disabled = true;
  botao.textContent = textoCarregando;
  try {
    return await acao();
  } finally {
    botao.disabled = false;
    botao.textContent = original;
  }
}
