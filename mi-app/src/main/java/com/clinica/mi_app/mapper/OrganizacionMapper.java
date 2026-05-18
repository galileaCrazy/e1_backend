package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.OrganizacionResponse;
import com.clinica.mi_app.model.Organizacion;

public class OrganizacionMapper {

    public static OrganizacionResponse toResponse(Organizacion o) {
        OrganizacionResponse r = new OrganizacionResponse();
        r.setId(o.getId());
        r.setNombre(o.getNombre());
        r.setPlan(o.getPlan());
        r.setTrialHasta(o.getTrialHasta());
        r.setActivo(o.getActivo());
        r.setCreatedAt(o.getCreatedAt());
        return r;
    }
}
