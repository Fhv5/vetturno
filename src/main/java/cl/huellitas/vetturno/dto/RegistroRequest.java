package cl.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
    @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") @Size(max = 254) String email,
    @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password
) {}
