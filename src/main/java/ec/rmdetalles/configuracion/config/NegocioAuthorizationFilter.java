package ec.rmdetalles.configuracion.config;

import ec.rmdetalles.configuracion.repository.AccesoNegocioProjection;
import ec.rmdetalles.configuracion.repository.AccesoNegocioRepository;
import ec.rmdetalles.configuracion.repository.FrontendNegocioRepository;
import ec.rmdetalles.configuracion.repository.NegocioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class NegocioAuthorizationFilter extends OncePerRequestFilter {
    private final AccesoNegocioRepository accesos;
    private final NegocioRepository negocios;
    private final FrontendNegocioRepository frontends;

    public NegocioAuthorizationFilter(
            AccesoNegocioRepository accesos,
            NegocioRepository negocios,
            FrontendNegocioRepository frontends
    ) {
        this.accesos = accesos;
        this.negocios = negocios;
        this.frontends = frontends;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/actuator/health") || "OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String codigo = request.getHeader("X-Negocio-Codigo");
        if (codigo == null || codigo.isBlank()) {
            responderError(response, 400, "Falta el encabezado X-Negocio-Codigo.");
            return;
        }
        codigo = codigo.trim().toUpperCase(Locale.ROOT);

        var negocio = negocios.findByCodigoAndActivoTrue(codigo);
        if (negocio.isEmpty()) {
            responderError(response, 404, "No se encontró un negocio activo con ese código.");
            return;
        }

        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()
                && !frontends.existsByNegocio_CodigoAndOrigenAndActivoTrue(codigo, origin)) {
            responderError(response, 403, "Este origen no está habilitado para este negocio.");
            return;
        }

        NegocioContext.set(negocio.get().getId());
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                UUID usuarioId;
                try {
                    usuarioId = UUID.fromString(jwtAuth.getToken().getSubject());
                } catch (RuntimeException ex) {
                    responderError(response, 401, "El identificador del usuario no es válido.");
                    return;
                }

                List<AccesoNegocioProjection> filas = accesos.buscarAcceso(usuarioId, codigo);
                if (filas.isEmpty()) {
                    responderError(response, 403, "El usuario no tiene acceso a este negocio.");
                    return;
                }

                Set<GrantedAuthority> authorities = new LinkedHashSet<>();
                for (AccesoNegocioProjection fila : filas) {
                    if (fila.getRolCodigo() != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + fila.getRolCodigo()));
                    }
                    if (fila.getPermisoCodigo() != null) {
                        authorities.add(new SimpleGrantedAuthority(fila.getPermisoCodigo()));
                    }
                }
                var scopedAuthentication = new JwtAuthenticationToken(
                        jwtAuth.getToken(), authorities, jwtAuth.getName());
                SecurityContextHolder.getContext().setAuthentication(scopedAuthentication);
            }
            filterChain.doFilter(request, response);
        } finally {
            NegocioContext.clear();
        }
    }

    private void responderError(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"detail\":\"" + mensaje + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/v3/api-docs") || path.startsWith("/v3/api-docs/")
                || path.equals("/v3/api-docs.yaml");
    }}
