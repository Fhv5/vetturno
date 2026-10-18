package cl.huellitas.vetturno.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas", uniqueConstraints = @UniqueConstraint(name = "uk_cita_veterinario_fecha", columnNames = {"veterinario_id", "fecha_hora"}))
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    protected Cita() {}

    public Cita(LocalDateTime fechaHora, String motivo, Mascota mascota, Veterinario veterinario) {
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.mascota = mascota;
        this.veterinario = veterinario;
    }

    public Long getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getMotivo() { return motivo; }
    public Mascota getMascota() { return mascota; }
    public Veterinario getVeterinario() { return veterinario; }
}
