// minha-empresa.js
(async function(){
  function getUser(){
    try{
      const raw = localStorage.getItem('currentUser') || localStorage.getItem('userInfo');
      return raw ? JSON.parse(raw) : null;
    }catch(e){ return null; }
  }
  function statusBadge(status){
    const s = (status||'').toLowerCase();
    if(s.startsWith('aprovad')) return {cls:'badge aprovada', text:'Aprovada'};
    if(s.startsWith('rejeit')) return {cls:'badge rejeitada', text:'Rejeitada'};
    return {cls:'badge pendente', text: status ? status : 'Pendente'};
  }
  async function findEmpresaByEmail(email){
    if(!email) return null;
    // Tenta por responsavelEmail e depois por email
    const endpoints = [
      `/api/empresas?responsavelEmail=${encodeURIComponent(email)}&_limit=1`,
      `/api/empresas?email=${encodeURIComponent(email)}&_limit=1`
    ];
    for(const url of endpoints){
      try{
        const r = await fetch(url);
        if(!r.ok) continue;
        const arr = await r.json();
        if(Array.isArray(arr) && arr.length) return arr[0];
      }catch(e){}
    }
    return null;
  }
  function renderEmpresa(empresa){
    const nome = empresa.nomeFantasia || empresa.razaoSocial || empresa.nome || 'Minha Empresa';
    document.getElementById('empresa-nome').textContent = nome;
    document.getElementById('empresa-sub').textContent = empresa.cnpj ? `CNPJ ${empresa.cnpj}` : (empresa.segmento || '—');
    const st = statusBadge(empresa.status);
    const badge = document.getElementById('empresa-status');
    badge.className = st.cls;
    badge.textContent = st.text;

    const info = document.getElementById('empresa-info');
    info.innerHTML = `
      <ul class="list">
        <li><strong>Razão Social:</strong> ${empresa.razaoSocial || '—'}</li>
        <li><strong>Nome Fantasia:</strong> ${empresa.nomeFantasia || '—'}</li>
        <li><strong>Segmento:</strong> ${empresa.segmento || '—'}</li>
        <li><strong>Responsável:</strong> ${empresa.responsavelNome || '—'}</li>
        <li><strong>E-mail do Responsável:</strong> ${empresa.responsavelEmail || empresa.email || '—'}</li>
        <li><strong>Status:</strong> ${st.text}</li>
      </ul>
    `;

    const tools = document.getElementById('tools-buttons');
    tools.innerHTML = '';
    const help = document.getElementById('tools-help');
    const aprovado = st.text.toLowerCase()==='aprovada';

    if(aprovado){
      help.textContent = 'Tudo pronto! Importe seu CSV e acompanhe suas métricas.';
      const btnImp = document.createElement('a');
      btnImp.href = 'importacao.html';
      btnImp.className = 'btn btn-primary';
      btnImp.innerHTML = '<i class="fa fa-file-import" style="margin-right:.5rem"></i> Importar CSV';
      tools.appendChild(btnImp);
    }else{
      help.textContent = 'Sua empresa precisa estar aprovada para liberar as ferramentas.';
      const btn = document.createElement('button');
      btn.disabled = true;
      btn.className = 'btn btn-secondary';
      btn.textContent = 'Importar CSV (aguardando aprovação)';
      tools.appendChild(btn);
    }
  }

  function loadInsights(email){
    try{
      const raw = localStorage.getItem('insights_'+email);
      return raw ? JSON.parse(raw) : null;
    }catch(e){ return null; }
  }
  function renderRecs(ins){
    const box = document.getElementById('recs');
    if(!ins){
      box.innerHTML = '<div class="empty">Sem recomendações ainda. Faça a importação do CSV para gerarmos sugestões personalizadas.</div>';
      return;
    }
    // Monta recomendações simples a partir de topHours e topPosts
    const horas = (ins.topHours||[]).map(h=>`${String(h.hour).padStart(2,'0')}:00 (média ${Math.round(h.avg)} interações)`);
    const posts = (ins.topPostsAbs||[]).map((p,idx)=>`${idx+1}º — ${p.title||('Post #'+(p.i+1))} (${Math.round(p.interactions)} interações)`);
    box.innerHTML = `
      <div class="grid-2">
        <div>
          <h3 class="h6">Melhores horários</h3>
          <ul class="list">${horas.map(h=>`<li>${h}</li>`).join('')}</ul>
        </div>
        <div>
          <h3 class="h6">Top posts</h3>
          <ul class="list">${posts.map(p=>`<li>${p}</li>`).join('')}</ul>
        </div>
      </div>
      <p class="muted" style="margin-top:12px">Recomendações geradas a partir do último CSV importado nesta conta.</p>
    `;
  }
  
  // Habilita o botao "Minhas dicas" apos o CSV ser importado.
  // No inicio vamos testar manualmente, depois ligamos com o fluxo real.
  function habilitarMinhasDicas(cnpj)
  {
      var btn = document.getElementById("btn-minhas-dicas");
      if (!btn)
      {
          return;
      }

      btn.disabled = false;
      // guarda o CNPJ no proprio botao
      btn.dataset.cnpj = cnpj;
  }

  // Quando a pagina carregar, conecta o clique do botao
  document.addEventListener("DOMContentLoaded", function ()
  {
      var btn = document.getElementById("btn-minhas-dicas");
      if (!btn)
      {
          return;
      }

      // *** TESTE: habilita o botao para o CNPJ da empresa 2 ***
      // Depois podemos remover e chamar habilitarMinhasDicas(...)
      // a partir do fluxo de upload do CSV.
      habilitarMinhasDicas("00000000000002");

      btn.addEventListener("click", function ()
      {
          if (btn.disabled)
          {
              return;
          }

          var cnpj = btn.dataset.cnpj;
          if (!cnpj)
          {
              alert("CNPJ nao definido para gerar o guia.");
              return;
          }

          buscarGuiaPostagem(cnpj);
      });
  });

  // Faz GET na rota /empresa/:cnpj/guia-postagem e mostra na tela
  function buscarGuiaPostagem(cnpj)
  {
      var container = document.getElementById("guia-postagem");
      if (!container)
      {
          return;
      }

      container.innerHTML = "<p>Gerando dicas...</p>";

      fetch("/empresa/" + encodeURIComponent(cnpj) + "/guia-postagem")
          .then(function (resp)
          {
              if (!resp.ok)
              {
                  return resp.text().then(function (txt)
                  {
                      throw new Error("Erro " + resp.status + ": " + txt);
                  });
              }
              return resp.json();
          })
          .then(function (guia)
          {
              renderizarGuia(guia, container);
          })
          .catch(function (erro)
          {
              console.error(erro);
              container.innerHTML =
                  "<p style='color:red;'>Erro ao gerar guia: "
                  + erro.message + "</p>";
          });
  }

  // Monta o HTML do guia de postagem
  function renderizarGuia(guia, container)
  {
      var html = "";

      html += "<div style='border:1px solid #ddd; border-radius:8px; padding:12px; background:#f8fafc; font-size:14px;'>";

      // Resumo
      html += "<h3 style='font-weight:bold; margin-bottom:4px;'>Resumo do periodo</h3>";
      html += "<p>" + (guia.resumo_periodo || "") + "</p>";

      // Top 3 insights
      html += "<h3 style='font-weight:bold; margin-top:12px; margin-bottom:4px;'>Top 3 insights</h3>";
      html += "<ul>";
      if (guia.top_3_insights)
      {
          guia.top_3_insights.forEach(function (t)
          {
              html += "<li>" + t + "</li>";
          });
      }
      html += "</ul>";

      // Melhores horarios
      html += "<h3 style='font-weight:bold; margin-top:12px; margin-bottom:4px;'>Melhores horarios</h3>";
      if (guia.melhores_horarios && guia.melhores_horarios.length > 0)
      {
          html += "<p>" + guia.melhores_horarios.join(", ") + "</p>";
      }
      else
      {
          html += "<p>—</p>";
      }

      // Tom de voz
      html += "<h3 style='font-weight:bold; margin-top:12px; margin-bottom:4px;'>Tom de voz recomendado</h3>";
      html += "<ul>";
      if (guia.diretrizes_tom_voz)
      {
          guia.diretrizes_tom_voz.forEach(function (t)
          {
              html += "<li>" + t + "</li>";
          });
      }
      html += "</ul>";

      // O que evitar
      html += "<h3 style='font-weight:bold; margin-top:12px; margin-bottom:4px;'>O que evitar</h3>";
      html += "<ul>";
      if (guia.o_que_evitar)
      {
          guia.o_que_evitar.forEach(function (t)
          {
              html += "<li>" + t + "</li>";
          });
      }
      html += "</ul>";

      // Sugestoes de posts
      html += "<h3 style='font-weight:bold; margin-top:12px; margin-bottom:4px;'>Sugestoes de posts</h3>";
      if (guia.sugestoes_posts)
      {
          guia.sugestoes_posts.forEach(function (sug, i)
          {
              html += "<div style='border:1px solid #e2e8f0; border-radius:6px; padding:8px; margin-top:6px; background:white;'>";
              html += "<p style='font-weight:bold;'>" + (sug.titulo || ("Ideia " + (i + 1))) + "</p>";
              html += "<p><strong>Legenda:</strong> " + (sug.descricao_legendada || "") + "</p>";
              html += "<p style='font-size:12px;'><strong>Justificativa:</strong> " + (sug.justificativa || "") + "</p>";
              html += "</div>";
          });
      }

      html += "</div>";

      container.innerHTML = html;
  }


  // Main
  const user = getUser();
  if(!user){
    alert('Você precisa estar logado para acessar esta página.');
    window.location.href = 'login.html';
    return;
  }
  const email = user.email || user.responsavelEmail || user?.usuario?.email;
  const empresa = await findEmpresaByEmail(email);
  if(!empresa){
    document.getElementById('empresa-nome').textContent = 'Nenhuma empresa encontrada';
    document.getElementById('empresa-sub').textContent = 'Cadastre sua empresa para liberar esta área.';
    document.getElementById('empresa-status').className = 'badge pendente';
    document.getElementById('empresa-status').textContent = '—';
    document.getElementById('empresa-info').innerHTML = '<div class="empty">Sem empresa cadastrada.</div>';
    const tools = document.getElementById('tools-buttons');
    tools.innerHTML = '<a href="index.html" class="btn btn-secondary">Ir para Cadastrar</a>';
    document.getElementById('recs').innerHTML = '<div class="empty">Cadastre sua empresa e depois importe um CSV para ver recomendações.</div>';
    return;
  }
  renderEmpresa(empresa);
  const insights = loadInsights(email);
  renderRecs(insights);
})();