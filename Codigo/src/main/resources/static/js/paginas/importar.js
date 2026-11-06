import { api } from "../api.js";
import { CAMPOS, camposFaltando, lerCsv, montarPosts, sugerirMapeamento } from "../csv.js";
import { gerarCsvExemplo } from "../exemplo.js";
import { montarMenu } from "../menu-app.js";
import { exigirLogin } from "../sessao.js";
import { comCarregamento, escapeHtml, formatarData, formatarNumero, mostrarAviso } from "../ui.js";

const usuario = exigirLogin();

const elementos = {
  etapas: document.getElementById("etapas"),
  soltar: document.getElementById("soltar"),
  arquivo: document.getElementById("arquivo"),
  usarExemplo: document.getElementById("usar-exemplo"),
  erroArquivo: document.getElementById("erro-arquivo"),
  etapaColunas: document.getElementById("etapa-colunas"),
  resumoArquivo: document.getElementById("resumo-arquivo"),
  mapeamento: document.getElementById("mapeamento"),
  previaCabecalho: document.getElementById("previa-cabecalho"),
  previaCorpo: document.getElementById("previa-corpo"),
  etapaImportar: document.getElementById("etapa-importar"),
  resumoImportacao: document.getElementById("resumo-importacao"),
  resultado: document.getElementById("resultado"),
  importar: document.getElementById("importar"),
  verPainel: document.getElementById("ver-painel"),
};

function contar(quantidade, singular, plural) {
  return `${formatarNumero(quantidade)} ${quantidade === 1 ? singular : plural}`;
}

const estado = { empresa: null, nomeArquivo: "", cabecalhos: [], linhas: [], mapeamento: {} };

if (usuario) {
  montarMenu(usuario, "importar.html");
  iniciar();
}

async function iniciar() {
  try {
    const empresas = await api("/api/empresas/minhas");
    const cnpj = new URLSearchParams(location.search).get("cnpj");
    estado.empresa = empresas.find((e) => e.cnpj === cnpj) ?? empresas[0];
  } catch (erro) {
    mostrarAviso(elementos.erroArquivo, erro.message);
    return;
  }

  if (!estado.empresa) {
    document.getElementById("sem-empresa").hidden = false;
    elementos.etapas.hidden = true;
    return;
  }
  document.getElementById("para-empresa").textContent =
    `Envie o CSV exportado do Meta Business Suite com os posts de ${estado.empresa.nome_fantasia}.`;
  elementos.verPainel.href = `painel.html?cnpj=${estado.empresa.cnpj}`;
}

// ---------- Etapa 1: arquivo ----------

function carregarTexto(nome, texto) {
  const { cabecalhos, linhas } = lerCsv(texto);
  if (!cabecalhos.length || !linhas.length) {
    mostrarAviso(elementos.erroArquivo, "O arquivo está vazio ou não é um CSV. Exporte de novo em formato CSV e tente outra vez.");
    return;
  }
  mostrarAviso(elementos.erroArquivo, "");
  Object.assign(estado, { nomeArquivo: nome, cabecalhos, linhas, mapeamento: sugerirMapeamento(cabecalhos) });
  mostrarColunas();
  elementos.etapaColunas.scrollIntoView({ behavior: "smooth", block: "start" });
}

async function lerArquivo(arquivo) {
  if (!arquivo) return;
  if (arquivo.size > 5 * 1024 * 1024) {
    mostrarAviso(elementos.erroArquivo, "O arquivo passa de 5 MB. Exporte um período menor e tente de novo.");
    return;
  }
  carregarTexto(arquivo.name, await arquivo.text());
}

elementos.arquivo.addEventListener("change", () => lerArquivo(elementos.arquivo.files[0]));

elementos.soltar.addEventListener("dragover", (e) => {
  e.preventDefault();
  elementos.soltar.classList.add("arrastando");
});
elementos.soltar.addEventListener("dragleave", () => elementos.soltar.classList.remove("arrastando"));
elementos.soltar.addEventListener("drop", (e) => {
  e.preventDefault();
  elementos.soltar.classList.remove("arrastando");
  lerArquivo(e.dataTransfer.files[0]);
});

