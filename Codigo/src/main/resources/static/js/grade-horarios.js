// Grade de horários: engajamento médio por dia da semana e hora do dia.

// Índices do Date.getDay(): 0 = domingo. A grade começa na segunda.
const NOMES_DIAS = ["Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"];
const SIGLAS_DIAS = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];
const ORDEM_DIAS = [1, 2, 3, 4, 5, 6, 0];

/** Horas mostradas na grade: das 6h às 23h. */
export const HORAS = Array.from({ length: 18 }, (_, i) => i + 6);

/** "2026-10-01T19:30:00" vira Date no horário local, sem conversão de fuso. */
function paraData(valor) {
  if (valor instanceof Date) return valor;
  const [data, hora = "00:00"] = String(valor).split("T");
  const [ano, mes, dia] = data.split("-").map(Number);
  const [h, m] = hora.split(":").map(Number);
  return new Date(ano, mes - 1, dia, h, m);
}

/**
 * Agrupa os posts por dia e hora.
 * @param posts lista de { publicadoEm, engajamento } (engajamento por mil visualizações)
 */
export function agregar(posts) {
  const celulas = new Map();
  const porHora = new Map();

  for (const post of posts) {
    const data = paraData(post.publicadoEm);
    const dia = data.getDay();
    const hora = data.getHours();
    if (!HORAS.includes(hora)) continue;

    const chave = `${dia}-${hora}`;
    const celula = celulas.get(chave) ?? { dia, hora, soma: 0, quantidade: 0 };
    celula.soma += post.engajamento;
    celula.quantidade += 1;
    celulas.set(chave, celula);

    const total = porHora.get(hora) ?? { soma: 0, quantidade: 0 };
    total.soma += post.engajamento;
    total.quantidade += 1;
    porHora.set(hora, total);
  }

  let maximo = 0;
  let melhor = null;
  for (const celula of celulas.values()) {
    celula.media = celula.soma / celula.quantidade;
    if (celula.media > maximo) {
      maximo = celula.media;
      melhor = celula;
    }
  }

  const melhoresHoras = [...porHora.entries()]
    .map(([hora, t]) => ({ hora, media: t.soma / t.quantidade }))
    .sort((a, b) => b.media - a.media)
    .slice(0, 3)
    .map((h) => h.hora);

  return { celulas, maximo, melhor, melhoresHoras, totalPosts: posts.length };
}

/** Nível de cor de 1 a 4, proporcional ao maior engajamento da grade. */
export function nivel(media, maximo) {
  if (!maximo) return 1;
  const proporcao = media / maximo;
  if (proporcao > 0.75) return 4;
  if (proporcao > 0.5) return 3;
  if (proporcao > 0.25) return 2;
  return 1;
}

export function descreverMelhor(agregado) {
  if (!agregado.melhor) return "";
  return `${NOMES_DIAS[agregado.melhor.dia]}, ${agregado.melhor.hora}h`;
}

function plural(n, singular, plural) {
  return `${n} ${n === 1 ? singular : plural}`;
}

/** Desenha a grade dentro do elemento informado. */
export function desenhar(elemento, agregado, { animar = false } = {}) {
  elemento.replaceChildren();
  elemento.classList.add("grade");
  elemento.classList.toggle("grade-animada", animar);
  elemento.setAttribute("role", "img");
  elemento.setAttribute(
    "aria-label",
    agregado.melhor
      ? `Grade de engajamento por dia e horário. Melhor horário: ${descreverMelhor(agregado)}.`
      : "Grade de engajamento por dia e horário, ainda sem posts."
  );

  let ordem = 0;
  for (const dia of ORDEM_DIAS) {
    const rotulo = document.createElement("span");
    rotulo.className = "grade-dia";
    rotulo.textContent = SIGLAS_DIAS[dia];
    elemento.append(rotulo);

    for (const hora of HORAS) {
      const celula = document.createElement("span");
      celula.className = "grade-celula";
      celula.style.setProperty("--ordem", String(ordem++));
      const dados = agregado.celulas.get(`${dia}-${hora}`);
      if (dados) {
        celula.dataset.nivel = String(nivel(dados.media, agregado.maximo));
        celula.title = `${NOMES_DIAS[dia]}, ${hora}h: ${plural(dados.quantidade, "post", "posts")}, ${Math.round(dados.media)} interações por mil visualizações`;
        if (dados === agregado.melhor) celula.dataset.melhor = "";
      } else {
        celula.title = `${NOMES_DIAS[dia]}, ${hora}h: sem posts`;
      }
      elemento.append(celula);
    }
  }

  elemento.append(document.createElement("span"));
  for (const hora of HORAS) {
    const rotulo = document.createElement("span");
    rotulo.className = "grade-hora";
    const destaque = agregado.melhoresHoras.includes(hora);
    if (destaque) rotulo.classList.add("grade-hora-destaque");
    // Em telas pequenas não cabem 18 rótulos: mostra de 3 em 3 horas e os melhores horários
    rotulo.textContent = destaque || hora % 3 === 0 ? `${hora}` : "";
    elemento.append(rotulo);
  }
}

/** Legenda da escala de cores. */
export function legenda() {
  const elemento = document.createElement("div");
  elemento.className = "grade-legenda";
  elemento.setAttribute("aria-hidden", "true");
  elemento.innerHTML =
    "<span>Menos engajamento</span>" +
    [1, 2, 3, 4].map((n) => `<i style="background: var(--grade-${n})"></i>`).join("") +
    "<span>Mais</span>";
  return elemento;
}
