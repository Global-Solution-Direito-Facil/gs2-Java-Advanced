package br.com.fiap.direitofacil.auth;

import br.com.fiap.direitofacil.config.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Segurança da API (mesma base do PetPulse): stateless, JWT validado pelo resource server
 * e papel lido da claim "role" sem prefixo. Diferença: o token é assinado com HS256 e a chave
 * compartilhada com a .NET (texto UTF-8), em vez do par de chaves RSA.
 * As regras por papel ficam no filtro (antes da validação do corpo) e nos controllers com @PreAuthorize.
 */
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, RespostaErroSeguranca respostaErro) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registrar").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // papel conferido aqui, antes da validação do corpo (os controllers repetem com @PreAuthorize)
                        .requestMatchers("/api/v1/gestores/**", "/api/v1/painel/**").hasAuthority("GESTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/direitos", "/api/v1/laudos").hasAuthority("GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/direitos/**", "/api/v1/casos/*/orientacao").hasAuthority("GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/direitos/**").hasAuthority("GESTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/casos/*/status").hasAuthority("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/casos/me").hasAuthority("CIDADAO")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(respostaErro)
                        .accessDeniedHandler(respostaErro)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(respostaErro)
                        .accessDeniedHandler(respostaErro)
                )
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties jwt) {
        return NimbusJwtDecoder.withSecretKey(chave(jwt))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave(jwt)));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** A authority é o valor da claim "role" (CIDADAO ou GESTOR), sem o prefixo ROLE_. */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    /** Chave HS256 a partir do texto UTF-8 da variável JWT_SECRET (sem decodificar Base64). */
    private SecretKey chave(JwtProperties jwt) {
        return new SecretKeySpec(jwt.secretBytes(), "HmacSHA256");
    }
}
