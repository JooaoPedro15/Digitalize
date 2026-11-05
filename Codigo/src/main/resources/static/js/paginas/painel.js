import { api } from "../api.js";
import {
  aplicarMascaraCnpj,
  htmlFormularioEmpresa,
  lerFormularioEmpresa,
  validarEmpresa,
} from "../formulario-empresa.js";
import { agregar, desenhar, descreverMelhor, legenda } from "../grade-horarios.js";
import { montarMenu } from "../menu-app.js";
import { ehAdmin, exigirLogin } from "../sessao.js";
import {
  comCarregamento,
  escapeHtml,
  formatarCnpj,
  formatarData,
  formatarNumero,
  mostrarAviso,
  primeiroNome,
  seloStatus,
} from "../ui.js";

const usuario = exigirLogin();
const conteudo = document.getElementById("conteudo");
const DEMONSTRACAO = "demo@digitalize.app";

if (usuario) {
  montarMenu(usuario, "painel.html");
  carregar();
}

async function carregar() {
  try {
    const empresas = await api("/api/empresas/minhas");
    const cnpjPedido = new URLSearchParams(location.search).get("cnpj");
    const empresa = empresas.find((e) => e.cnpj === cnpjPedido) ?? empresas[0];
    if (empresa) {
      await mostrarEmpresa(empresa, empresas);
    } else {
      mostrarCadastro();
    }
  } catch (erro) {
    conteudo.innerHTML = `<div class="aviso aviso-erro" role="alert">${escapeHtml(erro.message)}</div>`;
  }
}

// ---------- Sem empresa: cadastro do negócio ----------

function mostrarCadastro() {
  const atalhoAdmin = ehAdmin(usuario)
    ? '<p class="aviso">Você é administrador. Para aprovar empresas, use a página <a href="admin.html">Administração</a>.</p>'
    : "";

  conteudo.innerHTML = `
    <header class="cabecalho-pagina">
      <div>
        <h1>Olá, ${escapeHtml(primeiroNome(usuario.nome))}</h1>
        <p>Cadastre o seu negócio para importar as métricas do Instagram e receber o guia de postagem.</p>
      </div>
    </header>
    ${atalhoAdmin}
    <section class="bloco" aria-labelledby="titulo-cadastro">
      <header><h2 id="titulo-cadastro">Cadastre o seu negócio</h2></header>
      ${htmlFormularioEmpresa({}, { textoBotao: "Cadastrar negócio" })}
    </section>`;

  const form = document.getElementById("form-empresa");
  const erro = document.getElementById("erro-empresa");
  aplicarMascaraCnpj(form.cnpj);

  form.addEventListener("submit", (evento) => {
    evento.preventDefault();
    const dados = lerFormularioEmpresa(form);
    const problema = validarEmpresa(dados);
    if (problema) {
      mostrarAviso(erro, problema);
      return;
    }
    comCarregamento(form.querySelector("[type=submit]"), "Cadastrando...", async () => {
      try {
        await api("/api/empresas", { metodo: "POST", corpo: dados });
        await carregar();
      } catch (e) {
        mostrarAviso(erro, e.message);
      }
    });
  });
}

// ---------- Com empresa: grade, guia, importações e dados ----------

