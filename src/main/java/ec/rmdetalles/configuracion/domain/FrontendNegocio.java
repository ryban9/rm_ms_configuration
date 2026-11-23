package ec.rmdetalles.configuracion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "frontends_negocio", schema = "rm_detalles")
public class FrontendNegocio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @Column(nullable = false, length = 500)
    private String origen;

    @Column(name = "host_publico", length = 253)
    private String hostPublico;

    @Column(nullable = false)
    private boolean activo;

    protected FrontendNegocio() { }

    public FrontendNegocio(Negocio negocio, String origen, String hostPublico) {
        this.negocio = negocio;
        this.origen = origen;
        this.hostPublico = hostPublico;
        this.activo = true;
    }

    public UUID getId() { return id; }
    public String getOrigen() { return origen; }
    public String getHostPublico() { return hostPublico; }
    public boolean isActivo() { return activo; }

    public void actualizar(String origen, String hostPublico, Boolean activo) {
        this.origen = origen;
        this.hostPublico = hostPublico;
        if (activo != null) this.activo = activo;
    }

    public void desactivar() { this.activo = false; }
}
