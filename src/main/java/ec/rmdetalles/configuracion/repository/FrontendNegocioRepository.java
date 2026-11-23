package ec.rmdetalles.configuracion.repository;

import ec.rmdetalles.configuracion.domain.FrontendNegocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface FrontendNegocioRepository extends JpaRepository<FrontendNegocio, UUID> {
    boolean existsByOrigenAndActivoTrue(String origen);
    boolean existsByNegocio_CodigoAndOrigenAndActivoTrue(String negocioCodigo, String origen);
    List<FrontendNegocio> findByNegocio_IdOrderByOrigenAsc(UUID negocioId);
    Optional<FrontendNegocio> findByIdAndNegocio_Id(UUID id, UUID negocioId);
    boolean existsByNegocio_IdAndOrigenIgnoreCase(UUID negocioId, String origen);
    boolean existsByNegocio_IdAndOrigenIgnoreCaseAndIdNot(UUID negocioId, String origen, UUID id);
}
