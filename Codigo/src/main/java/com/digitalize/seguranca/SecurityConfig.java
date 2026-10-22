package com.digitalize.seguranca;

import com.digitalize.comum.Mensagem;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * Regras de acesso da API.
 *
 * - Público: front-end, health check, login, login demo, cadastro e vitrine de empresas aprovadas;
 * - Admin: listar todas as empresas e usuários, aprovar/rejeitar, excluir e cadastrar pelo painel;
 * - Demais rotas da API: qualquer usuário logado (a posse da empresa é conferida nos serviços).
 */
@Configuration
public class SecurityConfig {

    private final JsonMapper jsonMapper;

    public SecurityConfig(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API sem estado: o token vai no cabeçalho Authorization, não em cookie,
            // então não há sessão no servidor nem risco de CSRF.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(regras -> regras
                .requestMatchers(HttpMethod.POST, "/api/login", "/api/login/demo", "/api/usuarios").permitAll()
                .requestMatchers(HttpMethod.GET, "/health", "/error", "/api/demo", "/api/empresas/publicas").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/empresas").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/empresas/*/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/empresas/*").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                .requestMatchers("/api/**", "/canais/**", "/importacoes/**", "/posts/**", "/empresa/**").authenticated()

                // Arquivos estáticos do front-end (HTML, CSS, JS, imagens)
                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(recurso -> recurso
                .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePapel()))
                .authenticationEntryPoint(naoAutenticado())
                .accessDeniedHandler(acessoNegado()))
            .exceptionHandling(erros -> erros
                .authenticationEntryPoint(naoAutenticado())
                .accessDeniedHandler(acessoNegado()));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Transforma a claim "papel" do token em ROLE_USUARIO ou ROLE_ADMIN. */
    private JwtAuthenticationConverter conversorDePapel() {
        JwtGrantedAuthoritiesConverter papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName("papel");
        papeis.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(papeis);
        return conversor;
    }

    private AuthenticationEntryPoint naoAutenticado() {
        return (request, response, e) ->
                escrever(response, HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
    }

    private AccessDeniedHandler acessoNegado() {
        return (request, response, e) ->
                escrever(response, HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação.");
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getOutputStream(), Mensagem.erro(mensagem));
    }
}
