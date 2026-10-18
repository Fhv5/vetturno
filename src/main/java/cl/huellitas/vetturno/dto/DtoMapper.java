package cl.huellitas.vetturno.dto;

import cl.huellitas.vetturno.model.*;

public final class DtoMapper {
    private DtoMapper() {}

    public static PropietarioDTO toDto(Propietario p) {
        return new PropietarioDTO(p.getId(), p.getNombre(), p.getTelefono(), p.getEmail());
    }

    public static MascotaDTO toDto(Mascota m) {
        return new MascotaDTO(m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(),
            m.getPropietario().getId(), m.getPropietario().getNombre());
    }

    public static VeterinarioDTO toDto(Veterinario v) {
        return new VeterinarioDTO(v.getId(), v.getNombre(), v.getEspecialidad());
    }

    public static CitaDTO toDto(Cita c) {
        return new CitaDTO(c.getId(), c.getFechaHora(), c.getMotivo(), c.getMascota().getNombre(),
            c.getMascota().getPropietario().getNombre(), c.getVeterinario().getNombre(),
            c.getMascota().getId(), c.getMascota().getPropietario().getId(), c.getVeterinario().getId());
    }
}
