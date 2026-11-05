// Formulário de cadastro e edição do negócio, usado no painel.

import { escapeHtml, formatarCnpj } from "./ui.js";

const SEGMENTOS = [
  "Alimentação",
  "Beleza e estética",
  "Moda e acessórios",
  "Saúde e bem-estar",
  "Serviços",
  "Educação",
  "Comércio varejista",
  "Outro",
];

export function htmlFormularioEmpresa(empresa = {}, { textoBotao, edicao = false } = {}) {
  const valor = (campo) => escapeHtml(empresa[campo] ?? "");
  const opcoes = SEGMENTOS.map(
    (s) => `<option${s === empresa.segmento ? " selected" : ""}>${escapeHtml(s)}</option>`
  ).join("");

  return `
    <form class="formulario" id="form-empresa" novalidate>
      <div class="formulario-duplo">
        <div class="campo">
          <label for="nome_fantasia">Nome do negócio</label>
          <input id="nome_fantasia" name="nome_fantasia" required maxlength="120" value="${valor("nome_fantasia")}">
        </div>
        <div class="campo">
          <label for="razao_social">Razão social</label>
          <input id="razao_social" name="razao_social" required maxlength="120" value="${valor("razao_social")}">
        </div>
        <div class="campo">
          <label for="cnpj">CNPJ</label>
          <input id="cnpj" name="cnpj" inputmode="numeric" required maxlength="18" placeholder="00.000.000/0000-00"
            value="${escapeHtml(formatarCnpj(empresa.cnpj ?? ""))}"${edicao ? " readonly" : ""}>
          ${edicao ? "<small>O CNPJ não pode ser alterado.</small>" : ""}
        </div>
        <div class="campo">
          <label for="segmento">Segmento</label>
          <select id="segmento" name="segmento">
            <option value="">Escolha um segmento</option>
            ${opcoes}
          </select>
        </div>
        <div class="campo">
          <label for="endereco">Cidade e estado</label>
          <input id="endereco" name="endereco" maxlength="200" placeholder="Belo Horizonte - MG" value="${valor("endereco")}">
        </div>
        <div class="campo">
          <label for="email_contato">Email de contato</label>
          <input id="email_contato" name="email_contato" type="email" value="${valor("email_contato")}">
          <small>Aparece na vitrine pública.</small>
        </div>
      </div>
      <div class="aviso" id="erro-empresa" hidden></div>
      <div class="acoes">
        <button type="submit" class="botao botao-principal">${escapeHtml(textoBotao)}</button>
        ${edicao ? '<button type="button" class="botao botao-discreto" id="cancelar-edicao">Cancelar</button>' : ""}
      </div>
    </form>`;
}

/** Máscara simples de CNPJ enquanto a pessoa digita. */
export function aplicarMascaraCnpj(input) {
  input.addEventListener("input", () => {
    const d = input.value.replace(/\D/g, "").slice(0, 14);
    const partes = [d.slice(0, 2), d.slice(2, 5), d.slice(5, 8), d.slice(8, 12), d.slice(12, 14)];
    let texto = partes[0];
    if (partes[1]) texto += `.${partes[1]}`;
    if (partes[2]) texto += `.${partes[2]}`;
    if (partes[3]) texto += `/${partes[3]}`;
    if (partes[4]) texto += `-${partes[4]}`;
    input.value = texto;
  });
}

export function lerFormularioEmpresa(form) {
  const dados = Object.fromEntries(new FormData(form));
  return {
    cnpj: String(dados.cnpj ?? "").replace(/\D/g, ""),
    nome_fantasia: String(dados.nome_fantasia ?? "").trim(),
    razao_social: String(dados.razao_social ?? "").trim(),
    segmento: dados.segmento || null,
    endereco: String(dados.endereco ?? "").trim() || null,
    email_contato: String(dados.email_contato ?? "").trim() || null,
  };
}

/** Confere no navegador o que dá para conferir antes de chamar a API. */
export function validarEmpresa(dados) {
  if (!dados.nome_fantasia) return "Informe o nome do negócio.";
  if (!dados.razao_social) return "Informe a razão social.";
  if (dados.cnpj.length !== 14) return "O CNPJ precisa ter 14 dígitos.";
  return "";
}
