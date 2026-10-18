package cl.huellitas.vetturno.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mascotas")
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String especie;

    @Column(length = 100)
    private String raza;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Propietario propietario;

    protected Mascota() {}

    public Mascota(String nombre, String especie, String raza, Propietario propietario) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.propietario = propietario;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecie() { return especie; }
    public String getRaza() { return raza; }
    public Propietario getPropietario() { return propietario; }
}