async function mostrarEmpresa(empresa, empresas) {
  const base = `/api/empresas/${empresa.cnpj}`;
  const [posts, guia, importacoes] = await Promise.all([
    api(`${base}/posts`),
    api(`${base}/guia`),
    api(`${base}/importacoes`),
  ]);

  const seletor =
    empresas.length > 1
      ? `<label class="oculto-visualmente" for="trocar-empresa">Trocar de negócio</label>
         <select id="trocar-empresa" class="seletor-empresa">${empresas
           .map((e) => `<option value="${e.cnpj}"${e.cnpj === empresa.cnpj ? " selected" : ""}>${escapeHtml(e.nome_fantasia)}</option>`)
           .join("")}</select>`
      : `<strong>${escapeHtml(empresa.nome_fantasia)}</strong>`;

  const avisoStatus = {
    pendente: '<p class="aviso aviso-atencao">Seu negócio está aguardando aprovação para aparecer na vitrine pública. Enquanto isso, você já pode importar métricas e usar o guia.</p>',
    rejeitada: '<p class="aviso aviso-erro">O cadastro do seu negócio não foi aprovado para a vitrine pública. Revise os dados abaixo; ao salvar, ele volta para análise.</p>',
  }[empresa.status] ?? "";

  conteudo.innerHTML = `
    <header class="cabecalho-pagina">
      <div>
        <h1>Olá, ${escapeHtml(primeiroNome(usuario.nome))}</h1>
        <div class="linha-empresa">${seletor} ${seloStatus(empresa.status)}</div>
      </div>
      <a class="botao botao-principal" href="importar.html?cnpj=${empresa.cnpj}">Importar métricas</a>
    </header>
    ${avisoStatus}
    ${posts.length ? htmlResultados(posts) : htmlSemPosts(empresa)}
    ${posts.length ? htmlGuia(guia) : ""}
    ${htmlImportacoes(importacoes)}
    <section class="bloco" aria-labelledby="titulo-dados" id="bloco-dados"></section>`;

  document.getElementById("trocar-empresa")?.addEventListener("change", (e) => {
    location.search = `?cnpj=${e.target.value}`;
  });

  if (posts.length) {
    const agregado = agregar(posts.map((p) => ({ publicadoEm: p.publicado_em, engajamento: p.engajamento_por_mil })));
    desenhar(document.getElementById("grade"), agregado);
    document.getElementById("grade-legenda").append(legenda());
    document.getElementById("melhor-combinacao").textContent = descreverMelhor(agregado);
    document.getElementById("numero-horario").textContent =
      agregado.melhoresHoras.length ? `${agregado.melhoresHoras[0]}h` : "Sem dados";
  }

  mostrarDados(empresa);
}

function htmlResultados(posts) {
  const media = posts.reduce((soma, p) => soma + p.engajamento_por_mil, 0) / posts.length;
  return `
    <dl class="numeros">
      <div><dt>Posts nos últimos 30 dias</dt><dd>${formatarNumero(posts.length)}</dd></div>
      <div><dt>Engajamento médio</dt><dd>${formatarNumero(media)} <small>interações a cada mil visualizações</small></dd></div>
      <div><dt>Melhor horário para postar</dt><dd id="numero-horario"></dd></div>
    </dl>
    <section class="bloco" aria-labelledby="titulo-grade">
      <header>
        <h2 id="titulo-grade">Quando o seu público mais interage</h2>
        <p>Cada quadrado é um horário. Quanto mais escuro, mais interações por visualização.</p>
      </header>
      <div id="grade"></div>
      <div id="grade-legenda"></div>
      <p class="melhor-horario"><span aria-hidden="true"></span><span>Melhor combinação de dia e horário: <strong id="melhor-combinacao"></strong></span></p>
    </section>`;
}

function htmlSemPosts(empresa) {
  return `
    <section class="bloco" aria-labelledby="titulo-vazio">
      <header><h2 id="titulo-vazio">Importe as métricas do Instagram</h2></header>
      <p>Ainda não há posts dos últimos 30 dias. Importe o CSV exportado do Meta Business Suite para ver em quais dias e horários o seu público mais interage e receber o guia de postagem.</p>
      <div class="acoes"><a class="botao botao-principal" href="importar.html?cnpj=${empresa.cnpj}">Importar métricas</a></div>
    </section>`;
}

function lista(itens) {
  return itens?.length ? `<ul>${itens.map((i) => `<li>${escapeHtml(i)}</li>`).join("")}</ul>` : "<p>Sem itens.</p>";
}

