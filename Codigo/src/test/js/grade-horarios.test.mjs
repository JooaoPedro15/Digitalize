import assert from "node:assert/strict";
import test from "node:test";

import { agregar, descreverMelhor, nivel } from "../../main/resources/static/js/grade-horarios.js";
import { engajamentoPorMil } from "../../main/resources/static/js/exemplo.js";
import { escapeHtml, formatarCnpj, formatarData } from "../../main/resources/static/js/ui.js";

// 2026-10-02 é uma sexta-feira
const posts = [
  { publicadoEm: "2026-10-02T21:00:00", engajamento: 200 },
  { publicadoEm: "2026-10-09T21:30:00", engajamento: 100 },
  { publicadoEm: "2026-10-05T09:00:00", engajamento: 40 },
  { publicadoEm: "2026-10-05T03:00:00", engajamento: 900 },
];

test("agrupa por dia e hora calculando a média", () => {
  const { celulas } = agregar(posts);
  const sextaNoite = celulas.get("5-21");

  assert.equal(sextaNoite.quantidade, 2);
  assert.equal(sextaNoite.media, 150);
});

test("ignora horas fora da grade", () => {
  assert.equal(agregar(posts).celulas.has("1-3"), false);
});

test("encontra a melhor combinação e ordena as melhores horas", () => {
  const agregado = agregar(posts);

  assert.equal(descreverMelhor(agregado), "Sexta, 21h");
  assert.deepEqual(agregado.melhoresHoras, [21, 9]);
});

test("sem posts não há melhor horário", () => {
  const agregado = agregar([]);

  assert.equal(agregado.melhor, null);
  assert.equal(descreverMelhor(agregado), "");
});

test("o nível da cor é proporcional ao maior engajamento", () => {
  assert.equal(nivel(10, 100), 1);
  assert.equal(nivel(50, 100), 2);
  assert.equal(nivel(75, 100), 3);
  assert.equal(nivel(100, 100), 4);
});

test("calcula o engajamento igual ao backend", () => {
  assert.equal(engajamentoPorMil({ visualizacoes: 1000, curtidas: 40, comentarios: 5, compartilhamentos: 5 }), 50);
  assert.equal(engajamentoPorMil({ visualizacoes: 0, curtidas: 40, comentarios: 0, compartilhamentos: 0 }), 0);
});

test("neutraliza HTML vindo de legendas e nomes", () => {
  assert.equal(
    escapeHtml(`<img src=x onerror="alert('x')">`),
    "&lt;img src=x onerror=&quot;alert(&#039;x&#039;)&quot;&gt;"
  );
  assert.equal(escapeHtml(null), "");
});

test("formata CNPJ e data", () => {
  assert.equal(formatarCnpj("12345678000100"), "12.345.678/0001-00");
  assert.equal(formatarData("2026-10-01"), "01/10/2026");
});
