package com.digitalize.seguranca;

import com.digitalize.empresa.EmpresaController;
import com.digitalize.empresa.EmpresaService;
import com.digitalize.importacao.ImportacaoController;
import com.digitalize.importacao.ImportacaoService;
import com.digitalize.usuario.UsuarioController;
import com.digitalize.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confere as regras do SecurityConfig sem banco: os serviços são simulados (mocks).
 */
@WebMvcTest(controllers = {UsuarioController.class, EmpresaController.class, ImportacaoController.class})
@Import({SecurityConfig.class, JwtConfig.class})
class RegrasDeAcessoTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UsuarioService usuarioService;
    @MockitoBean
    EmpresaService empresaService;
    @MockitoBean
    ImportacaoService importacaoService;
    @MockitoBean
    TokenService tokenService;

    private static JwtRequestPostProcessor usuarioComum() {
        return jwt().jwt(token -> token.subject("2").claim("email", "ana@teste.com").claim("papel", "USUARIO"))
                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"));
    }

    private static JwtRequestPostProcessor admin() {
        return jwt().jwt(token -> token.subject("1").claim("email", "admin@teste.com").claim("papel", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void semLoginRecebe401ComMensagem() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Faça login para continuar."));
    }

    @Test
    void tokenInvalidoRecebe401() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", "Bearer token-falsificado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComumNaoAcessaRotasDeAdmin() throws Exception {
        mvc.perform(get("/api/usuarios").with(usuarioComum())).andExpect(status().isForbidden());
        mvc.perform(get("/api/empresas").with(usuarioComum())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/empresas/12345678000100").with(usuarioComum())).andExpect(status().isForbidden());
    }

    @Test
    void adminAcessaRotasDeAdmin() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of());

        mvc.perform(get("/api/usuarios").with(admin())).andExpect(status().isOk());
    }

    @Test
    void vitrineELoginSaoPublicos() throws Exception {
        when(empresaService.listarPublicas()).thenReturn(List.of());

        mvc.perform(get("/api/empresas/publicas")).andExpect(status().isOk());
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()); // chegou ao controller: falhou só na validação
    }

    @Test
    void importarPostsExigeLogin() throws Exception {
        String rota = "/api/empresas/12345678000100/importacoes";
        String corpo = "{\"arquivo\":\"insta.csv\",\"posts\":[{\"publicado_em\":\"2026-10-01T10:00:00\"}]}";

        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(rota).with(usuarioComum()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }
}
