package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.PacienteResponse;
import com.clinica.mi_app.model.Paciente;

public class PacienteMapper {

    public static PacienteResponse toResponse(Paciente p) {
        PacienteResponse r = new PacienteResponse();
        r.setId(p.getId());
        r.setOrganizacionId(p.getOrganizacion().getId());
        r.setNombre(p.getNombre());
        r.setTelefono(p.getTelefono());
        r.setFechaNacimiento(p.getFechaNacimiento());
        r.setSexo(p.getSexo());
        r.setEmail(p.getEmail());
        r.setNotas(p.getNotas());
        r.setActivo(p.getActivo());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
