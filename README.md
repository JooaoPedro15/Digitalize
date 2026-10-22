# Digitalize

O Digitalize é um projeto acadêmico de desenvolvimento e pesquisa criado para ajudar microempreendedores a melhorarem sua presença online.

A proposta do sistema é oferecer uma solução digital simples e acessível para pequenos negócios que ainda não têm estrutura, tempo ou conhecimento técnico para organizar sua comunicação e suas ações de marketing digital.

O projeto nasceu a partir de entrevistas, análise de mercado e levantamento de dificuldades reais enfrentadas por microempreendedores, como falta de orientação, pouco tempo para gerir redes sociais e ausência de apoio técnico. A partir disso, a equipe desenvolveu um conjunto de ferramentas e diretrizes voltadas para o dia a dia desses profissionais.

Em essência, o Digitalize busca facilitar o acesso de pequenos empreendedores a práticas de marketing digital e gestão de presença online.

## Versão de portfólio

Esta versão publicada no meu GitHub pessoal é uma continuação individual do projeto, usada para portfólio, estudos e melhorias técnicas.

O trabalho original foi desenvolvido em grupo no contexto acadêmico da PUC Minas. Esta versão mantém a base do projeto entregue pela equipe, mas também registra as partes em que atuei com mais profundidade e os pontos que pretendo evoluir individualmente.

## Status da versão de portfólio

- **Site no ar:** https://digitalize-vaux.onrender.com (use o botão "Entrar como demonstração" na tela de login);
- Backend em **Java 17 + Spring Boot**, com Spring Data JPA, Bean Validation e migrações com Flyway, em `Codigo/src/main/java/com/digitalize`;
- O front-end estático é servido pelo próprio Spring Boot a partir de `Codigo/src/main/resources/static`;
- O PostgreSQL é necessário para testar o fluxo completo de usuários, empresas, importações e posts;
- A integração com Azure OpenAI é opcional. Quando as variáveis não estão configuradas, o Guia de Postagem é gerado localmente a partir das métricas;
- Arquivos de build, IDE, dados locais e credenciais reais não fazem parte da versão pública do portfólio.

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

## Arquitetura

O backend é organizado por funcionalidade, cada pacote com entidade, repositório, serviço e controller:

| Pacote | Responsabilidade |
|---|---|
| `usuario` | Cadastro, login (senha com BCrypt) e administrador inicial |
| `empresa` | Cadastro e aprovação das empresas |
| `canal` | Perfis da empresa nas redes sociais |
| `importacao` | Registro dos CSVs de métricas importados |
| `post` | Publicações e suas métricas |
| `guia` | Análise de engajamento e geração do Guia de Postagem |
| `demo` | Conta de demonstração com dados de exemplo |
| `comum`, `config` | Tratamento de erros, health check e conexão com o banco |

O banco fica no schema `digitalize` e é criado pelo Flyway a partir de `Codigo/src/main/resources/db/migration`. O Hibernate apenas valida se as entidades batem com as tabelas (`ddl-auto=validate`).

## Como executar localmente

É necessário ter Java 17 (ou mais recente) e um PostgreSQL. O Maven não precisa estar instalado: o projeto usa o Maven Wrapper.

Use o arquivo `.env.example` como referência: copie para `.env` e preencha os valores. O Spring Boot carrega esse arquivo automaticamente quando ele está na raiz do repositório ou na pasta `Codigo`.

Variáveis principais:

- `DATABASE_URL`: connection string no formato `postgresql://usuario:senha@host/banco` (Neon, Render). Tem prioridade sobre as demais;
- `DB_URL`, `DB_USER`, `DB_PASS`: alternativa no formato JDBC. Sem nenhuma delas, o projeto usa `jdbc:postgresql://localhost:5432/digitalize`;
- `DIGITALIZE_ADMIN_EMAIL` e `DIGITALIZE_ADMIN_PASSWORD`: criam o administrador inicial;
- `DIGITALIZE_DEMO=true`: cria a conta de demonstração.

Na pasta `Codigo`:

```bash
./mvnw spring-boot:run
```

Para rodar os testes:

```bash
./mvnw test
```

O servidor sobe na porta `8080`, ou na porta definida em `PORT`. Depois de iniciar:

- Aplicação web: `http://localhost:8080`;
- Health check: `http://localhost:8080/health`;
- Painel administrativo: `http://localhost:8080/admin.html`.

Fluxo principal para testar:

1. Criar um usuário comum pela tela de login;
2. Cadastrar uma empresa pelo site;
3. Aprovar a empresa no painel administrativo;
4. Entrar em "Minha Empresa";
5. Importar um CSV em `importacao.html`;
6. Gerar o Guia de Postagem.

## Modo demonstração

Com `DIGITALIZE_DEMO=true`, o backend cria na inicialização uma conta de demonstração com a empresa fictícia "Doce Lar Confeitaria", um canal e posts de exemplo dos últimos 28 dias. A tela de login passa a exibir o botão "Entrar como demonstração", que leva direto para "Minha Empresa" com o Guia de Postagem já disponível.

Os posts de exemplo são recriados a cada reinicialização, com datas relativas ao dia atual.

## Deploy

O repositório inclui um `Dockerfile` (build com Maven e execução com Java 17, com limites de memória para o plano gratuito) e um `render.yaml` para publicação no [Render](https://render.com) com banco PostgreSQL externo (por exemplo, [Neon](https://neon.tech)).

1. Criar um banco PostgreSQL no Neon e copiar a connection string;
2. No Render, criar um novo Blueprint apontando para este repositório;
3. Preencher a variável `DATABASE_URL` com a connection string do Neon;
4. Aguardar o build. A aplicação fica disponível no endereço gerado pelo Render.
