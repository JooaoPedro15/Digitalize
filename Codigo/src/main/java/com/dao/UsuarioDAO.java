package com.dao;

import com.model.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario autenticar(String email, String senha) {
        // SQL ajustado para procurar na tabela correta (midiasocial) e ignorar maiúsculas/minúsculas
        String sql = "SELECT * FROM midiasocial.usuarios WHERE LOWER(email) = LOWER(?)";
        
        // Logs para você ver no Log Stream do Azure
        System.out.println("--- TENTATIVA DE LOGIN ---");
        System.out.println("Email recebido: [" + email + "]");

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String senhaBanco = rs.getString("senha");
                    String emailBanco = rs.getString("email");
                    String tipoBanco = rs.getString("tipo");
                    
                    System.out.println("Usuário encontrado: " + emailBanco);
                    
                    // Compara a senha (usando trim para evitar erros de espaço)
                    // Se você estiver usando senha pura (123456), isso vai funcionar.
                    if (senha.trim().equals(senhaBanco.trim())) {
                        System.out.println(">> SENHA OK! LOGIN SUCESSO <<");
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNome(rs.getString("nome"));
                        u.setEmail(emailBanco);
                        u.setSenha(senhaBanco);
                        u.setTipo(tipoBanco);
                        u.setAtivo(rs.getBoolean("ativo"));
                        return u;
                    } else {
                        System.out.println(">> SENHA INCORRETA <<");
                        System.out.println("Esperado: [" + senhaBanco + "] / Recebido: [" + senha + "]");
                    }
                } else {
                    System.out.println(">> EMAIL NÃO ENCONTRADO NO BANCO <<");
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO SQL NO LOGIN: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Usuario u) {
        String sql = "INSERT INTO midiasocial.usuarios (nome, email, senha, tipo, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail().trim().toLowerCase());
            ps.setString(3, u.getSenha().trim());
            ps.setString(4, u.getTipo());
            ps.setBoolean(5, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}