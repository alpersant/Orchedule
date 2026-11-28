package com.orchedule.infrastructure.security;

import com.orchedule.identity.api.UserAuthQuery;
import com.orchedule.identity.domain.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Autentica peticiones con un JWT de acceso en la cabecera {@code Authorization: Bearer}.
 *
 * <p><strong>No es un bean de Spring</strong> a propósito: lo instancia {@code SecurityConfig} y lo
 * añade a la cadena de seguridad. Si fuese un {@code @Component}, Spring Modulith (observabilidad)
 * lo envolvería en un proxy CGLIB y, como {@code GenericFilterBean.init} es {@code final}, el
 * contenedor fallaría al arrancar con un {@code NullPointerException} en {@code logger}. Además,
 * un filtro registrado como bean se añadiría una segunda vez a la cadena del contenedor servlet.</p>
 */
public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserAuthQuery userAuthQuery;

    public JwtAuthenticationFilter(TokenService tokenService, UserAuthQuery userAuthQuery) {
        this.tokenService = tokenService;
        this.userAuthQuery = userAuthQuery;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        if (!tokenService.isValid(token) || tokenService.isRefreshToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(tokenService.getSubject(token));

            userAuthQuery.findActiveById(userId).ifPresent(user -> {
                var authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        } catch (IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || request.getRequestURI().startsWith("/actuator/");
    }
}
