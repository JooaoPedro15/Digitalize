// Leitura do CSV de métricas e conversão para o formato da API.

/** Campos que a Digitalize entende, com palavras que costumam aparecer no cabeçalho. */
export const CAMPOS = [
  { chave: "publicadoEm", rotulo: "Data e hora da publicação", obrigatorio: true,
    dicas: ["horario de publicacao", "publicado", "data", "date", "time", "hora", "posted", "publish"] },
  { chave: "legenda", rotulo: "Legenda", obrigatorio: false,
    dicas: ["legenda", "descricao", "caption", "description", "titulo", "title", "texto"] },
  { chave: "visualizacoes", rotulo: "Visualizações ou impressões", obrigatorio: true,
    dicas: ["visualizac", "impress", "views", "impressions", "plays", "reproduc"] },
  { chave: "alcance", rotulo: "Alcance", obrigatorio: false, dicas: ["alcance", "reach", "contas alcancadas"] },
  { chave: "curtidas", rotulo: "Curtidas", obrigatorio: true, dicas: ["curtida", "like", "reac"] },
  { chave: "comentarios", rotulo: "Comentários", obrigatorio: false, dicas: ["comentario", "comment"] },
  { chave: "compartilhamentos", rotulo: "Compartilhamentos", obrigatorio: false, dicas: ["compartilh", "share"] },
  { chave: "salvamentos", rotulo: "Salvamentos", obrigatorio: false, dicas: ["salvament", "salvo", "save"] },
];

function normalizar(texto) {
  return String(texto ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "");
}

/** Escolhe entre ";", "," e tab olhando a primeira linha (fora de aspas). */
export function detectarSeparador(primeiraLinha) {
  const contagem = { ";": 0, ",": 0, "\t": 0 };
  let entreAspas = false;
  for (const c of primeiraLinha) {
    if (c === '"') entreAspas = !entreAspas;
    else if (!entreAspas && c in contagem) contagem[c]++;
  }
  return Object.entries(contagem).sort((a, b) => b[1] - a[1])[0][0];
}

/** Lê o texto do CSV (com aspas, quebras de linha dentro de campos e BOM). */
export function lerCsv(texto) {
  const conteudo = String(texto).replace(/^﻿/, "");
  const separador = detectarSeparador(conteudo.split(/\r?\n/, 1)[0]);

  const linhas = [];
  let linha = [];
  let campo = "";
  let entreAspas = false;

  for (let i = 0; i < conteudo.length; i++) {
    const c = conteudo[i];
    if (entreAspas) {
      if (c === '"' && conteudo[i + 1] === '"') {
        campo += '"';
        i++;
      } else if (c === '"') {
        entreAspas = false;
      } else {
        campo += c;
      }
    } else if (c === '"') {
      entreAspas = true;
    } else if (c === separador) {
      linha.push(campo);
      campo = "";
    } else if (c === "\n") {
      linha.push(campo);
      linhas.push(linha);
      linha = [];
      campo = "";
    } else if (c !== "\r") {
      campo += c;
    }
  }
  if (campo || linha.length) {
    linha.push(campo);
    linhas.push(linha);
  }

  const naoVazias = linhas.filter((l) => l.some((v) => v.trim() !== ""));
  const [cabecalhos = [], ...dados] = naoVazias;
  const nomes = cabecalhos.map((c) => c.trim());
  return {
    cabecalhos: nomes,
    linhas: dados.map((valores) => Object.fromEntries(nomes.map((nome, i) => [nome, valores[i] ?? ""]))),
  };
}

/** Sugere qual coluna do arquivo corresponde a cada campo. */
export function sugerirMapeamento(cabecalhos) {
  const usados = new Set();
  const mapeamento = {};
  for (const campo of CAMPOS) {
    const encontrado = campo.dicas
      .map((dica) => cabecalhos.find((c) => !usados.has(c) && normalizar(c).includes(dica)))
      .find(Boolean);
    mapeamento[campo.chave] = encontrado ?? "";
    if (encontrado) usados.add(encontrado);
  }
  return mapeamento;
}

