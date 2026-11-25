package ec.rmdetalles.configuracion.repository;

import ec.rmdetalles.configuracion.api.EstadoPedidoRequest;
import ec.rmdetalles.configuracion.api.EstadoPedidoResponse;
import ec.rmdetalles.configuracion.api.TransicionPedidoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ConfiguracionPedidoRepository {
    private final JdbcTemplate jdbc;

    public ConfiguracionPedidoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void bloquearNegocio(UUID negocioId) {
        jdbc.queryForObject("SELECT id FROM rm_detalles.negocios WHERE id = ? FOR UPDATE",
                UUID.class, negocioId);
    }

    public List<EstadoPedidoResponse> listarEstados(UUID negocioId) {
        return jdbc.query("""
                SELECT codigo, nombre, orden, es_inicial, es_final, activo
                FROM rm_detalles.estados_pedido WHERE negocio_id = ?
                ORDER BY orden, codigo
                """, (rs, n) -> new EstadoPedidoResponse(
                rs.getString("codigo"), rs.getString("nombre"), rs.getShort("orden"),
                rs.getBoolean("es_inicial"), rs.getBoolean("es_final"), rs.getBoolean("activo")),
                negocioId);
    }

    public void guardarEstado(UUID negocioId, String codigo, EstadoPedidoRequest estado) {
        jdbc.update("""
                INSERT INTO rm_detalles.estados_pedido
                    (negocio_id, codigo, nombre, orden, es_inicial, es_final, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (negocio_id, codigo) DO UPDATE SET
                    nombre = EXCLUDED.nombre, orden = EXCLUDED.orden,
                    es_inicial = EXCLUDED.es_inicial, es_final = EXCLUDED.es_final,
                    activo = EXCLUDED.activo
                """, negocioId, codigo, estado.nombre().trim(), estado.orden(),
                estado.inicial(), estado.finalizado(), estado.activo());
    }

    public List<TransicionPedidoResponse> listarTransiciones(UUID negocioId) {
        return jdbc.query("""
                SELECT estado_origen, estado_destino, activo
                FROM rm_detalles.transiciones_pedido WHERE negocio_id = ?
                ORDER BY estado_origen, estado_destino
                """, (rs, n) -> new TransicionPedidoResponse(
                rs.getString("estado_origen"), rs.getString("estado_destino"),
                rs.getBoolean("activo")), negocioId);
    }

    public void guardarTransicion(UUID negocioId, String origen, String destino, boolean activo) {
        jdbc.update("""
                INSERT INTO rm_detalles.transiciones_pedido
                    (negocio_id, estado_origen, estado_destino, activo)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (negocio_id, estado_origen, estado_destino)
                DO UPDATE SET activo = EXCLUDED.activo
                """, negocioId, origen, destino, activo);
    }
}
