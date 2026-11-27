package ec.rmdetalles.configuracion.config;

import ec.rmdetalles.configuracion.repository.AccesoNegocioRepository;
import ec.rmdetalles.configuracion.repository.FrontendNegocioRepository;
import ec.rmdetalles.configuracion.repository.NegocioRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    NegocioAuthorizationFilter negocioAuthorizationFilter(
            AccesoNegocioRepository accesos,
            NegocioRepository negocios,
            FrontendNegocioRepository frontends
    ) {
        return new NegocioAuthorizationFilter(accesos, negocios, frontends);
    }

    @Bean
    FilterRegistrationBean<NegocioAuthorizationFilter> disableContainerFilterRegistration(
            NegocioAuthorizationFilter filter
    ) {
        FilterRegistrationBean<NegocioAuthorizationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            NegocioAuthorizationFilter negocioAuthorizationFilter
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll().requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/configuracion/**")
                        .hasAuthority("CONFIGURACION_GESTIONAR")
                        .requestMatchers("/api/v1/configuracion/**")
                        .hasAuthority("CONFIGURACION_GESTIONAR")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(negocioAuthorizationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(FrontendNegocioRepository frontends) {
        return request -> {
            String origin = request.getHeader("Origin");
            if (origin == null || origin.isBlank()
                    || !frontends.existsByOrigenAndActivoTrue(origin)) {
                return null;
            }
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of(origin));
            configuration.setAllowedMethods(
                    List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(
                    List.of("Authorization", "Content-Type", "X-Negocio-Codigo"));
            configuration.setExposedHeaders(List.of("Location"));
            configuration.setAllowCredentials(true);
            return configuration;
        };
    }
}
