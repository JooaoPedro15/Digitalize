import assert from "node:assert/strict";
import test from "node:test";

import {
  camposFaltando,
  converterData,
  converterNumero,
  lerCsv,
  montarPosts,
  sugerirMapeamento,
} from "../../main/resources/static/js/csv.js";
import { gerarCsvExemplo } from "../../main/resources/static/js/exemplo.js";

test("lê CSV separado por ponto e vírgula, com BOM e aspas", () => {
  const { cabecalhos, linhas } = lerCsv('﻿Data;Legenda\n01/10/2026;"Bolo; com ""calda"""\n');

  assert.deepEqual(cabecalhos, ["Data", "Legenda"]);
  assert.equal(linhas[0].Legenda, 'Bolo; com "calda"');
});

test("lê CSV separado por vírgula e ignora linhas vazias", () => {
  const { linhas } = lerCsv("a,b\r\n1,2\r\n\r\n3,4\r\n");

  assert.equal(linhas.length, 2);
  assert.equal(linhas[1].b, "4");
});

test("reconhece as colunas de uma exportação em português", () => {
  const cabecalhos = ["Horário de publicação", "Descrição", "Impressões", "Alcance", "Curtidas", "Comentários"];
  const mapa = sugerirMapeamento(cabecalhos);

  assert.equal(mapa.publicadoEm, "Horário de publicação");
  assert.equal(mapa.legenda, "Descrição");
  assert.equal(mapa.visualizacoes, "Impressões");
  assert.equal(mapa.salvamentos, "");
  assert.deepEqual(camposFaltando(mapa), []);
});

test("aponta os campos obrigatórios sem coluna", () => {
  assert.deepEqual(camposFaltando(sugerirMapeamento(["Legenda"])), [
    "Data e hora da publicação",
    "Visualizações ou impressões",
    "Curtidas",
  ]);
});

test("converte números nos formatos brasileiro e americano", () => {
  assert.equal(converterNumero("1.234"), 1234);
  assert.equal(converterNumero("1.234,6"), 1235);
  assert.equal(converterNumero("1,234.6"), 1235);
  assert.equal(converterNumero(""), 0);
  assert.equal(converterNumero("abc"), 0);
});

test("converte datas e recusa as inválidas", () => {
  assert.equal(converterData("01/10/2026 19:30"), "2026-10-01T19:30:00");
  assert.equal(converterData("2026-10-01T19:30:15"), "2026-10-01T19:30:15");
  assert.equal(converterData("2026-10-01"), "2026-10-01T00:00:00");
  assert.equal(converterData("10/25/2026 08:00"), "2026-10-25T08:00:00");
  assert.equal(converterData("31/02/2026"), null);
  assert.equal(converterData("ontem"), null);
});

test("monta os posts e conta as linhas sem data válida", () => {
  const { posts, ignoradas } = montarPosts(
    [
      { Data: "01/10/2026 19:30", Views: "1.000", Likes: "50" },
      { Data: "sem data", Views: "1", Likes: "1" },
    ],
    { publicadoEm: "Data", visualizacoes: "Views", curtidas: "Likes" }
  );

  assert.equal(ignoradas, 1);
  assert.equal(posts[0].publicado_em, "2026-10-01T19:30:00");
  assert.equal(posts[0].visualizacoes, 1000);
  assert.equal(posts[0].comentarios, 0);
});

test("o arquivo de exemplo é lido sem nenhuma correção manual", () => {
  const { cabecalhos, linhas } = lerCsv(gerarCsvExemplo());
  const mapa = sugerirMapeamento(cabecalhos);
  const { posts, ignoradas } = montarPosts(linhas, mapa);

  assert.deepEqual(camposFaltando(mapa), []);
  assert.equal(ignoradas, 0);
  assert.equal(posts.length, linhas.length);
  assert.ok(posts.length > 0);
});
