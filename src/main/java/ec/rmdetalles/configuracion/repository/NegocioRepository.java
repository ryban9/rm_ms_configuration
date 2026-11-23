package ec.rmdetalles.configuracion.repository;

import ec.rmdetalles.configuracion.domain.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NegocioRepository extends JpaRepository<Negocio, UUID> {
    Optional<Negocio> findByCodigoAndActivoTrue(String codigo);
}
