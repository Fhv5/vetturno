package cl.huellitas.vetturno.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Clock clock;
    private final Duration ttl;
    private final String issuer;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.ttl}") Duration ttl,
                      @Value("${app.jwt.issuer}") String issuer, Clock clock) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32 || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalStateException("JWT requiere un secreto de al menos 32 bytes y una duración positiva");
        }
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(issuer)));
        this.clock = clock;
        this.ttl = ttl;
        this.issuer = issuer;
    }

    public String generar(UserDetails usuario) {
        var now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(usuario.getUsername())
            .issuedAt(now).expiresAt(now.plus(ttl))
            .claim("roles", usuario.getAuthorities().stream().map(a -> a.getAuthority()).toList()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt validar(String token) {
        var jwt = decoder.decode(token);
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (jwt.getSubject() == null || jwt.getExpiresAt() == null || roles == null || roles.isEmpty()
            || roles.stream().anyMatch(r -> !List.of("ROLE_USER", "ROLE_ADMIN").contains(r))) {
            throw new BadJwtException("Token inválido");
        }
        return jwt;
    }
}
