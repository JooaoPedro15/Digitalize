package com.digitalize.seguranca;

import com.digitalize.comum.ApiException;
import com.digitalize.empresa.Empresa;
import com.digitalize.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioLogadoTest {

    private static Empresa empresaDoUsuario(long idDono) {
        Usuario dono = mock(Usuario.class);
        when(dono.getId()).thenReturn(idDono);
        Empresa empresa = new Empresa("12345678000100");
        empresa.setUsuario(dono);
        return empresa;
    }

    @Test
    void responsavelAcessaASuaEmpresa() {
        assertThat(new UsuarioLogado(5L, "ana@teste.com", false).podeAcessar(empresaDoUsuario(5L))).isTrue();
    }

    @Test
    void outroUsuarioRecebe403() {
        UsuarioLogado intruso = new UsuarioLogado(9L, "bia@teste.com", false);

        assertThatThrownBy(() -> intruso.exigirAcesso(empresaDoUsuario(5L)))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminAcessaQualquerEmpresa() {
        assertThat(new UsuarioLogado(1L, "admin@teste.com", true).podeAcessar(empresaDoUsuario(5L))).isTrue();
    }

    @Test
    void empresaSemResponsavelSoOAdminAcessa() {
        Empresa semDono = new Empresa("12345678000100");

        assertThat(new UsuarioLogado(5L, "ana@teste.com", false).podeAcessar(semDono)).isFalse();
        assertThat(new UsuarioLogado(1L, "admin@teste.com", true).podeAcessar(semDono)).isTrue();
    }
}
