import { api } from "../api.js";
import { montarMenu } from "../menu-app.js";
import { exigirLogin } from "../sessao.js";
import { escapeHtml, formatarCnpj, mostrarAviso, seloStatus } from "../ui.js";

const usuario = exigirLogin({ admin: true });
const aviso = document.getElementById("aviso-admin");
const tabelaEmpresas = document.getElementById("tabela-empresas");
const filtros = document.getElementById("filtros");

let empresas = [];
let filtro = "todas";

if (usuario) {
  montarMenu(usuario, "admin.html");
  carregar();
}

async function carregar() {
  try {
    const [listaEmpresas, usuarios] = await Promise.all([api("/api/empresas"), api("/api/usuarios")]);
    empresas = listaEmpresas;
    mostrarEmpresas();
    mostrarUsuarios(usuarios);
  } catch (erro) {
    mostrarAviso(aviso, erro.message);
  }
}

function botoesDeAcao(empresa) {
  const cnpj = escapeHtml(empresa.cnpj);
  const nome = escapeHtml(empresa.nome_fantasia);
  const botoes = [];
  if (empresa.status !== "aprovada") {
    botoes.push(`<button type="button" class="botao botao-pequeno botao-principal" data-acao="aprovada" data-cnpj="${cnpj}" data-nome="${nome}">Aprovar</button>`);
  }
  if (empresa.status !== "rejeitada") {
    botoes.push(`<button type="button" class="botao botao-pequeno botao-secundario" data-acao="rejeitada" data-cnpj="${cnpj}" data-nome="${nome}">Rejeitar</button>`);
  }
  botoes.push(`<button type="button" class="botao botao-pequeno botao-perigo" data-acao="excluir" data-cnpj="${cnpj}" data-nome="${nome}">Excluir</button>`);
  return botoes.join("");
}

function mostrarEmpresas() {
  const visiveis = empresas.filter((e) => filtro === "todas" || e.status === filtro);
  tabelaEmpresas.innerHTML = visiveis.length
    ? visiveis
        .map(
          (e) => `<tr>
            <td><strong>${escapeHtml(e.nome_fantasia)}</strong><br><span class="nota">${escapeHtml(e.segmento ?? "Segmento não informado")}</span></td>
            <td class="numero">${escapeHtml(formatarCnpj(e.cnpj))}</td>
            <td>${escapeHtml(e.responsavel_email ?? "Sem responsável")}</td>
            <td>${seloStatus(e.status)}</td>
            <td class="acoes-tabela">${botoesDeAcao(e)}</td>
          </tr>`
        )
        .join("")
    : '<tr><td colspan="5">Nenhum negócio nesta situação.</td></tr>';
}

function mostrarUsuarios(usuarios) {
  document.getElementById("tabela-usuarios").innerHTML = usuarios
    .map(
      (u) => `<tr>
        <td>${escapeHtml(u.nome)}</td>
        <td>${escapeHtml(u.email)}</td>
        <td>${u.tipo === "admin" ? '<span class="selo selo-admin">Administrador</span>' : "Empreendedor"}</td>
      </tr>`
    )
    .join("");
}

filtros.addEventListener("click", (evento) => {
  const botao = evento.target.closest("[data-filtro]");
  if (!botao) return;
  filtro = botao.dataset.filtro;
  filtros.querySelectorAll("[data-filtro]").forEach((b) => {
    const ativo = b === botao;
    b.setAttribute("aria-pressed", String(ativo));
    b.classList.toggle("botao-secundario", ativo);
    b.classList.toggle("botao-discreto", !ativo);
  });
  mostrarEmpresas();
});

tabelaEmpresas.addEventListener("click", async (evento) => {
  const botao = evento.target.closest("[data-acao]");
  if (!botao) return;
  const { acao, cnpj, nome } = botao.dataset;

  try {
    if (acao === "excluir") {
      if (!confirm(`Excluir ${nome}? Os posts e importações do negócio também serão apagados.`)) return;
      await api(`/api/empresas/${cnpj}`, { metodo: "DELETE" });
      mostrarAviso(aviso, `${nome} foi excluído.`, "sucesso");
    } else {
      await api(`/api/empresas/${cnpj}/status`, { metodo: "PUT", corpo: { status: acao } });
      mostrarAviso(aviso, `${nome} foi ${acao === "aprovada" ? "aprovado" : "rejeitado"}.`, "sucesso");
    }
    await carregar();
  } catch (erro) {
    mostrarAviso(aviso, erro.message);
  }
});
