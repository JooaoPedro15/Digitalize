import { api } from "../api.js";
import { salvarSessao, usuarioAtual } from "../sessao.js";
import { comCarregamento, mostrarAviso } from "../ui.js";

const parametros = new URLSearchParams(location.search);

/** Para onde ir depois de entrar: a página que pediu login, ou o painel. */
function destino(usuario) {
  const voltar = parametros.get("voltar");
  // Só aceita páginas deste site, nunca um endereço externo
  if (voltar && /^[a-z-]+\.html(\?[\w=&%.-]*)?$/.test(voltar)) return voltar;
  return usuario.tipo === "admin" ? "admin.html" : "painel.html";
}

function entrarCom(resposta) {
  salvarSessao(resposta);
  location.replace(destino(resposta.usuario));
}

const jaLogado = usuarioAtual();
if (jaLogado) location.replace(destino(jaLogado));

if (parametros.get("sessao") === "expirada") {
  document.getElementById("aviso-sessao").hidden = false;
}

// Abas
const abas = {
  entrar: [document.getElementById("aba-entrar"), document.getElementById("form-entrar")],
  criar: [document.getElementById("aba-criar"), document.getElementById("form-criar")],
};

function mostrarAba(nome) {
  for (const [chave, [aba, painel]] of Object.entries(abas)) {
    const ativa = chave === nome;
    aba.setAttribute("aria-selected", String(ativa));
    painel.hidden = !ativa;
  }
  abas[nome][1].querySelector("input").focus();
}

abas.entrar[0].addEventListener("click", () => mostrarAba("entrar"));
abas.criar[0].addEventListener("click", () => mostrarAba("criar"));
if (parametros.get("aba") === "criar") mostrarAba("criar");

// Entrar
const formEntrar = document.getElementById("form-entrar");
const erroEntrar = document.getElementById("erro-entrar");

formEntrar.addEventListener("submit", (evento) => {
  evento.preventDefault();
  const email = formEntrar.email.value.trim();
  const senha = formEntrar.senha.value;
  if (!email || !senha) {
    mostrarAviso(erroEntrar, "Preencha email e senha.");
    return;
  }

  comCarregamento(formEntrar.querySelector("button"), "Entrando...", async () => {
    try {
      entrarCom(await api("/api/login", { metodo: "POST", corpo: { email, senha } }));
    } catch (erro) {
      mostrarAviso(erroEntrar, erro.message);
    }
  });
});

// Criar conta: depois de cadastrar, já entra direto
const formCriar = document.getElementById("form-criar");
const erroCriar = document.getElementById("erro-criar");

formCriar.addEventListener("submit", (evento) => {
  evento.preventDefault();
  const nome = formCriar.nome.value.trim();
  const email = formCriar.email.value.trim();
  const senha = formCriar.senha.value;
  if (!nome || !email || senha.length < 6) {
    mostrarAviso(erroCriar, "Preencha nome, email e uma senha com pelo menos 6 caracteres.");
    return;
  }

  comCarregamento(formCriar.querySelector("button"), "Criando conta...", async () => {
    try {
      await api("/api/usuarios", { metodo: "POST", corpo: { nome, email, senha } });
      entrarCom(await api("/api/login", { metodo: "POST", corpo: { email, senha } }));
    } catch (erro) {
      mostrarAviso(erroCriar, erro.message);
    }
  });
});

// Demonstração
const botaoDemo = document.getElementById("entrar-demo");
const erroDemo = document.getElementById("erro-demo");

api("/api/demo")
  .then(({ ativo }) => {
    document.getElementById("caixa-demo").hidden = !ativo;
  })
  .catch(() => {});

botaoDemo.addEventListener("click", () =>
  comCarregamento(botaoDemo, "Abrindo a demonstração...", async () => {
    try {
      entrarCom(await api("/api/login/demo", { metodo: "POST" }));
    } catch (erro) {
      mostrarAviso(erroDemo, erro.message);
    }
  })
);
