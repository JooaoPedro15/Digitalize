// gate-importacao.js
(function(){
  function getCurrentUser(){
    try{
      const raw = localStorage.getItem('currentUser');
      return raw ? JSON.parse(raw) : null;
    }catch(e){ return null; }
  }

  async function hasEmpresaAprovada(email){
    if(!email) return false;
    try{
      // CORREÇÃO: Usa a rota correta /api/empresas/usuario/:email
      const r = await fetch(`/api/empresas/usuario/${encodeURIComponent(email)}`);
      if(r.ok){ 
        const arr = await r.json();
        // Verifica se alguma das empresas desse usuário está aprovada
        return Array.isArray(arr) && arr.some(e => (e.status||'').toLowerCase().startsWith('aprovad'));
      }
    }catch(e){}
    return false;
  }

  async function toggleLink(){
    const link = document.getElementById('link-importacao'); // Se existir no seu HTML
    const linkMinha = document.getElementById('link-minha-empresa');
    
    if(!link && !linkMinha) return;
    
    const u = getCurrentUser();
    if(!u){ 
        if(link) link.style.display = 'none'; 
        if(linkMinha) linkMinha.style.display = 'none';
        return; 
    }
    
    const email = u.email;
    
    // Checa se tem empresa (aprovada ou não) para mostrar o link "Minha Empresa"
    try {
        const r = await fetch(`/api/empresas/usuario/${encodeURIComponent(email)}`);
        const arr = await r.json();
        if(arr.length > 0 && linkMinha) {
            linkMinha.style.display = 'inline'; // ou 'block'
        }
        
        // Checa aprovação para liberar Importação
        const aprovada = arr.some(e => (e.status||'').toLowerCase().startsWith('aprovad'));
        if(link) link.style.display = aprovada ? 'inline' : 'none';
        
    } catch(e) {}
  }

  document.addEventListener('DOMContentLoaded', toggleLink);
})();