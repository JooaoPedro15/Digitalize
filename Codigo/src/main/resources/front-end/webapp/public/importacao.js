// importacao.js — CSV → métricas (frontend-only)
(function(){
  // Elements
  const elFile = document.getElementById('csvFile');
  const elInfo = document.getElementById('fileInfo');
  const elMapCard = document.getElementById('mapCard');
  const elMapping = document.getElementById('mapping');
  const elBtnParse = document.getElementById('btnParse');
  const elBtnDemo = document.getElementById('btnDemo');
  const elBtnMap = document.getElementById('btnAplicarMap');
  const elResCard = document.getElementById('resCard');
  const elTHead = document.getElementById('thead');
  const elTBody = document.getElementById('tbody');
  const elBtnPrint = document.getElementById('btnPrint');
  const rankModeSel = document.getElementById('rankMode');

  // State
  let rows = [];       // Array<object>
  let headers = [];    // CSV headers
  let mapping = {};    // field->header
  let mappedRows = []; // Normalized rows

  const REQUIRED_FIELDS = [
    { key: 'datetime', label: 'Data/Hora', required: true },
    { key: 'title', label: 'Título/Legenda', required: false },
    { key: 'impressions', label: 'Impressões', required: false },
    { key: 'reach', label: 'Alcance (Reach)', required: false },
    { key: 'likes', label: 'Likes', required: true },
    { key: 'comments', label: 'Comentários', required: true },
    { key: 'shares', label: 'Compartilhamentos', required: false },
    { key: 'saves', label: 'Saves/Salvos', required: false },
  ];

  const HINTS = {
    datetime: ['date', 'data', 'datetime', 'horario', 'hora', 'publicado', 'posted'],
    title: ['titulo', 'title', 'legenda', 'caption', 'post', 'conteudo'],
    impressions: ['impress', 'impressions', 'impressoes'],
    reach: ['reach', 'alcance'],
    likes: ['like', 'curtida', 'likes', 'curtidas'],
    comments: ['comment', 'comentario', 'comentarios', 'comments'],
    shares: ['share', 'compart', 'compartilhamentos'],
    saves: ['save', 'salvo', 'salvos', 'saves']
  };

  function sanitizeHeader(h){
    return (h||'').toString().trim().toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  function autoMap(headers){
    const normalized = headers.map(h=>({raw:h, norm: sanitizeHeader(h)}));
    const res = {};
    for(const f of REQUIRED_FIELDS){
      let best = null;
      for(const h of normalized){
        for(const hint of (HINTS[f.key]||[])){
          if(h.norm.includes(hint)){
            best = h.raw;
            break;
          }
        }
        if(best) break;
      }
      res[f.key] = best || '';
    }
    return res;
  }

  // Simple CSV parser with quoted fields support
  function parseCSV(text){
    const out = [];
    let row = [];
    let field = '';
    let i=0, inQuotes=false;
    while(i<text.length){
      const c = text[i];
      if(inQuotes){
        if(c === '"'){
          if(text[i+1] === '"'){ field += '"'; i+=2; continue; }
          inQuotes = false; i++; continue;
        }else{ field += c; i++; continue; }
      }else{
        if(c === '"'){ inQuotes = true; i++; continue; }
        if(c === ','){ row.push(field); field=''; i++; continue; }
        if(c === '\n'){
          row.push(field); field=''; out.push(row); row=[]; i++; continue;
        }
        if(c === '\r'){ i++; continue; }
        field += c; i++;
      }
    }
    // last field
    if(field.length>0 || row.length>0){ row.push(field); out.push(row); }
    return out;
  }

  function toNumber(x){
    if(x==null) return 0;
    let s = String(x).replace(/\./g,'').replace(',', '.').replace(/[^\d\.\-]/g,'');
    const n = parseFloat(s);
    return isNaN(n)?0:n;
  }

  function buildMappingUI(){
    elMapping.innerHTML = '';
    for(const f of REQUIRED_FIELDS){
      const wrap = document.createElement('div');
      wrap.innerHTML = `
        <label class="form-label">${f.label} ${f.required?'<span class="badge badge-ok ms-1">obrigatório</span>':'<span class="badge badge-warn ms-1">opcional</span>'}</label>
        <select class="form-select mapping-select" data-key="${f.key}">
          <option value="">— selecione —</option>
          ${headers.map(h=>`<option value="${h}">${h}</option>`).join('')}
        </select>
      `;
      elMapping.appendChild(wrap);
    }
    // Auto-select suggestions
    const guess = autoMap(headers);
    for(const sel of elMapping.querySelectorAll('select')){
      const key = sel.getAttribute('data-key');
      const v = guess[key] || '';
      if(v && headers.includes(v)) sel.value = v;
    }
  }

  function applyMapping(){
    mapping = {};
    for(const sel of elMapping.querySelectorAll('select')){
      const key = sel.getAttribute('data-key');
      mapping[key] = sel.value || '';
    }

    // Validate required fields
    const missing = REQUIRED_FIELDS.filter(f=>f.required && !mapping[f.key]).map(f=>f.label);
    if(missing.length){
      alert('Selecione as colunas obrigatórias: ' + missing.join(', '));
      return false;
    }

    // Normalize rows
    mappedRows = rows.map(r=>{
      const o = {};
      for(const k in mapping){
        const col = mapping[k];
        o[k] = col ? r[col] : '';
      }
      // Casts
      o.likes = toNumber(o.likes);
      o.comments = toNumber(o.comments);
      o.shares = toNumber(o.shares);
      o.saves = toNumber(o.saves);
      o.impressions = toNumber(o.impressions);
      o.reach = toNumber(o.reach);
      // datetime
      o.datetime = parseDate(o.datetime);
      o.hour = o.datetime ? o.datetime.getHours() : null;
      // derived
      o.interactions = (o.likes||0)+(o.comments||0)+(o.shares||0)+(o.saves||0);
      o.rateImp = o.impressions>0 ? o.interactions/o.impressions : 0;
      o.rateReach = o.reach>0 ? o.interactions/o.reach : 0;
      return o;
    }).filter(x=>x.datetime instanceof Date && !isNaN(x.datetime));
    return true;
  }

  function parseDate(s){
    if(!s) return null;
    let t = String(s).trim();
    // Try ISO / locale / dd/mm/yyyy hh:mm
    const tryFormats = [
      (t)=>new Date(t), // ISO or parseable
      (t)=>{
        // dd/mm/yyyy hh:mm[:ss]
        const m = t.match(/^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{2,4})(?:\s+(\d{1,2}):(\d{2})(?::(\d{2}))?)?$/);
        if(!m) return null;
        const dd = +m[1], mm=+m[2]-1, yy=+m[3] < 100 ? 2000+ +m[3]: +m[3];
        const hh = m[4]?+m[4]:0, mi=m[5]?+m[5]:0, ss=m[6]?+m[6]:0;
        return new Date(yy, mm, dd, hh, mi, ss);
      }
    ];
    for(const fn of tryFormats){
      const d = fn(t);
      if(d && d instanceof Date && !isNaN(d)) return d;
    }
    return null;
  }

  function calcMetrics(){
    if(!mappedRows.length) return null;
    const totalPosts = mappedRows.length;
    let minDate = mappedRows[0].datetime, maxDate = mappedRows[0].datetime;
    let sumInter=0, sumImp=0, sumReach=0;

    const byHour = new Map(); // hour -> {sumInter, count}
    for(const r of mappedRows){
      sumInter += r.interactions;
      sumImp += r.impressions;
      sumReach += r.reach;
      if(r.datetime){
        if(r.datetime < minDate) minDate = r.datetime;
        if(r.datetime > maxDate) maxDate = r.datetime;
      }
      if(r.hour!=null){
        const h = r.hour;
        if(!byHour.has(h)) byHour.set(h, {sum:0, count:0});
        const obj = byHour.get(h);
        obj.sum += r.interactions;
        obj.count += 1;
      }
    }

    const erImp = sumImp>0 ? sumInter/sumImp : 0;
    const erReach = sumReach>0 ? sumInter/sumReach : 0;

    const hourArr = Array.from(byHour.entries()).map(([h,v])=>({hour:h, avg:v.sum/Math.max(1,v.count)}));
    hourArr.sort((a,b)=>b.avg - a.avg);
    const topHours = hourArr.slice(0,3);

    function topPosts(mode='abs'){
      const arr = mappedRows.map((r,i)=>({i, title:r.title||`Post #${i+1}`, interactions:r.interactions, rate:r.rateImp, impressions:r.impressions}));
      if(mode==='rate'){
        arr.sort((a,b)=> (b.rate||0) - (a.rate||0));
      }else{
        arr.sort((a,b)=> b.interactions - a.interactions);
      }
      return arr.slice(0,3);
    }

    return { totalPosts, minDate, maxDate, sumInter, erImp, erReach, topHours, topPosts };
  }

  function formatPct(x){ return (x*100).toFixed(2).replace('.', ',') + '%'; }
  function formatInt(n){ return (Math.round(n)).toLocaleString('pt-BR'); }
  function formatDate(d){
    try{
      return d.toLocaleDateString('pt-BR', { day:'2-digit', month:'2-digit', year:'numeric'});
    }catch(e){ return '—'; }
  }
  function pad2(n){ return n.toString().padStart(2,'0'); }

  function renderPreview(){
    // head
    elTHead.innerHTML = '<tr>' + headers.map(h=>`<th>${h}</th>`).join('') + '</tr>';
    // first up to 20 rows
    const max = Math.min(20, rows.length);
    let html = '';
    for(let i=0;i<max;i++){
      const r = rows[i];
      html += '<tr>' + headers.map(h=>`<td>${(r[h]??'')}</td>`).join('') + '</tr>';
    }
    elTBody.innerHTML = html;
  }

  function renderResults(mode='abs'){
    const M = calcMetrics();
    if(!M) return;

    document.getElementById('m_posts').textContent = formatInt(M.totalPosts);
    document.getElementById('m_periodo').textContent = `${formatDate(M.minDate)} a ${formatDate(M.maxDate)}`;
    document.getElementById('m_interacoes').textContent = formatInt(M.sumInter);
    document.getElementById('m_er').textContent = `${formatPct(M.erReach)} • ${formatPct(M.erImp)}`;

    const ulH = document.getElementById('lista_horarios');
    ulH.innerHTML = M.topHours.map(x=>`<li><strong>${pad2(x.hour)}:00</strong> — média de ${formatInt(x.avg)} interações</li>`).join('');

    const ulP = document.getElementById('lista_posts');
    const tops = M.topPosts(mode);
    ulP.innerHTML = tops.map((x,idx)=> `<li><strong>${idx+1}º</strong> ${x.title} — ${formatInt(x.interactions)} interações ${x.impressions?`(${formatPct((x.interactions/Math.max(1,x.impressions)))})`:''}</li>`).join('');


  // Salvar insights no localStorage por usuário (se houver)
  try{
    const uRaw = localStorage.getItem('currentUser') || localStorage.getItem('userInfo');
    if(uRaw){
      const u = JSON.parse(uRaw);
      const email = u.email || u.responsavelEmail || (u.usuario && u.usuario.email);
      if(email){
        const save = {
          topHours: M.topHours,
          topPostsAbs: M.topPosts('abs'),
          topPostsRate: M.topPosts('rate'),
          sumInter: M.sumInter,
          totalPosts: M.totalPosts,
          period: {min: M.minDate, max: M.maxDate},
          erImp: M.erImp, erReach: M.erReach
        };
        localStorage.setItem('insights_'+email, JSON.stringify(save));
      }
    }
  }catch(e){/* ignore */}

  }

  function rowsArray(table){
    const [head, ...body] = table;
    const headers = head;
    return {
      headers,
      rows: body.filter(r => r.some(c=>String(c||'').trim()!=='')).map(r=>{
        const o = {};
        headers.forEach((h,idx)=> o[h]= r[idx] ?? '');
        return o;
      })
    };
  }

  // Event handlers
  elFile?.addEventListener('change', ()=>{
    const f = elFile.files?.[0];
    if(f) elInfo.textContent = `${f.name} • ${(f.size/1024/1024).toFixed(2)} MB`;
    else elInfo.textContent = 'Nenhum arquivo';
  });

  elBtnParse?.addEventListener('click', ()=>{
    const f = elFile.files?.[0];
    if(!f){ alert('Selecione um arquivo CSV.'); return; }
    if(f.size > 20*1024*1024){ alert('Arquivo muito grande (máx 20MB).'); return; }
    const reader = new FileReader();
    reader.onload = (e)=>{
      const text = e.target.result;
      const table = parseCSV(text);
      if(!table || !table.length){ alert('CSV vazio ou inválido.'); return; }
      const obj = rowsArray(table);
      headers = obj.headers;
      rows = obj.rows;
      renderPreview();
      buildMappingUI();
      elMapCard.style.display = '';
      window.scrollTo({top: elMapCard.offsetTop - 16, behavior: 'smooth'});
    };
    reader.readAsText(f, 'utf-8');
  });

  elBtnDemo?.addEventListener('click', ()=>{
    // Small demo dataset
    const demo = `Data,Impressões,Reach,Likes,Comentários,Compartilhamentos,Saves,Título
01/09/2025 18:30,1200,900,120,30,10,6,Teclado mecânico novo
02/09/2025 12:15,800,650,60,12,5,3,Promoção de periféricos
03/09/2025 21:05,1500,1100,210,44,20,11,Setup gamer minimalista
04/09/2025 09:20,600,500,30,6,3,2,Cafezinho e unboxing
05/09/2025 20:10,1700,1300,250,55,25,13,Review do headset X`;
    const table = parseCSV(demo);
    const obj = rowsArray(table);
    headers = obj.headers;
    rows = obj.rows;
    elInfo.textContent = 'Demo.csv • 0.00 MB';
    renderPreview();
    buildMappingUI();
    elMapCard.style.display = '';
    window.scrollTo({top: elMapCard.offsetTop - 16, behavior: 'smooth'});
  });

  elBtnMap?.addEventListener('click', ()=>{
    if(!applyMapping()) return;
    elResCard.style.display = '';
    renderResults(rankModeSel.value);
    window.scrollTo({top: elResCard.offsetTop - 16, behavior: 'smooth'});
  });

  rankModeSel?.addEventListener('change', ()=>{
    if(mappedRows.length) renderResults(rankModeSel.value);
  });

  elBtnPrint?.addEventListener('click', ()=>{
    window.print();
  });
})();