package cl.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;

public record MascotaRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "La especie es obligatoria") @Size(max = 60) String especie,
    @Size(max = 100) String raza,
    @NotNull(message = "El propietario es obligatorio") @Positive Long propietarioId
) {}
