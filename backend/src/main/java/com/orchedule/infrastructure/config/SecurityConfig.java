package com.orchedule.infrastructure.config;

import com.orchedule.identity.api.UserAuthQuery;
import com.orchedule.identity.domain.TokenService;
import com.orchedule.infrastructure.security.JwtAuthenticationFilter;
import com.orchedule.infrastructure.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * Cadena de seguridad de la API REST.
     *
     * <p>CSRF está desactivado de forma deliberada: la API es stateless y solo acepta
     * credenciales en la cabecera {@code Authorization: Bearer}, que el navegador no
     * adjunta automáticamente. No se usan sesiones, cookies de autenticación,
     * formLogin ni HTTP Basic. Si en el futuro el token se transporta en una cookie,
     * esta decisión debe revisarse y CSRF volver a activarse.</p>
     *
     * <p>El filtro JWT se crea aquí y no es un bean: así no se registra también en el contenedor
     * servlet ni lo envuelve ningún proxy (ver {@link JwtAuthenticationFilter}).</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenService tokenService,
            UserAuthQuery userAuthQuery
    ) {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenService, userAuthQuery);

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR S4502: API stateless con Bearer en cabecera, sin cookies
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                        .accessDeniedHandler(
                                new AccessDeniedHandlerImpl()
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        .requestMatchers("/actuator/health", "/actuator/health/**")
                        .permitAll()

                        .requestMatchers("/actuator/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/register"
                        )
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
