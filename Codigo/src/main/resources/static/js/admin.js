(function (global) {
  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function onlyDigits(value) {
    return String(value ?? "").replace(/\D/g, "");
  }

  function firstText() {
    for (const value of arguments) {
      if (value !== undefined && value !== null && String(value).trim() !== "") {
        return value;
      }
    }
    return "-";
  }

  function normalizeStatus(status) {
    const value = String(status || "pendente").toLowerCase();
    if (value === "aprovada" || value === "ativa") {
      return { value: "aprovada", label: "aprovada", className: "status-aprovada" };
    }
    if (value === "rejeitada" || value === "inativa") {
      return { value: "rejeitada", label: "rejeitada", className: "status-rejeitada" };
    }
    return { value: "pendente", label: "pendente", className: "status-pendente" };
  }

  function renderEmpresaRows(empresas) {
    if (!Array.isArray(empresas) || empresas.length === 0) {
      return '<tr><td colspan="4">Nenhuma empresa cadastrada.</td></tr>';
    }

    return empresas.map((empresa) => {
      const cnpj = onlyDigits(empresa.cnpj);
      const status = normalizeStatus(empresa.status);
      const nomeFantasia = firstText(empresa.nomeFantasia, empresa.nome_fantasia);

      return `
        <tr>
          <td>${escapeHtml(firstText(empresa.cnpj, cnpj))}</td>
          <td>${escapeHtml(nomeFantasia)}</td>
          <td><span class="status-badge ${status.className}">${escapeHtml(status.label)}</span></td>
          <td>
            <button type="button" data-action="view-empresa" data-cnpj="${escapeHtml(cnpj)}" class="btn-sm" title="Visualizar empresa">
              <i class="fas fa-eye"></i>
            </button>
          </td>
        </tr>
      `;
    }).join("");
  }

  function renderUsuarios(usuarios) {
    if (!Array.isArray(usuarios) || usuarios.length === 0) {
      return '<tr><td colspan="3">Nenhum usuario cadastrado.</td></tr>';
    }

    return usuarios.map((usuario) => `
      <tr>
        <td>${escapeHtml(firstText(usuario.id))}</td>
        <td>${escapeHtml(firstText(usuario.email))}</td>
        <td>${escapeHtml(firstText(usuario.tipo))}</td>
      </tr>
    `).join("");
  }

  const api = {
    escapeHtml,
    onlyDigits,
    normalizeStatus,
    renderEmpresaRows,
    renderUsuarios,
  };

  global.DigitalizeAdmin = api;

  if (typeof module !== "undefined" && module.exports) {
    module.exports = api;
  }
})(typeof window !== "undefined" ? window : globalThis);
