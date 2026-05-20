(async function () {
  function getUser() {
    try {
      const raw = localStorage.getItem("currentUser");
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function statusBadge(status) {
    const value = (status || "pendente").toLowerCase();
    if (value.startsWith("aprovad")) return { cls: "badge aprovada", text: "Aprovada" };
    if (value.startsWith("rejeit")) return { cls: "badge rejeitada", text: "Rejeitada" };
    return { cls: "badge pendente", text: "Pendente" };
  }

  async function findEmpresaByEmail(email) {
    if (!email) return null;

    try {
      const response = await fetch(`/api/empresas/usuario/${encodeURIComponent(email)}`);
      if (!response.ok) return null;

      const empresas = await response.json();
      return Array.isArray(empresas) && empresas.length > 0 ? empresas[0] : null;
    } catch (e) {
      console.error("Erro ao buscar empresa:", e);
      return null;
    }
  }

  function renderEmpresa(empresa) {
    const nome = empresa.nomeFantasia || empresa.nome_fantasia || "Minha Empresa";
    const razao = empresa.razaoSocial || empresa.razao_social || "-";
    const cnpj = empresa.cnpj || "-";
    const segmento = empresa.segmento || "-";
    const endereco = typeof empresa.endereco === "string" ? empresa.endereco : "-";
    const responsavel = empresa.responsavelEmail || empresa.responsavel_email || "-";
    const status = statusBadge(empresa.status);

    document.getElementById("empresa-nome").textContent = nome;
    document.getElementById("empresa-sub").textContent = `CNPJ ${cnpj}`;

    const badge = document.getElementById("empresa-status");
    badge.className = status.cls;
    badge.textContent = status.text;

    document.getElementById("empresa-info").innerHTML = `
      <ul class="list">
        <li><strong>Razao Social:</strong> ${escapeHtml(razao)}</li>
        <li><strong>Nome Fantasia:</strong> ${escapeHtml(nome)}</li>
        <li><strong>Segmento:</strong> ${escapeHtml(segmento)}</li>
        <li><strong>Endereco:</strong> ${escapeHtml(endereco)}</li>
        <li><strong>Responsavel:</strong> ${escapeHtml(responsavel)}</li>
        <li><strong>Status:</strong> ${escapeHtml(status.text)}</li>
      </ul>
    `;

    const tools = document.getElementById("tools-buttons");
    const help = document.getElementById("tools-help");
    tools.innerHTML = "";

    if (status.text === "Aprovada") {
      help.textContent = "Tudo pronto. Importe seu CSV para atualizar metricas e gerar recomendacoes.";
      const importLink = document.createElement("a");
      importLink.href = "importacao.html";
      importLink.className = "btn btn-primary";
      importLink.innerHTML = '<i class="fas fa-file-import"></i> Importar CSV';
      tools.appendChild(importLink);
      return;
    }

    help.textContent = "Sua empresa precisa estar aprovada para liberar as ferramentas.";
    const disabledButton = document.createElement("button");
    disabledButton.disabled = true;
    disabledButton.className = "btn btn-secondary";
    disabledButton.textContent = "Aguardando aprovacao";
    tools.appendChild(disabledButton);
  }

  function renderGuide(container, data) {
    const list = (items) => {
      if (!Array.isArray(items) || items.length === 0) return "<li>Nenhum item encontrado.</li>";
      return items.map((item) => `<li>${escapeHtml(item)}</li>`).join("");
    };

    const sugestoes = Array.isArray(data.sugestoes_posts) ? data.sugestoes_posts : [];
    const sugestoesHtml = sugestoes.length
      ? sugestoes.map((item) => `
          <li>
            <strong>${escapeHtml(item.titulo || "Sugestao")}</strong><br>
            ${escapeHtml(item.descricao_legendada || item.descricao || "")}
            <div class="muted">${escapeHtml(item.justificativa || "")}</div>
          </li>
        `).join("")
      : "<li>Nenhuma sugestao encontrada.</li>";

    container.innerHTML = `
      <p>${escapeHtml(data.resumo_periodo || "Guia gerado com os dados disponiveis.")}</p>
      <h3 class="h6">Insights</h3>
      <ul class="list">${list(data.top_3_insights)}</ul>
      <h3 class="h6">Melhores horarios</h3>
      <ul class="list">${list(data.melhores_horarios)}</ul>
      <h3 class="h6">Tom de voz</h3>
      <ul class="list">${list(data.diretrizes_tom_voz)}</ul>
      <h3 class="h6">O que evitar</h3>
      <ul class="list">${list(data.o_que_evitar)}</ul>
      <h3 class="h6">Sugestoes de posts</h3>
      <ul class="list">${sugestoesHtml}</ul>
    `;
  }

  const user = getUser();
  if (!user) {
    alert("Voce precisa estar logado.");
    window.location.href = "login.html";
    return;
  }

  const empresa = await findEmpresaByEmail(user.email || user.responsavelEmail);
  if (!empresa) {
    document.getElementById("empresa-nome").textContent = "Nenhuma empresa encontrada";
    document.getElementById("empresa-sub").textContent = "Cadastre sua empresa para ver os detalhes.";
    document.getElementById("empresa-info").innerHTML = '<div class="empty">Sem cadastro.</div>';
    return;
  }

  renderEmpresa(empresa);

  const btnDicas = document.getElementById("btn-minhas-dicas");
  if (btnDicas) {
    btnDicas.disabled = false;
    btnDicas.addEventListener("click", async () => {
      const container = document.getElementById("guia-postagem");
      btnDicas.disabled = true;
      container.innerHTML = "<p>Gerando dicas...</p>";

      try {
        const response = await fetch(`/empresa/${encodeURIComponent(empresa.cnpj)}/guia-postagem`);
        const data = await response.json();
        if (!response.ok) {
          throw new Error(data.error || "Erro ao gerar guia.");
        }
        renderGuide(container, data);
      } catch (e) {
        container.innerHTML = '<div class="empty">Nao foi possivel gerar o guia agora.</div>';
      } finally {
        btnDicas.disabled = false;
      }
    });
  }
})();
