package cl.huellitas.vetturno;

import cl.huellitas.vetturno.model.Rol;
import cl.huellitas.vetturno.repository.UsuarioRepository;
import cl.huellitas.vetturno.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VetTurnoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired Clock clock;
    @Value("${app.jwt.secret}") String secret;

    private static final String PASSWORD = "Test-password-2026";

    @BeforeEach
    void clean() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        if (!"vetturno_test".equals(database)) { throw new IllegalStateException("Se requiere vetturno_test"); }
        for (String table : List.of("citas", "mascotas", "propietarios", "veterinarios", "usuarios")) {
            jdbc.update("DELETE FROM " + table);
        }
    }

    @Test
    void applicationStartsWithRealMysqlAndForeignKeys() {
        assertThat(jdbc.queryForObject("SELECT VERSION()", String.class)).startsWith("8.");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema=DATABASE()", Integer.class)).isEqualTo(3);
    }

    @Test
    void registrationReturnsTokenAndHashesPasswordWithoutAcceptingAdminRole() throws Exception {
        var result = postJson("/api/auth/register", null, Map.of("email", "PAULA@example.test", "password", PASSWORD, "rol", "ADMIN"));
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String token = body(result).get("token").asText();
        assertThat(token.split("\\.")).hasSize(3);
        var user = usuarios.findByEmail("paula@example.test").orElseThrow();
        assertThat(user.getRol()).isEqualTo(Rol.USER);
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(encoder.matches(PASSWORD, user.getPassword())).isTrue();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("password", PASSWORD);
        mvc.perform(get("/api/citas").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        var r = postJson("/api/auth/register", null, Map.of("email", "invalid", "password", "123"));
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(r).get("errores").has("email")).isTrue();
        assertThat(body(r).get("errores").has("password")).isTrue();
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void duplicateEmailIsCaseInsensitive() throws Exception {
        register("paula@example.test");
        var r = postJson("/api/auth/register", null, Map.of("email", "PAULA@example.test", "password", PASSWORD));
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(r).get("mensaje").asText()).contains("registrado");
        assertThat(usuarios.count()).isEqualTo(1);
    }

    @Test
    void loginReturnsUsableTokenAndNoSession() throws Exception {
        register("paula@example.test");
        var result = postJson("/api/auth/login", null, Map.of("email", "paula@example.test", "password", PASSWORD));
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String token = body(result).get("token").asText();
        var claims = SignedJWT.parse(token).getJWTClaimsSet();
        assertThat(claims.getExpirationTime()).isAfter(new Date());
        assertThat(claims.getIssuer()).isEqualTo("vetturno");
        assertThat(claims.getClaims()).doesNotContainKeys("password");
        mvc.perform(get("/api/citas").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(cookie().doesNotExist("JSESSIONID"));
        mvc.perform(get("/api/citas")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordAndUnknownUserReceiveSameUnauthorizedError() throws Exception {
        register("paula@example.test");
        var wrong = postJson("/api/auth/login", null, Map.of("email", "paula@example.test", "password", "incorrect"));
        var unknown = postJson("/api/auth/login", null, Map.of("email", "missing@example.test", "password", "incorrect"));
        assertThat(wrong.getResponse().getStatus()).isEqualTo(401);
        assertThat(unknown.getResponse().getStatus()).isEqualTo(401);
        assertThat(body(wrong).get("mensaje")).isEqualTo(body(unknown).get("mensaje"));
    }

    @Test
    void missingTokenReturnsConsistentErrorForEveryBusinessResource() throws Exception {
        for (String route : List.of("citas", "mascotas", "propietarios", "veterinarios")) {
            mvc.perform(get("/api/" + route)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.mensaje").isString())
                .andExpect(jsonPath("$.errores").isMap()).andExpect(jsonPath("$.timestamp").isString());
        }
    }

    @Test
    void userCannotCreateVeterinario() throws Exception {
        var r = postJson("/api/veterinarios", register("paula@example.test"), Map.of("nombre", "Andrés", "especialidad", "General"));
        assertThat(r.getResponse().getStatus()).isEqualTo(403);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM veterinarios", Integer.class)).isZero();
    }

    @Test
    void controlledPromotionRequiresNewLoginAndAdminCanCreateVeterinario() throws Exception {
        String oldToken = register("marta@example.test");
        jdbc.update("UPDATE usuarios SET rol='ADMIN' WHERE email=?", "marta@example.test");
        assertThat(postJson("/api/veterinarios", oldToken, Map.of("nombre", "Andrés", "especialidad", "General"))
            .getResponse().getStatus()).isEqualTo(403);
        String admin = login("marta@example.test");
        var r = postJson("/api/veterinarios", admin, Map.of("nombre", "Andrés", "especialidad", "General"));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        mvc.perform(get("/api/veterinarios").header("Authorization", "Bearer " + oldToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Andrés"));
    }

    @Test
    void ownerCreationAndListReturnFlatDto() throws Exception {
        String token = register("paula@example.test");
        var r = postJson("/api/propietarios", token, Map.of("nombre", "Elena", "telefono", "+56912345678", "email", "elena@example.test"));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        assertThat(body(r).has("mascotas")).isFalse();
        mvc.perform(get("/api/propietarios").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Elena"));
    }

    @Test
    void petCreationResolvesOwnerAndListsFlatDto() throws Exception {
        var f = fixture();
        mvc.perform(get("/api/mascotas").header("Authorization", "Bearer " + f.token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].propietarioId").value(f.owner()))
            .andExpect(jsonPath("$[0].propietarioNombre").value("Elena"))
            .andExpect(jsonPath("$[0].citas").doesNotExist()).andExpect(jsonPath("$[0].propietario").doesNotExist());
    }

    @Test
    void petWithMissingOwnerIsRejectedWithoutInsert() throws Exception {
        var r = postJson("/api/mascotas", register("paula@example.test"), Map.of("nombre", "Luna", "especie", "Canina", "propietarioId", Long.MAX_VALUE));
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(r).get("mensaje").asText()).contains("propietario");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mascotas", Integer.class)).isZero();
    }

    @Test
    void futureAppointmentIsCreatedAndContainsReadableRelations() throws Exception {
        var f = fixture();
        var r = postJson("/api/citas", f.token(), appointment(f, future()));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        assertThat(body(r).get("mascota").asText()).isEqualTo("Luna");
        assertThat(body(r).get("propietario").asText()).isEqualTo("Elena");
        assertThat(body(r).get("veterinario").asText()).isEqualTo("Andrés");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM citas", Integer.class)).isEqualTo(1);
    }

    @Test
    void pastAppointmentIsRejected() throws Exception {
        var f = fixture();
        var r = postJson("/api/citas", f.token(), appointment(f, LocalDateTime.now(clock).minusDays(1).toString()));
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(r).get("errores").has("fechaHora")).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM citas", Integer.class)).isZero();
    }

    @Test
    void duplicateSlotIsRejectedAndOnlyOneAppointmentRemains() throws Exception {
        var f = fixture();
        var appointment = appointment(f, future());
        assertThat(postJson("/api/citas", f.token(), appointment).getResponse().getStatus()).isEqualTo(201);
        var r = postJson("/api/citas", f.token(), appointment);
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(r).get("mensaje").asText()).contains("horario");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM citas", Integer.class)).isEqualTo(1);
    }

    @Test
    void appointmentsAreOrderedAndFilteredByVeterinario() throws Exception {
        var f = fixture();
        String admin = admin();
        long otherVet = create("/api/veterinarios", admin, Map.of("nombre", "Carolina", "especialidad", "Felinos"));
        String first = LocalDateTime.now(clock).plusDays(2).withNano(0).toString();
        String later = LocalDateTime.now(clock).plusDays(3).withNano(0).toString();
        postJson("/api/citas", f.token(), appointment(f, later));
        postJson("/api/citas", f.token(), Map.of("fechaHora", first, "motivo", "Control", "mascotaId", f.pet(), "veterinarioId", otherVet));
        mvc.perform(get("/api/citas").header("Authorization", "Bearer " + f.token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].veterinarioId").value(otherVet));
        mvc.perform(get("/api/citas/veterinario/" + f.vet()).header("Authorization", "Bearer " + f.token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].veterinarioId").value(f.vet()));
    }

    @Test
    void differentVeterinariosCanShareTheSameTime() throws Exception {
        var f = fixture();
        long second = create("/api/veterinarios", admin(), Map.of("nombre", "Carolina", "especialidad", "General"));
        String date = future();
        assertThat(postJson("/api/citas", f.token(), appointment(f, date)).getResponse().getStatus()).isEqualTo(201);
        assertThat(postJson("/api/citas", f.token(), Map.of("fechaHora", date, "motivo", "Control", "mascotaId", f.pet(), "veterinarioId", second)).getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void missingAppointmentRelationsReturnControlledErrors() throws Exception {
        var f = fixture();
        for (String field : List.of("mascotaId", "veterinarioId")) {
            var request = new HashMap<String, Object>(appointment(f, future()));
            request.put(field, Long.MAX_VALUE);
            assertThat(postJson("/api/citas", f.token(), request).getResponse().getStatus()).isEqualTo(400);
        }
        mvc.perform(get("/api/citas/veterinario/" + Long.MAX_VALUE).header("Authorization", "Bearer " + f.token()))
            .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM citas", Integer.class)).isZero();
    }

    @Test
    void everyInputRejectsBlankOrMissingRequiredFields() throws Exception {
        String token = register("paula@example.test");
        String admin = admin();
        for (String route : List.of("propietarios", "mascotas", "citas")) {
            assertThat(postJson("/api/" + route, token, Map.of()).getResponse().getStatus()).isEqualTo(400);
        }
        assertThat(postJson("/api/veterinarios", admin, Map.of()).getResponse().getStatus()).isEqualTo(400);
        var blank = postJson("/api/propietarios", token, Map.of("nombre", " ", "telefono", " ", "email", "bad"));
        assertThat(blank.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(blank).get("errores").size()).isEqualTo(3);
    }

    @Test
    void malformedJsonDatesAndIdsReturnSafeErrors() throws Exception {
        var f = fixture();
        mvc.perform(post("/api/citas").header("Authorization", "Bearer " + f.token())
            .contentType(MediaType.APPLICATION_JSON).content("{broken"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        assertThat(postJson("/api/citas", f.token(), appointment(f, "not-a-date")).getResponse().getStatus()).isEqualTo(400);
        var r = mvc.perform(get("/api/citas/veterinario/no-id").header("Authorization", "Bearer " + f.token()))
            .andExpect(status().isBadRequest()).andReturn();
        assertThat(r.getResponse().getContentAsString()).doesNotContain("Exception", "java.", "stackTrace", "cl.huellitas");
    }

    @Test
    void expiredTamperedWrongIssuerAndMalformedTokensAreRejected() throws Exception {
        register("paula@example.test");
        String expired = new JwtService(secret, Duration.ofSeconds(1), "vetturno", Clock.fixed(Instant.now().minusSeconds(100), clock.getZone()))
            .generar(User.withUsername("paula@example.test").password("unused").roles("USER").build());
        String valid = login("paula@example.test");
        String tampered = valid.substring(0, valid.lastIndexOf('.') + 1) + "invalid-signature";
        var claims = new JWTClaimsSet.Builder().subject("paula@example.test").issuer("other")
            .expirationTime(Date.from(Instant.now().plusSeconds(60))).claim("roles", List.of("ROLE_ADMIN")).build();
        var wrongIssuer = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        wrongIssuer.sign(new MACSigner(secret));
        for (String token : List.of(expired, tampered, wrongIssuer.serialize(), "broken")) {
            mvc.perform(get("/api/citas").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        }
    }

    @Test
    void passwordUtf8LimitIsHandledWithoutServerError() throws Exception {
        String multibyte = "á".repeat(40);
        var r = postJson("/api/auth/register", null, Map.of("email", "paula@example.test", "password", multibyte));
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void simultaneousReservationsStoreExactlyOneRow() throws Exception {
        var f = fixture();
        var request = appointment(f, future());
        var pool = Executors.newFixedThreadPool(4);
        var ready = new CountDownLatch(4);
        var start = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) { throw new IllegalStateException("Timeout"); }
                    return postJson("/api/citas", f.token(), request).getResponse().getStatus();
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (var future : futures) { statuses.add(future.get(30, TimeUnit.SECONDS)); }
            assertThat(statuses).containsExactlyInAnyOrder(201, 400, 400, 400);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM citas", Integer.class)).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }

    @Test
    void swaggerAndOpenApiArePublicAndDeclareBearerSecurity() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        var r = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var spec = body(r);
        assertThat(spec.at("/info/title").asText()).isEqualTo("VetTurno");
        assertThat(spec.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(spec.get("paths").size()).isEqualTo(7);
        assertThat(spec.at("/paths/~1api~1citas~1veterinario~1{id}/get").isMissingNode()).isFalse();
    }

    private MvcResult postJson(String route, String token, Object data) throws Exception {
        MockHttpServletRequestBuilder request = post(route).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(data));
        if (token != null) { request.header("Authorization", "Bearer " + token); }
        return mvc.perform(request).andReturn();
    }

    private JsonNode body(MvcResult result) throws Exception { return json.readTree(result.getResponse().getContentAsByteArray()); }

    private String register(String email) throws Exception {
        var r = postJson("/api/auth/register", null, Map.of("email", email, "password", PASSWORD));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        return body(r).get("token").asText();
    }

    private String login(String email) throws Exception {
        var r = postJson("/api/auth/login", null, Map.of("email", email, "password", PASSWORD));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        return body(r).get("token").asText();
    }

    private String admin() throws Exception {
        if (!usuarios.existsByEmail("marta@example.test")) { register("marta@example.test"); }
        jdbc.update("UPDATE usuarios SET rol='ADMIN' WHERE email=?", "marta@example.test");
        return login("marta@example.test");
    }

    private long create(String route, String token, Object request) throws Exception {
        var r = postJson(route, token, request);
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        return body(r).get("id").asLong();
    }

    private Fixture fixture() throws Exception {
        String token = register("paula@example.test");
        long owner = create("/api/propietarios", token, Map.of("nombre", "Elena", "telefono", "+56912345678"));
        long pet = create("/api/mascotas", token, Map.of("nombre", "Luna", "especie", "Canina", "raza", "Mestiza", "propietarioId", owner));
        long vet = create("/api/veterinarios", admin(), Map.of("nombre", "Andrés", "especialidad", "General"));
        return new Fixture(token, owner, pet, vet);
    }

    private String future() { return LocalDateTime.now(clock).plusDays(1).withNano(0).toString(); }

    private Map<String, Object> appointment(Fixture f, String date) {
        return Map.of("fechaHora", date, "motivo", "Control anual", "mascotaId", f.pet(), "veterinarioId", f.vet());
    }

    private record Fixture(String token, long owner, long pet, long vet) {}
}
