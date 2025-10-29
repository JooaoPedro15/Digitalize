
// Mostra a aba "Importar CSV" somente se usuário logado tiver empresa aprovada
(function(){
  function getCurrentUser(){
    try{
      const raw = localStorage.getItem('currentUser') || localStorage.getItem('userInfo');
      return raw ? JSON.parse(raw) : null;
    }catch(e){ return null; }
  }

  async function hasEmpresaAprovada(email){
    if(!email) return false;
    const qparams = [
      `responsavelEmail=${encodeURIComponent(email)}`,
      `email=${encodeURIComponent(email)}`
    ];
    // Tente por responsavelEmail e depois por email
    for(const q of qparams){
      try{
        const r = await fetch(`/api/empresas?${q}`);
        if(!r.ok) continue;
        const arr = await r.json();
        if(Array.isArray(arr) && arr.some(e => (e.status||'').toLowerCase().startsWith('aprovad'))){
          return true;
        }
      }catch(e){/*ignora*/}
    }
    return false;
  }

  async function toggleLink(){
    const link = document.getElementById('link-importacao');
    if(!link) return;
    const u = getCurrentUser();
    if(!u){ link.style.display = 'none'; return; }
    const email = u.email || u.responsavelEmail || u?.usuario?.email;
    if(await hasEmpresaAprovada(email)){
      link.style.display = '';
    }else{
      link.style.display = 'none';
    }
  }

  document.addEventListener('DOMContentLoaded', toggleLink);
})();


// Também controlar o link 'Minha Empresa' (mostrar se usuário tem qualquer empresa cadastrada)
(function(){
  async function hasEmpresa(email){
    if(!email) return false;
    try{
      const r1 = await fetch(`/api/empresas?responsavelEmail=${encodeURIComponent(email)}&_limit=1`);
      if(r1.ok){ const a = await r1.json(); if(Array.isArray(a) && a.length) return true; }
      const r2 = await fetch(`/api/empresas?email=${encodeURIComponent(email)}&_limit=1`);
      if(r2.ok){ const b = await r2.json(); if(Array.isArray(b) && b.length) return true; }
    }catch(e){}
    return false;
  }
  function getUser(){
    try{
      const raw = localStorage.getItem('currentUser') || localStorage.getItem('userInfo');
      return raw ? JSON.parse(raw) : null;
    }catch(e){ return null; }
  }
  async function toggleMinhaEmpresa(){
    const link = document.getElementById('link-minha-empresa');
    if(!link) return;
    const u = getUser();
    if(!u){ link.style.display = 'none'; return; }
    const email = u.email || u.responsavelEmail || u?.usuario?.email;
    if(await hasEmpresa(email)){
      link.style.display = '';
    }else{
      link.style.display = 'none';
    }
  }
  document.addEventListener('DOMContentLoaded', toggleMinhaEmpresa);
})();

// Reagir a mudanças no localStorage (ex.: login/logout em outra aba) e quando a aba volta a focar
(function(){
  async function rerun(){
    // reuse existing functions by reloading the page or dispatching DOMContentLoaded handlers
    const ev = new Event('DOMContentLoaded');
    document.dispatchEvent(ev);
  }
  window.addEventListener('storage', rerun);
  document.addEventListener('visibilitychange', ()=>{ if(!document.hidden) rerun(); });
})();