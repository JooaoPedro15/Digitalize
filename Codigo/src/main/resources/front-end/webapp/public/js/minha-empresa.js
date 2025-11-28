// minha-empresa.js
(async function () {
  function getUser() {
    try {
      const raw = localStorage.getItem("currentUser");
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  function statusBadge(status) {
    const s = (status || "").toLowerCase();
    if (s.startsWith("aprovad")) return { cls: "badge aprovada", text: "Aprovada" };
    if (s.startsWith("rejeit")) return { cls: "badge rejeitada", text: "Rejeitada" };
    return { cls: "badge pendente", text: status ? status : "Pendente" };
  }

  async function findEmpresaByEmail(email) {
    if (!email) return null;
    
    // --- CORREÇÃO: Usar a rota específica que criamos no Java ---
    const url = `/api/empresas/usuario/${encodeURIComponent(email)}`;
    
    try {
      const r = await fetch(url);
      if (r.ok) {
        const arr = await r.json();
        // Se o usuário tiver empresas, pega a primeira
        if (Array.isArray(arr) && arr.length > 0) {
          return arr[0];
        }
      }
    } catch (e) {
      console.error("Erro ao buscar empresa:", e);
    }
    return null;
  }

  function renderEmpresa(empresa) {
    const nome = empresa.nomeFantasia || empresa.nome_fantasia || "Minha Empresa";
    const razao = empresa.razaoSocial || empresa.razao_social || "—";
    const cnpj = empresa.cnpj || "—";
    const segmento = empresa.segmento || "—";
    const status = empresa.status || "pendente";
    
    // Tratamento de endereço (String ou Objeto)
    let enderecoStr = "—";
    if (typeof empresa.endereco === 'string') {
        enderecoStr = empresa.endereco;
    } else if (empresa.endereco && typeof empresa.endereco === 'object') {
        enderecoStr = empresa.endereco.cidade || "—";
    }

    document.getElementById("empresa-nome").textContent = nome;
    document.getElementById("empresa-sub").textContent = `CNPJ ${cnpj}`;

    const st = statusBadge(status);
    const badge = document.getElementById("empresa-status");
    badge.className = st.cls;
    badge.textContent = st.text;

    const info = document.getElementById("empresa-info");
    info.innerHTML = `
      <ul class="list">
        <li><strong>Razão Social:</strong> ${razao}</li>
        <li><strong>Nome Fantasia:</strong> ${nome}</li>
        <li><strong>Segmento:</strong> ${segmento}</li>
        <li><strong>Endereço:</strong> ${enderecoStr}</li>
        <li><strong>Responsável:</strong> ${empresa.responsavelEmail || "—"}</li>
        <li><strong>Status:</strong> ${st.text}</li>
      </ul>
    `;

    const tools = document.getElementById("tools-buttons");
    tools.innerHTML = "";
    const help = document.getElementById("tools-help");
    
    if (st.text === "Aprovada") {
      help.textContent = "Tudo pronto! Importe seu CSV e acompanhe suas métricas.";
      const btnImp = document.createElement("a");
      btnImp.href = "importacao.html"; // Ou a página correta de importação
      btnImp.className = "btn btn-primary";
      btnImp.innerHTML = '<i class="fas fa-file-import"></i> Importar CSV';
      // Adiciona evento para navegar se for SPA
      btnImp.onclick = (e) => {
         e.preventDefault();
         // Tenta usar o roteador global se existir
         if(window.digitalizeApp) window.digitalizeApp.navigateTo('importacao'); // ajuste conforme sua rota
         else alert("Funcionalidade de importação aqui");
      };
      tools.appendChild(btnImp);
    } else {
      help.textContent = "Sua empresa precisa estar aprovada para liberar as ferramentas.";
      const btn = document.createElement("button");
      btn.disabled = true;
      btn.className = "btn btn-secondary";
      btn.textContent = "Aguardando Aprovação";
      tools.appendChild(btn);
    }
  }

  // --- Main Logic ---
  const user = getUser();
  if (!user) {
    alert("Você precisa estar logado.");
    window.location.href = "login.html";
    return;
  }

  const email = user.email || user.responsavelEmail;
  const empresa = await findEmpresaByEmail(email);

  if (!empresa) {
    // Caso não ache empresa
    document.getElementById("empresa-nome").textContent = "Nenhuma empresa encontrada";
    document.getElementById("empresa-sub").textContent = "Cadastre sua empresa para ver os detalhes.";
    document.getElementById("empresa-info").innerHTML = '<div class="empty">Sem cadastro.</div>';
    return;
  }

  renderEmpresa(empresa);
  
  // Carregar Insights (Guia) se houver botão
  const btnDicas = document.getElementById("btn-minhas-dicas");
  if(btnDicas) {
      btnDicas.disabled = false;
      btnDicas.onclick = () => {
          // Lógica de chamar o GuiaService
          const container = document.getElementById("guia-postagem");
          container.innerHTML = "<p>Gerando dicas com IA...</p>";
          fetch(`/empresa/${empresa.cnpj}/guia-postagem`)
            .then(r => r.json())
            .then(data => {
                // Renderizar JSON do guia (simplificado)
                container.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
            })
            .catch(e => container.innerHTML = "Erro ao gerar guia.");
      };
  }
})();