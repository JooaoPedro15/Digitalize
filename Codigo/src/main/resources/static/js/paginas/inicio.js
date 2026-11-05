import { api } from "../api.js";
import { agregar, desenhar, descreverMelhor, legenda } from "../grade-horarios.js";
import { engajamentoPorMil, gerarPostsExemplo } from "../exemplo.js";
import { salvarSessao, usuarioAtual } from "../sessao.js";
import { comCarregamento, escapeHtml, mostrarAviso } from "../ui.js";

// Grade de exemplo, montada com dados fictícios
const exemplo = agregar(
  gerarPostsExemplo().map((p) => ({ publicadoEm: p.publicadoEm, engajamento: engajamentoPorMil(p) }))
);
desenhar(document.getElementById("grade-exemplo"), exemplo, { animar: true });
document.getElementById("legenda-escala").append(legenda());
document.getElementById("melhor-exemplo").textContent = descreverMelhor(exemplo);

// Quem já entrou vê o atalho para o painel no lugar de "Entrar"
if (usuarioAtual()) {
  document.getElementById("navegacao").innerHTML =
    '<a href="#negocios" class="esconder-celular">Negócios</a>' +
    '<a href="painel.html" class="botao botao-principal">Ir para o painel</a>';
}

// Demonstração
const botaoDemo = document.getElementById("entrar-demo");
const erroDemo = document.getElementById("erro-demo");

api("/api/demo")
  .then(({ ativo }) => {
    if (!ativo) {
      botaoDemo.hidden = true;
      document.getElementById("nota-demo").hidden = true;
    }
  })
  .catch(() => {});

botaoDemo.addEventListener("click", () =>
  comCarregamento(botaoDemo, "Abrindo a demonstração...", async () => {
    try {
      salvarSessao(await api("/api/login/demo", { metodo: "POST" }));
      location.href = "painel.html";
    } catch (erro) {
      mostrarAviso(erroDemo, erro.message);
    }
  })
);

// Vitrine de negócios aprovados
const lista = document.getElementById("lista-negocios");
const busca = document.getElementById("busca");
let negocios = [];

function cidade(endereco) {
  return endereco ?? "Cidade não informada";
}

function mostrarNegocios() {
  const termo = busca.value.trim().toLowerCase();
  const filtrados = negocios.filter((n) =>
    [n.nome_fantasia, n.segmento, n.endereco].some((v) => String(v ?? "").toLowerCase().includes(termo))
  );

  if (!filtrados.length) {
    lista.innerHTML = `<li class="lista-vazia">${
      negocios.length ? "Nenhum negócio encontrado com essa busca." : "Ainda não há negócios aprovados."
    }</li>`;
    return;
  }

  lista.innerHTML = filtrados
    .map(
      (n) => `<li>
        <strong>${escapeHtml(n.nome_fantasia)}</strong>
        <span>${escapeHtml(n.segmento ?? "Segmento não informado")}</span>
        <span>${escapeHtml(cidade(n.endereco))}</span>
      </li>`
    )
    .join("");
}

busca.addEventListener("input", mostrarNegocios);

api("/api/empresas/publicas")
  .then((dados) => {
    negocios = dados;
    mostrarNegocios();
  })
  .catch(() => {
    lista.innerHTML = '<li class="lista-vazia">Não foi possível carregar os negócios agora. Recarregue a página para tentar de novo.</li>';
  });
