package cl.huellitas.vetturno.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarios;
    private final SecurityErrorWriter errors;

    public JwtAuthFilter(JwtService jwtService, UsuarioDetailsService usuarios, SecurityErrorWriter errors) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
        this.errors = errors;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/register") || path.equals("/api/auth/login")
            || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) { throw new IllegalArgumentException(); }
                var jwt = jwtService.validar(header.substring(7));
                var user = usuarios.loadUserByUsername(jwt.getSubject());
                var roles = jwt.getClaimAsStringList("roles").stream().map(SimpleGrantedAuthority::new)
                    .filter(role -> user.getAuthorities().contains(role)
                        || role.getAuthority().equals("ROLE_USER")
                        && user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).toList();
                var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, roles);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | AuthenticationException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                errors.write(response, 401, "Token inválido o expirado");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
