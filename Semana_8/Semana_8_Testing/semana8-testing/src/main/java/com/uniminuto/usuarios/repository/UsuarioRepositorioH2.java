package com.uniminuto.usuarios.repository;

import com.uniminuto.usuarios.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * IMPLEMENTACIÓN CON JDBC PURO + H2
 *
 * Se usa H2 (base de datos en memoria) para que las pruebas de integración
 * no dependan de un servidor MySQL externo. El comportamiento SQL es idéntico.
 *
 * NOTA DIDÁCTICA: En un proyecto Spring Boot real, esta capa sería reemplazada
 * por JPA/Hibernate. Aquí usamos JDBC puro para que los estudiantes vean
 * exactamente qué sucede "debajo" del ORM y entiendan qué está probando
 * cada prueba de integración.
 */
public class UsuarioRepositorioH2 implements UsuarioRepositorio {

    private final Connection conexion;

    public UsuarioRepositorioH2(Connection conexion) {
        this.conexion = conexion;
        inicializarEsquema();
    }

    /**
     * Crea la tabla al instanciar el repositorio.
     * En pruebas de integración esto sucede al inicio de cada test class,
     * garantizando un estado limpio.
     */
    private void inicializarEsquema() {
        String sql = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                nombre  VARCHAR(100) NOT NULL,
                email   VARCHAR(150) NOT NULL UNIQUE,
                edad    INT NOT NULL,
                activo  BOOLEAN NOT NULL DEFAULT TRUE
            )
            """;
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar esquema H2", e);
        }
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() == null) {
            return insertar(usuario);
        } else {
            return actualizar(usuario);
        }
    }

    private Usuario insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, email, edad, activo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setInt(3, usuario.getEdad());
            ps.setBoolean(4, usuario.isActivo());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                usuario.setId(keys.getLong(1));
            }
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar usuario", e);
        }
    }

    private Usuario actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre=?, email=?, edad=?, activo=? WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setInt(3, usuario.getEdad());
            ps.setBoolean(4, usuario.isActivo());
            ps.setLong(5, usuario.getId());
            ps.executeUpdate();
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapearFila(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar por id", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapearFila(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar por email", e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario", e);
        }
    }

    @Override
    public boolean existePorEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar email", e);
        }
    }

    /** Convierte una fila del ResultSet en un objeto Usuario (mapeo O/R manual) */
    private Usuario mapearFila(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .id(rs.getLong("id"))
                .nombre(rs.getString("nombre"))
                .email(rs.getString("email"))
                .edad(rs.getInt("edad"))
                .activo(rs.getBoolean("activo"))
                .build();
    }
}
