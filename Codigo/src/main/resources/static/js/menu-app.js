// Menu lateral das páginas logadas (painel, importação e administração).

import { ehAdmin, encerrarSessao } from "./sessao.js";
import { escapeHtml } from "./ui.js";

const PAGINAS = [
  { arquivo: "painel.html", nome: "Painel" },
  { arquivo: "importar.html", nome: "Importar métricas" },
  { arquivo: "admin.html", nome: "Administração", somenteAdmin: true },
];

export function montarMenu(usuario, paginaAtual) {
  const links = PAGINAS.filter((p) => !p.somenteAdmin || ehAdmin(usuario))
    .map((p) => {
      const atual = p.arquivo === paginaAtual ? ' aria-current="page"' : "";
      return `<a href="${p.arquivo}"${atual}>${p.nome}</a>`;
    })
    .join("");

  const menu = document.getElementById("menu");
  menu.innerHTML = `
    <a class="marca" href="./"><img src="img/logo.svg" alt="" width="34" height="34">Digitalize</a>
    <nav aria-label="Painel">${links}</nav>
    <div class="app-usuario">
      <span>${escapeHtml(usuario.nome)}<br>${escapeHtml(usuario.email)}</span>
      <button type="button" id="sair">Sair</button>
    </div>`;

  document.getElementById("sair").addEventListener("click", () => {
    encerrarSessao();
    location.href = "./";
  });
}
