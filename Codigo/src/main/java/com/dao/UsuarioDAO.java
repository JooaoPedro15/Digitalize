package com.dao;

import com.model.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario autenticar(String email, String senha) {
        // SQL ajustado para ignorar se o email foi digitado com Maiúsculas/minúsculas
        String sql = "SELECT * FROM usuarios WHERE LOWER(email) = LOWER(?)";
        
        // Logs para Debug no Azure (Aparecem no Log Stream)
        System.out.println("--- TENTATIVA DE LOGIN ---");
        System.out.println("Email recebido do site: [" + email + "]");
        
        // ATENÇÃO: Se a senha estiver criptografada no banco, a comparação direta falha.
        // Se estiver texto puro (123456), vai funcionar.

        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Remove espaços em branco antes e depois (.trim)
            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String senhaBanco = rs.getString("senha");
                    String emailBanco = rs.getString("email");
                    String tipoBanco = rs.getString("tipo");
                    
                    System.out.println("Usuário encontrado no banco: " + emailBanco);
                    System.out.println("Senha gravada no banco: [" + senhaBanco + "]");

                    // COMPARAÇÃO:
                    // Usa .trim() para garantir que espaços invisíveis não atrapalhem
                    if (senha.trim().equals(senhaBanco.trim())) {
                        System.out.println(">> SENHAS BATEM! LOGIN SUCESSO <<");
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNome(rs.getString("nome"));
                        u.setEmail(emailBanco);
                        u.setSenha(senhaBanco);
                        u.setTipo(tipoBanco);
                        u.setAtivo(rs.getBoolean("ativo"));
                        return u;
                    } else {
                        System.out.println(">> SENHAS NÃO BATEM! <<");
                        System.out.println("Senha digitada: [" + senha + "]");
                        System.out.println("Senha do banco: [" + senhaBanco + "]");
                        
                        // Dica sobre criptografia
                        if (senhaBanco.length() > 20) {
                            System.out.println("AVISO: A senha do banco parece ser um Hash (Criptografada).");
                            System.out.println("Se você cadastrou pelo site antigo, precisa resetar a senha para Texto Puro.");
                        }
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
        String sql = "INSERT INTO usuarios (nome, email, senha, tipo, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail().trim().toLowerCase()); // Salva email padronizado
            ps.setString(3, u.getSenha().trim()); // Salva senha sem espaços
            ps.setString(4, u.getTipo());
            ps.setBoolean(5, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}