/** "1.234" → 1234; "1,5" → 2; "12 mil" não é suportado e vira 12. */
export function converterNumero(texto) {
  let valor = String(texto ?? "").trim().replace(/\s/g, "");
  if (!valor) return 0;
  if (valor.includes(",") && valor.includes(".")) {
    // O último separador é o decimal: "1.234,5" ou "1,234.5"
    valor = valor.lastIndexOf(",") > valor.lastIndexOf(".")
      ? valor.replace(/\./g, "").replace(",", ".")
      : valor.replace(/,/g, "");
  } else if (/^\d{1,3}(\.\d{3})+$/.test(valor)) {
    valor = valor.replace(/\./g, "");
  } else {
    valor = valor.replace(",", ".");
  }
  const numero = Number.parseFloat(valor.replace(/[^\d.-]/g, ""));
  return Number.isFinite(numero) && numero > 0 ? Math.round(numero) : 0;
}

const dois = (n) => String(n).padStart(2, "0");

/**
 * Aceita "2026-10-01 19:30", "2026-10-01T19:30:00" e "01/10/2026 19:30".
 * Devolve "2026-10-01T19:30:00" (horário local, como a API espera) ou null.
 */
export function converterData(texto) {
  const valor = String(texto ?? "").trim();

  let m = valor.match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:[T\s](\d{1,2}):(\d{2})(?::(\d{2}))?)?/);
  if (m) return montarData(+m[1], +m[2], +m[3], +(m[4] ?? 0), +(m[5] ?? 0), +(m[6] ?? 0));

  m = valor.match(/^(\d{1,2})\/(\d{1,2})\/(\d{2,4})(?:,?\s+(\d{1,2}):(\d{2})(?::(\d{2}))?)?/);
  if (m) {
    let dia = +m[1];
    let mes = +m[2];
    if (mes > 12 && dia <= 12) [dia, mes] = [mes, dia]; // formato americano mês/dia
    const ano = +m[3] < 100 ? 2000 + +m[3] : +m[3];
    return montarData(ano, mes, dia, +(m[4] ?? 0), +(m[5] ?? 0), +(m[6] ?? 0));
  }
  return null;
}

function montarData(ano, mes, dia, hora, minuto, segundo) {
  const data = new Date(ano, mes - 1, dia, hora, minuto, segundo);
  if (data.getMonth() !== mes - 1 || data.getDate() !== dia || hora > 23 || minuto > 59) return null;
  return `${ano}-${dois(mes)}-${dois(dia)}T${dois(hora)}:${dois(minuto)}:${dois(segundo)}`;
}

/** Converte as linhas do CSV nos posts enviados à API. Linhas sem data válida são ignoradas. */
export function montarPosts(linhas, mapeamento) {
  const coluna = (linha, chave) => (mapeamento[chave] ? linha[mapeamento[chave]] : "");
  const posts = [];
  let ignoradas = 0;

  for (const linha of linhas) {
    const publicadoEm = converterData(coluna(linha, "publicadoEm"));
    if (!publicadoEm) {
      ignoradas++;
      continue;
    }
    posts.push({
      publicado_em: publicadoEm,
      legenda: String(coluna(linha, "legenda") ?? "").trim(),
      visualizacoes: converterNumero(coluna(linha, "visualizacoes")),
      alcance: converterNumero(coluna(linha, "alcance")),
      curtidas: converterNumero(coluna(linha, "curtidas")),
      comentarios: converterNumero(coluna(linha, "comentarios")),
      compartilhamentos: converterNumero(coluna(linha, "compartilhamentos")),
      salvamentos: converterNumero(coluna(linha, "salvamentos")),
    });
  }
  return { posts, ignoradas };
}

/** Campos obrigatórios que ainda não têm coluna escolhida. */
export function camposFaltando(mapeamento) {
  return CAMPOS.filter((c) => c.obrigatorio && !mapeamento[c.chave]).map((c) => c.rotulo);
}
