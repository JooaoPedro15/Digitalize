package com.digitalize.demo;

import com.digitalize.canal.Canal;
import com.digitalize.canal.CanalService;
import com.digitalize.empresa.Empresa;
import com.digitalize.empresa.EmpresaRepository;
import com.digitalize.empresa.StatusEmpresa;
import com.digitalize.importacao.Importacao;
import com.digitalize.importacao.ImportacaoRepository;
import com.digitalize.importacao.ImportacaoService;
import com.digitalize.importacao.StatusImportacao;
import com.digitalize.post.Post;
import com.digitalize.post.PostRepository;
import com.digitalize.usuario.Papel;
import com.digitalize.usuario.Usuario;
import com.digitalize.usuario.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cria uma conta de demonstração com empresa, canal e posts de exemplo.
 *
 * Só roda com DIGITALIZE_DEMO=true. Os posts são recriados a cada
 * inicialização com datas relativas ao dia atual, para que o Guia de
 * Postagem (que analisa os últimos 30 dias) sempre tenha dados.
 */
@Component
public class DemoSeeder implements ApplicationRunner {

    public static final String EMAIL = "demo@digitalize.app";
    static final String SENHA = "demo-digitalize";

    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);
    private static final String NOME = "Visitante Demo";
    private static final String CNPJ = "12345678000100";
    private static final String ARQUIVO = "demo-instagram.csv";

    private static final String[] LEGENDAS = {
        "Bastidores da produção do bolo de pote da semana",
        "Promoção relâmpago: 2 brigadeiros gourmet por R$10",
        "Conheça a história da nossa confeitaria",
        "Receita rápida de brigadeiro de colher",
        "Cliente do mês: obrigado pela parceria!",
        "Novo sabor chegando: pistache com frutas vermelhas",
        "Como montamos uma caixa de presente",
        "Enquete: qual sabor você quer na próxima fornada?",
        "Dicas para conservar seus doces por mais tempo",
        "Tour pela cozinha antes da abertura",
        "Encomendas abertas para o fim de semana",
        "Antes e depois da decoração do bolo de aniversário"
    };
    private static final int[] HORAS = {9, 12, 18, 20, 11, 19, 21, 8, 13, 18, 20, 17};

    private final boolean ativo;
    private final UsuarioService usuarioService;
    private final EmpresaRepository empresas;
    private final CanalService canalService;
    private final ImportacaoService importacaoService;
    private final ImportacaoRepository importacoes;
    private final PostRepository posts;

    public DemoSeeder(@Value("${digitalize.demo.ativo}") boolean ativo,
                      UsuarioService usuarioService,
                      EmpresaRepository empresas,
                      CanalService canalService,
                      ImportacaoService importacaoService,
                      ImportacaoRepository importacoes,
                      PostRepository posts) {
        this.ativo = ativo;
        this.usuarioService = usuarioService;
        this.empresas = empresas;
        this.canalService = canalService;
        this.importacaoService = importacaoService;
        this.importacoes = importacoes;
        this.posts = posts;
    }

    public boolean ativo() {
        return ativo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!ativo) {
            return;
        }

        Usuario usuario = usuarioService.garantir(NOME, EMAIL, SENHA, Papel.USUARIO);
        Empresa empresa = garantirEmpresa(usuario);
        Canal canal = canalService.garantirCanalPadrao(empresa);
        recriarPosts(canal);
        log.info("Conta demo pronta: {}", EMAIL);
    }

    private Empresa garantirEmpresa(Usuario usuario) {
        Empresa empresa = empresas.findByCnpj(CNPJ).orElseGet(() -> new Empresa(CNPJ));
        empresa.setNomeFantasia("Doce Lar Confeitaria");
        empresa.setRazaoSocial("Doce Lar Confeitaria LTDA (exemplo)");
        empresa.setSegmento("Alimentação");
        empresa.setEndereco("Belo Horizonte - MG");
        empresa.setEmailContato(EMAIL);
        empresa.setUsuario(usuario);
        empresa.setStatus(StatusEmpresa.APROVADA);
        return empresas.save(empresa);
    }

    private void recriarPosts(Canal canal) {
        posts.deleteByCanalId(canal.getId());
        importacoes.deleteByCanalId(canal.getId());

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(28);
        Importacao importacao = importacaoService.registrar(canal.getId(), ARQUIVO, inicio, hoje, StatusImportacao.PROCESSADA);

        List<Post> novos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            int hora = HORAS[i % HORAS.length];
            LocalDateTime publicadoEm = inicio.plusDays(1 + i).atTime(hora, (i * 7) % 60);
            String legenda = LEGENDAS[i % LEGENDAS.length] + (i >= LEGENDAS.length ? " (parte 2)" : "");

            // Posts à noite engajam mais, para o guia ter um padrão claro de horário
            boolean noite = hora >= 18;
            int views = 800 + (i * 137) % 1500 + (noite ? 900 : 0);
            int curtidas = views / (noite ? 9 : 18) + (i * 11) % 40;
            int comentarios = curtidas / 6 + i % 5;
            int compartilhamentos = curtidas / 10 + i % 3;

            Post post = new Post(canal, publicadoEm, legenda);
            post.atualizarMetricas(15 + (i * 9) % 45, (int) (views * 0.8), views, curtidas,
                    compartilhamentos, comentarios, curtidas / 5);
            post.setImportacao(importacao);
            novos.add(post);
        }
        posts.saveAll(novos);
    }
}
