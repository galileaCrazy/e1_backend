package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.ConsultorioResponse;
import com.clinica.mi_app.model.Consultorio;

public class ConsultorioMapper {

    public static ConsultorioResponse toResponse(Consultorio c) {
        ConsultorioResponse r = new ConsultorioResponse();
        r.setId(c.getId());
        r.setOrganizacionId(c.getOrganizacion().getId());
        r.setNombre(c.getNombre());
        r.setDireccion(c.getDireccion());
        r.setTelefono(c.getTelefono());
        r.setActivo(c.getActivo());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
