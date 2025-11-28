package com.dao;

import com.model.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario autenticar(String email, String senha) {
        String sql = "SELECT * FROM midiasocial.usuarios WHERE LOWER(email) = LOWER(?)";
        
        try (Connection conn = DAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String senhaBanco = rs.getString("senha");
                    if (senha.trim().equals(senhaBanco.trim())) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNome(rs.getString("nome"));
                        u.setEmail(rs.getString("email"));
                        u.setSenha(senhaBanco);
                        u.setTipo(rs.getString("tipo"));
                        u.setAtivo(rs.getBoolean("ativo"));
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
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