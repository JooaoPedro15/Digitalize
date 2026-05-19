# Código do projeto

Esta pasta contém a aplicação principal do Digitalize.

## Backend oficial

O backend oficial da versão de portfólio é a aplicação Java com Spark:

- `src/main/java/com/Main.java`: ponto de entrada do servidor;
- `src/main/java/com/service`: regras de negócio e rotas principais;
- `src/main/java/com/dao`: acesso ao PostgreSQL;
- `src/main/java/com/model`: entidades do domínio.

O front-end estático usado pelo Spark fica em:

- `src/main/resources/front-end/webapp/public`

## Código legado

A pasta `src/main/resources/front-end/webapp` também contém um servidor Node/Express (`server.js`) usado como protótipo em fases anteriores do projeto acadêmico. Ele foi mantido como referência histórica, mas não é o backend principal desta versão.

Para rodar o projeto de portfólio, use o fluxo Java/Maven descrito no README da raiz.
