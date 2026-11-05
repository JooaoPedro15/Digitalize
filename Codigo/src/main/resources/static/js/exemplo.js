// Dados de exemplo de uma confeitaria: usados na grade da página inicial
// e no arquivo CSV de exemplo da tela de importação.

const LEGENDAS = [
  "Bastidores da fornada de bolo de pote",
  "Promoção: 2 brigadeiros gourmet por R$ 10",
  "A história da nossa confeitaria",
  "Receita rápida de brigadeiro de colher",
  "Cliente do mês: obrigada pela parceria!",
  "Sabor novo: pistache com frutas vermelhas",
  "Como montamos uma caixa de presente",
  "Enquete: qual sabor na próxima fornada?",
  "Como conservar seus doces por mais tempo",
  "Tour pela cozinha antes de abrir",
  "Encomendas abertas para o fim de semana",
  "Antes e depois do bolo de aniversário",
];

// Horários em que uma confeitaria pequena costuma postar
const HORAS = [8, 9, 11, 12, 13, 15, 17, 18, 19, 20, 21, 22];

/** Gerador pseudoaleatório com semente: o exemplo sai igual em toda visita. */
function sorteador(semente) {
  let estado = semente;
  return () => {
    estado = (estado * 1664525 + 1013904223) % 4294967296;
    return estado / 4294967296;
  };
}

const dois = (n) => String(n).padStart(2, "0");

/**
 * Posts dos últimos dias com um padrão realista: noite e fim de semana engajam mais.
 * Cada post: { publicadoEm, legenda, visualizacoes, alcance, curtidas, comentarios, compartilhamentos, salvamentos }
 */
export function gerarPostsExemplo({ hoje = new Date(), dias = 42, porDia = 2 } = {}) {
  const aleatorio = sorteador(7);
  const posts = [];

  for (let d = dias; d >= 1; d--) {
    const data = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - d);
    const diaSemana = data.getDay();
    const horasDoDia = [...HORAS].sort(() => aleatorio() - 0.5).slice(0, porDia);

    for (const hora of horasDoDia) {
      const noite = hora >= 18 && hora <= 21;
      const fimDeSemana = diaSemana === 5 || diaSemana === 6;
      const fator = 1 + (noite ? 0.9 : 0) + (fimDeSemana ? 0.45 : 0) + (hora === 12 ? 0.3 : 0);

      const visualizacoes = Math.round(700 + aleatorio() * 900);
      const curtidas = Math.round((visualizacoes / 22) * fator * (0.8 + aleatorio() * 0.4));
      posts.push({
        publicadoEm: `${data.getFullYear()}-${dois(data.getMonth() + 1)}-${dois(data.getDate())}T${dois(hora)}:${dois(Math.floor(aleatorio() * 60))}:00`,
        legenda: LEGENDAS[posts.length % LEGENDAS.length],
        visualizacoes,
        alcance: Math.round(visualizacoes * 0.8),
        curtidas,
        comentarios: Math.round(curtidas / 7),
        compartilhamentos: Math.round(curtidas / 12),
        salvamentos: Math.round(curtidas / 6),
      });
    }
  }
  return posts;
}

/** Mesmo cálculo do backend: (curtidas + comentários + compartilhamentos) por mil visualizações. */
export function engajamentoPorMil(post) {
  if (!post.visualizacoes) return 0;
  return Math.floor(((post.curtidas + post.comentarios + post.compartilhamentos) * 1000) / post.visualizacoes);
}

/** CSV no formato de uma exportação do Meta Business Suite em português (separado por ";"). */
export function gerarCsvExemplo(opcoes) {
  const aspas = (texto) => `"${String(texto).replace(/"/g, '""')}"`;
  const linhas = gerarPostsExemplo({ dias: 30, ...opcoes }).map((p) => {
    const [data, hora] = p.publicadoEm.split("T");
    const [ano, mes, dia] = data.split("-");
    return [
      `${dia}/${mes}/${ano} ${hora.slice(0, 5)}`,
      aspas(p.legenda),
      p.visualizacoes,
      p.alcance,
      p.curtidas,
      p.comentarios,
      p.compartilhamentos,
      p.salvamentos,
    ].join(";");
  });
  const cabecalho = "Horário de publicação;Descrição;Impressões;Alcance;Curtidas;Comentários;Compartilhamentos;Salvamentos";
  return [cabecalho, ...linhas].join("\n");
}
