# Digitalize

O Digitalize é um projeto acadêmico de desenvolvimento e pesquisa criado para ajudar microempreendedores a melhorarem sua presença online.

A proposta do sistema é oferecer uma solução digital simples e acessível para pequenos negócios que ainda não têm estrutura, tempo ou conhecimento técnico para organizar sua comunicação e suas ações de marketing digital.

O projeto nasceu a partir de entrevistas, análise de mercado e levantamento de dificuldades reais enfrentadas por microempreendedores, como falta de orientação, pouco tempo para gerir redes sociais e ausência de apoio técnico. A partir disso, a equipe desenvolveu um conjunto de ferramentas e diretrizes voltadas para o dia a dia desses profissionais.

Em essência, o Digitalize busca facilitar o acesso de pequenos empreendedores a práticas de marketing digital e gestão de presença online.

## Versão de portfólio

Esta versão publicada no meu GitHub pessoal é uma continuação individual do projeto, usada para portfólio, estudos e melhorias técnicas.

O trabalho original foi desenvolvido em grupo no contexto acadêmico da PUC Minas. Esta versão mantém a base do projeto entregue pela equipe, mas também registra as partes em que atuei com mais profundidade e os pontos que pretendo evoluir individualmente.

## Status da versão de portfólio

- A aplicação principal desta versão roda pelo backend Java/Spark em `Codigo/src/main/java`;
- O front-end estático é servido pelo próprio backend e fica em `Codigo/src/main/resources/front-end/webapp/public`;
- O PostgreSQL é necessário para testar o fluxo completo de usuários, empresas, importações e posts;
- A integração com Azure OpenAI é opcional. Quando as variáveis não estão configuradas, o Guia de Postagem usa fallback local para manter a demonstração funcional;
- Arquivos de build, IDE, dados locais, conflitos de merge e credenciais reais não fazem parte da versão pública do portfólio.

## Minhas principais contribuições

- Desenvolvimento do backend em Java com Spark;
- Criação e ajuste de rotas da API REST;
- Integração com PostgreSQL;
- Modelagem e ajustes no banco de dados;
- Integração com Azure OpenAI;
- Desenvolvimento da lógica do Guia de Postagem;
- Correções na comunicação entre front-end e backend;
- Ajustes no fluxo de importação e análise de posts.

## Alunos integrantes da equipe

- Eduardo Murta de Abreu
- Jamille Micaele Soares Ferreira
- João Pedro Costa e Silva
- Larissa Varella Araújo

## Professores responsáveis

- Amália Soares Vieira de Vasconcelos
- Max do Val Machado

## Instruções de utilização

O projeto contém uma aplicação Java com Spark, integração com PostgreSQL e arquivos de front-end estático.

## Backend oficial

Nesta versão de portfólio, o backend oficial é o Java com Spark, localizado em `Codigo/src/main/java`. O front-end estático fica em `Codigo/src/main/resources/front-end/webapp/public` e é servido pelo próprio backend Spark.

O servidor Node/Express mantido em `Codigo/src/main/resources/front-end/webapp/server.js` é um protótipo legado das primeiras sprints. Ele permanece no repositório apenas como referência histórica do desenvolvimento, mas não deve ser usado como backend principal.

Para executar a versão backend oficial, é necessário ter:

- Java 17, conforme indicado em `.java-version`;
- Maven;
- PostgreSQL configurado;
- Variáveis de ambiente de banco de dados, quando aplicável.

Use o arquivo `.env.example` como referência para configurar o ambiente local. Ele lista as variáveis esperadas sem expor credenciais reais. Para usar localmente, copie para `.env` e preencha os valores necessários; o backend Java carrega esse arquivo automaticamente quando ele existir na raiz do projeto ou na pasta pai de execução.

Por padrão, o backend Java tenta usar as variáveis `DB_URL`, `DB_USER` e `DB_PASS`. Caso elas não estejam configuradas, o projeto usa o banco local `digitalize` em `localhost:5432`.

Para criar um usuário administrador inicial durante a migração do banco, configure também:

- `DIGITALIZE_ADMIN_EMAIL`;
- `DIGITALIZE_ADMIN_PASSWORD`.

Na pasta `Codigo`, o projeto pode ser compilado e executado com Maven:

```bash
mvn package
java -jar target/backend-fixed-0.1.0.jar
```

O servidor sobe, por padrão, na porta `8080`, ou na porta definida pela variável de ambiente `PORT`.

Depois que o servidor iniciar, acesse:

- Aplicação web: `http://localhost:8080`;
- Health check da API: `http://localhost:8080/health`;
- Painel administrativo: `http://localhost:8080/admin.html`.

Fluxo principal para testar a versão de portfólio:

1. Criar um usuário comum pela tela de login;
2. Cadastrar uma empresa pelo site;
3. Aprovar a empresa no painel administrativo;
4. Entrar em "Minha Empresa";
5. Importar um CSV em `importacao.html`;
6. Gerar o Guia de Postagem.

Observações técnicas desta versão:

- As senhas de novos usuários são armazenadas com hash PBKDF2 no backend Java;
- O Guia de Postagem funciona com Azure OpenAI quando as variáveis forem configuradas, mas possui fallback local para manter a aplicação utilizável em ambiente de portfólio;
- O arquivo `.env` real não deve ser versionado. Use apenas `.env.example` como referência pública.
