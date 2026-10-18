package cl.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record PropietarioRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "El teléfono es obligatorio") @Size(max = 30) String telefono,
    @Email(message = "Email inválido") @Size(max = 254) String email
) {}
