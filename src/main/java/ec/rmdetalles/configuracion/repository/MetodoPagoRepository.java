package ec.rmdetalles.configuracion.repository;

import ec.rmdetalles.configuracion.api.MetodoPagoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class MetodoPagoRepository {
    private final JdbcTemplate jdbc;

    public MetodoPagoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MetodoPagoResponse> listar(UUID negocioId) {
        return jdbc.query("""
                SELECT codigo, nombre, activo FROM rm_detalles.metodos_pago
                WHERE negocio_id = ? ORDER BY nombre, codigo
                """, (rs, row) -> new MetodoPagoResponse(
                rs.getString("codigo"), rs.getString("nombre"), rs.getBoolean("activo")),
                negocioId);
    }

    public void guardar(UUID negocioId, String codigo, String nombre, boolean activo) {
        jdbc.update("""
                INSERT INTO rm_detalles.metodos_pago (negocio_id, codigo, nombre, activo)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (negocio_id, codigo) DO UPDATE SET
                    nombre = EXCLUDED.nombre, activo = EXCLUDED.activo
                """, negocioId, codigo, nombre, activo);
    }
}