elementos.usarExemplo.addEventListener("click", () => carregarTexto("exemplo-confeitaria.csv", gerarCsvExemplo()));

// ---------- Etapa 2: colunas ----------

function mostrarColunas() {
  elementos.etapaColunas.hidden = false;
  elementos.resumoArquivo.textContent =
    `${estado.nomeArquivo}: ${contar(estado.linhas.length, "linha", "linhas")} e ${contar(estado.cabecalhos.length, "coluna", "colunas")}. ` +
    "A Digitalize escolheu as colunas abaixo pelo nome; corrija se alguma estiver errada.";

  const opcoes = (selecionada) =>
    ['<option value="">Não usar</option>']
      .concat(
        estado.cabecalhos.map(
          (c) => `<option value="${escapeHtml(c)}"${c === selecionada ? " selected" : ""}>${escapeHtml(c)}</option>`
        )
      )
      .join("");

  elementos.mapeamento.innerHTML = CAMPOS.map(
    (campo) => `<div class="campo">
      <label for="coluna-${campo.chave}">${campo.rotulo}${campo.obrigatorio ? "" : " (opcional)"}</label>
      <select id="coluna-${campo.chave}" data-campo="${campo.chave}">${opcoes(estado.mapeamento[campo.chave])}</select>
    </div>`
  ).join("");

  elementos.mapeamento.querySelectorAll("select").forEach((select) =>
    select.addEventListener("change", () => {
      estado.mapeamento[select.dataset.campo] = select.value;
      atualizarResumo();
    })
  );

  elementos.previaCabecalho.innerHTML = `<tr>${estado.cabecalhos.map((c) => `<th>${escapeHtml(c)}</th>`).join("")}</tr>`;
  elementos.previaCorpo.innerHTML = estado.linhas
    .slice(0, 5)
    .map((l) => `<tr>${estado.cabecalhos.map((c) => `<td>${escapeHtml(l[c])}</td>`).join("")}</tr>`)
    .join("");

  elementos.etapaImportar.hidden = false;
  mostrarAviso(elementos.resultado, "");
  elementos.verPainel.hidden = true;
  atualizarResumo();
}

function atualizarResumo() {
  const faltando = camposFaltando(estado.mapeamento);
  if (faltando.length) {
    elementos.resumoImportacao.textContent = `Escolha a coluna de: ${faltando.join(", ")}.`;
    elementos.importar.disabled = true;
    return;
  }

  const { posts, ignoradas } = montarPosts(estado.linhas, estado.mapeamento);
  elementos.importar.disabled = posts.length === 0;
  elementos.resumoImportacao.textContent = posts.length
    ? `${contar(posts.length, "post pronto", "posts prontos")} para importar${
        ignoradas ? `; ${contar(ignoradas, "linha sem data válida será ignorada", "linhas sem data válida serão ignoradas")}` : ""
      }. Posts que já existem só têm as métricas atualizadas.`
    : "Nenhuma linha tem uma data reconhecível. Confira a coluna de data e hora.";
}

// ---------- Etapa 3: importar ----------

elementos.importar.addEventListener("click", () =>
  comCarregamento(elementos.importar, "Importando...", async () => {
    const { posts } = montarPosts(estado.linhas, estado.mapeamento);
    try {
      const r = await api(`/api/empresas/${estado.empresa.cnpj}/importacoes`, {
        metodo: "POST",
        corpo: { arquivo: estado.nomeArquivo, posts },
      });
      mostrarAviso(
        elementos.resultado,
        `Importação concluída: ${contar(r.posts_novos, "post novo", "posts novos")} e ${contar(r.posts_atualizados, "atualizado", "atualizados")}, ` +
          `publicados de ${formatarData(r.periodo_inicio)} a ${formatarData(r.periodo_fim)}.`,
        "sucesso"
      );
      elementos.verPainel.hidden = false;
      elementos.verPainel.focus();
    } catch (erro) {
      mostrarAviso(elementos.resultado, erro.message);
    }
  })
);
