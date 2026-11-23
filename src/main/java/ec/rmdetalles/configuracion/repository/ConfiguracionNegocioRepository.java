package ec.rmdetalles.configuracion.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ConfiguracionNegocioRepository {
    private final JdbcTemplate jdbc;

    public ConfiguracionNegocioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ConfiguracionDato> listar(UUID negocioId) {
        return jdbc.query("""
                SELECT clave, valor::text AS valor_json, descripcion, actualizado_en
                FROM rm_detalles.configuraciones_negocio
                WHERE negocio_id = ?
                ORDER BY clave
                """, (rs, rowNum) -> map(rs), negocioId);
    }

    public Optional<ConfiguracionDato> buscar(UUID negocioId, String clave) {
        List<ConfiguracionDato> resultados = jdbc.query("""
                SELECT clave, valor::text AS valor_json, descripcion, actualizado_en
                FROM rm_detalles.configuraciones_negocio
                WHERE negocio_id = ? AND clave = ?
                """, (rs, rowNum) -> map(rs), negocioId, clave);
        return resultados.stream().findFirst();
    }

    public boolean actualizar(UUID negocioId, String clave, String valorJson) {
        int actualizados = jdbc.update("""
                UPDATE rm_detalles.configuraciones_negocio
                SET valor = ?::jsonb, actualizado_en = NOW()
                WHERE negocio_id = ? AND clave = ?
                """, valorJson, negocioId, clave);
        return actualizados == 1;
    }

    private ConfiguracionDato map(ResultSet rs) throws SQLException {
        return new ConfiguracionDato(
                rs.getString("clave"),
                rs.getString("valor_json"),
                rs.getString("descripcion"),
                rs.getObject("actualizado_en", OffsetDateTime.class)
        );
    }

    public record ConfiguracionDato(
            String clave,
            String valorJson,
            String descripcion,
            OffsetDateTime actualizadoEn
    ) { }
}
