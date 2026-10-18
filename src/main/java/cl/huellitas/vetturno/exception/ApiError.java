package cl.huellitas.vetturno.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(int status, String mensaje, Map<String, String> errores, Instant timestamp) {
    public static ApiError of(int status, String mensaje) {
        return new ApiError(status, mensaje, Map.of(), Instant.now());
    }
}
