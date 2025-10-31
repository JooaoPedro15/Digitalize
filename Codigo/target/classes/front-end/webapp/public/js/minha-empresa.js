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