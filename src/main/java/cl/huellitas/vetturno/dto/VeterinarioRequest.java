package cl.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record VeterinarioRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "La especialidad es obligatoria") @Size(max = 120) String especialidad
) {}
