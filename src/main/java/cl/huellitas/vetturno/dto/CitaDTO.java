package cl.huellitas.vetturno.dto;

import java.time.LocalDateTime;

public record CitaDTO(
    Long id, LocalDateTime fechaHora, String motivo, String mascota, String propietario, String veterinario, Long mascotaId, Long propietarioId, Long veterinarioId
) {}
