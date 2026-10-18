CREATE TABLE propietarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    email VARCHAR(254)
);
CREATE TABLE mascotas (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    especie VARCHAR(60) NOT NULL,
    raza VARCHAR(100),
    propietario_id BIGINT NOT NULL,
    CONSTRAINT fk_mascota_propietario FOREIGN KEY (propietario_id) REFERENCES propietarios(id)
);
CREATE TABLE veterinarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    especialidad VARCHAR(120) NOT NULL
);
CREATE TABLE citas (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME(6) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    mascota_id BIGINT NOT NULL,
    veterinario_id BIGINT NOT NULL,
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascotas(id),
    CONSTRAINT fk_cita_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id),
    CONSTRAINT uk_cita_veterinario_fecha UNIQUE (veterinario_id, fecha_hora),
    INDEX idx_citas_fecha (fecha_hora)
);
CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(16) NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('USER', 'ADMIN'))
);