function htmlGuia(guia) {
  const horarios = (guia.melhores_horarios ?? []).map((h) => `<span>${escapeHtml(h)}</span>`).join("");
  const sugestoes = (guia.sugestoes_posts ?? [])
    .map(
      (s) => `<div class="sugestao">
        <strong>${escapeHtml(s.titulo)}</strong>
        <p>${escapeHtml(s.descricao_legendada)}</p>
        <p>${escapeHtml(s.justificativa)}</p>
      </div>`
    )
    .join("");

  return `
    <section class="bloco" aria-labelledby="titulo-guia">
      <header>
        <h2 id="titulo-guia">Guia de postagem</h2>
        <p>Gerado a partir dos posts dos últimos 30 dias.</p>
      </header>
      <div class="guia">
        <p class="guia-resumo">${escapeHtml(guia.resumo_periodo)}</p>
        <section><h3>Melhores horários</h3><div class="guia-horarios">${horarios || "<p>Sem dados.</p>"}</div></section>
        <section><h3>O que funcionou</h3>${lista(guia.top_3_insights)}</section>
        <section><h3>Tom de voz</h3>${lista(guia.diretrizes_tom_voz)}</section>
        <section><h3>O que evitar</h3>${lista(guia.o_que_evitar)}</section>
        <section class="guia-largo"><h3>Ideias para os próximos posts</h3>${sugestoes || "<p>Sem sugestões.</p>"}</section>
      </div>
    </section>`;
}

function htmlImportacoes(importacoes) {
  const linhas = importacoes
    .map(
      (i) => `<tr>
        <td>${escapeHtml(i.arquivo)}</td>
        <td class="numero">${formatarData(i.periodo_inicio)} a ${formatarData(i.periodo_fim)}</td>
        <td class="numero">${formatarNumero(i.total_posts)}</td>
        <td class="numero">${formatarData(i.criado_em)}</td>
      </tr>`
    )
    .join("");

  return `
    <section class="bloco" aria-labelledby="titulo-importacoes">
      <header><h2 id="titulo-importacoes">Arquivos importados</h2></header>
      ${
        importacoes.length
          ? `<div class="tabela-envelope"><table>
              <thead><tr><th>Arquivo</th><th>Período dos posts</th><th>Posts</th><th>Importado em</th></tr></thead>
              <tbody>${linhas}</tbody>
            </table></div>`
          : "<p>Nenhum arquivo importado ainda.</p>"
      }
    </section>`;
}

function mostrarDados(empresa) {
  const bloco = document.getElementById("bloco-dados");
  const podeEditar = usuario.email !== DEMONSTRACAO;

  bloco.innerHTML = `
    <header>
      <h2 id="titulo-dados">Dados do negócio</h2>
      ${podeEditar ? '<button type="button" class="botao botao-secundario botao-pequeno" id="editar-dados">Editar dados</button>' : ""}
    </header>
    <dl class="formulario-duplo dados-empresa">
      ${[
        ["Razão social", empresa.razao_social],
        ["CNPJ", formatarCnpj(empresa.cnpj)],
        ["Segmento", empresa.segmento],
        ["Cidade e estado", empresa.endereco],
        ["Email de contato", empresa.email_contato],
      ]
        .map(([rotulo, valor]) => `<div><dt>${rotulo}</dt><dd>${escapeHtml(valor || "Não informado")}</dd></div>`)
        .join("")}
    </dl>
    ${podeEditar ? "" : '<p class="nota">A conta de demonstração não altera cadastros. Crie uma conta para cadastrar o seu próprio negócio.</p>'}`;

  document.getElementById("editar-dados")?.addEventListener("click", () => editarDados(empresa));
}

function editarDados(empresa) {
  const bloco = document.getElementById("bloco-dados");
  bloco.innerHTML = `
    <header><h2 id="titulo-dados">Editar dados do negócio</h2></header>
    <p class="aviso aviso-atencao">Ao salvar, o negócio volta para aprovação antes de reaparecer na vitrine.</p>
    ${htmlFormularioEmpresa(empresa, { textoBotao: "Salvar dados", edicao: true })}`;

  const form = document.getElementById("form-empresa");
  const erro = document.getElementById("erro-empresa");
  form.nome_fantasia.focus();
  document.getElementById("cancelar-edicao").addEventListener("click", () => mostrarDados(empresa));

  form.addEventListener("submit", (evento) => {
    evento.preventDefault();
    const dados = lerFormularioEmpresa(form);
    const problema = validarEmpresa(dados);
    if (problema) {
      mostrarAviso(erro, problema);
      return;
    }
    comCarregamento(form.querySelector("[type=submit]"), "Salvando...", async () => {
      try {
        await api(`/api/empresas/${empresa.cnpj}`, { metodo: "PUT", corpo: dados });
        await carregar();
      } catch (e) {
        mostrarAviso(erro, e.message);
      }
    });
  });
}
