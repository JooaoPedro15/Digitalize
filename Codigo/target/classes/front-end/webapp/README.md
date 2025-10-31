# Importação de CSV — Frontend

Arquivos adicionados:
- `public/importacao.html`
- `public/importacao.js`

Foi adicionado também um link **Importar CSV** no menu de `public/index.html` (navbar).

## Como rodar (frontend-only)

### Opção 1 — Abrir o arquivo
Abra `public/importacao.html` diretamente no navegador.

### Opção 2 — Servidor estático com Python
```bash
cd public
python -m http.server 5500
# acesse http://localhost:5500/importacao.html
```

### Opção 3 — Servidor estático com Node
```bash
cd public
npx http-server -p 5500
# acesse http://localhost:5500/importacao.html
```

## Como usar
1. Clique em **Ler CSV** e selecione seu arquivo (com cabeçalho).
2. Ajuste o **mapeamento** de colunas (auto-detectado) e clique em **Aplicar mapeamento**.
3. Veja **Resumo**, **Top horários**, **Top posts** e **Prévia**.
4. Para PDF 1 página, use **Imprimir PDF** (usa impressão do navegador).
