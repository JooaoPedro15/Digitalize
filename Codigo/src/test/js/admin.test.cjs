const test = require("node:test");
const assert = require("node:assert/strict");

const admin = require("../../main/resources/front-end/webapp/public/js/admin.js");

test("escapeHtml neutralizes HTML-sensitive characters", () => {
  assert.equal(
    admin.escapeHtml(`<img src=x onerror="alert('x')">`),
    "&lt;img src=x onerror=&quot;alert(&#039;x&#039;)&quot;&gt;"
  );
});

test("renderEmpresaRows escapes database fields and avoids inline handlers", () => {
  const html = admin.renderEmpresaRows([
    {
      cnpj: "12.345.678/0001-99",
      nomeFantasia: "<script>alert(1)</script>",
      status: `aprovada" onclick="alert(1)`,
    },
  ]);

  assert.match(html, /data-cnpj="12345678000199"/);
  assert.doesNotMatch(html, /<script>/);
  assert.doesNotMatch(html, /onclick=/);
  assert.match(html, /status-pendente/);
});

test("renderUsuarios escapes user fields", () => {
  const html = admin.renderUsuarios([
    { id: 7, email: "user@example.com<script>", tipo: "<b>admin</b>" },
  ]);

  assert.match(html, /user@example\.com&lt;script&gt;/);
  assert.match(html, /&lt;b&gt;admin&lt;\/b&gt;/);
  assert.doesNotMatch(html, /<b>admin<\/b>/);
});
