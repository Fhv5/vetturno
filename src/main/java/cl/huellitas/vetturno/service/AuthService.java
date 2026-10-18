package cl.huellitas.vetturno.service;

import cl.huellitas.vetturno.dto.*;
import cl.huellitas.vetturno.exception.BusinessException;
import cl.huellitas.vetturno.model.*;
import cl.huellitas.vetturno.repository.UsuarioRepository;
import cl.huellitas.vetturno.security.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService details;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, AuthenticationManager authenticationManager,
                       UsuarioDetailsService details, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.details = details;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmail(email)) { throw new BusinessException("El email ya está registrado"); }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("La contraseña debe ocupar como máximo 72 bytes UTF-8");
        }
        usuarios.saveAndFlush(new Usuario(email, encoder.encode(request.password()), Rol.USER));
        return new AuthResponse(jwt.generar(details.loadUserByUsername(email)));
    }

    public AuthResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        var auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
            request.email().trim().toLowerCase(Locale.ROOT), request.password()));
        return new AuthResponse(jwt.generar((UserDetails) auth.getPrincipal()));
    }
}
