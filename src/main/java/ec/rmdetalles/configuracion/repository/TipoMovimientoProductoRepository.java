package ec.rmdetalles.configuracion.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TipoMovimientoProductoRepository {
    private final JdbcTemplate jdbc;
    public TipoMovimientoProductoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<TipoMovimientoProductoDato> listar(UUID negocioId) {
        return jdbc.query("""
                SELECT codigo, nombre, factor_stock, permite_negativo, activo
                FROM rm_detalles.tipos_movimiento_producto
                WHERE negocio_id = ?
                ORDER BY codigo
                """, (rs, row) -> map(rs), negocioId);
    }

    public Optional<TipoMovimientoProductoDato> buscar(UUID negocioId, String codigo) {
        List<TipoMovimientoProductoDato> filas = jdbc.query("""
                SELECT codigo, nombre, factor_stock, permite_negativo, activo
                FROM rm_detalles.tipos_movimiento_producto
                WHERE negocio_id = ? AND codigo = ?
                """, (rs, row) -> map(rs), negocioId, codigo);
        return filas.stream().findFirst();
    }

    public boolean tieneMovimientos(UUID negocioId, String codigo) {
        Boolean existe = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM rm_detalles.movimientos_productos
                    WHERE negocio_id = ? AND tipo_codigo = ?
                )
                """, Boolean.class, negocioId, codigo);
        return Boolean.TRUE.equals(existe);
    }

    public void guardar(UUID negocioId, String codigo, String nombre, short factorStock, boolean activo) {
        jdbc.update("""
                INSERT INTO rm_detalles.tipos_movimiento_producto
                    (negocio_id, codigo, nombre, factor_stock, permite_negativo, activo)
                VALUES (?, ?, ?, ?, FALSE, ?)
                ON CONFLICT (negocio_id, codigo) DO UPDATE SET
                    nombre = EXCLUDED.nombre,
                    factor_stock = EXCLUDED.factor_stock,
                    permite_negativo = FALSE,
                    activo = EXCLUDED.activo
                """, negocioId, codigo, nombre, factorStock, activo);
    }

    private TipoMovimientoProductoDato map(ResultSet rs) throws SQLException {
        return new TipoMovimientoProductoDato(
                rs.getString("codigo"), rs.getString("nombre"), rs.getShort("factor_stock"),
                rs.getBoolean("permite_negativo"), rs.getBoolean("activo"));
    }
}
