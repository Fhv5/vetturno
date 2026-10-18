package cl.huellitas.vetturno.security;

import cl.huellitas.vetturno.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorWriter {
    private final ObjectMapper mapper;

    public SecurityErrorWriter(ObjectMapper mapper) { this.mapper = mapper; }

    public void write(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (status == 401) { response.setHeader("WWW-Authenticate", "Bearer"); }
        mapper.writeValue(response.getOutputStream(), ApiError.of(status, mensaje));
    }
}
