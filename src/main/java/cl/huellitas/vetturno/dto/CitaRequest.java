package cl.huellitas.vetturno.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CitaRequest(
    @NotNull(message = "La fecha es obligatoria") @Future(message = "La fecha debe ser futura") LocalDateTime fechaHora,
    @NotBlank(message = "El motivo es obligatorio") @Size(max = 500) String motivo,
    @NotNull @Positive Long mascotaId,
    @NotNull @Positive Long veterinarioId
) {}
