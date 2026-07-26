package dev.nexusone.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    // The gateway is now the browser-facing edge (the SPA calls :8080, not
    // ticket-service's :8082 directly), so CORS is enforced here. Routes are
    // plain org.springframework.web.servlet.function.RouterFunction beans, not
    // @Controller-mapped handlers, so WebMvcConfigurer#addCorsMappings (which
    // only wires into RequestMappingHandlerMapping) would silently not apply.
    // A CorsFilter runs as a servlet Filter ahead of dispatch, so it works
    // uniformly for functional and annotated endpoints alike.
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
