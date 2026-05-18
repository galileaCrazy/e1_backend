package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.MedicoResponse;
import com.clinica.mi_app.model.Medico;

public class MedicoMapper {

    public static MedicoResponse toResponse(Medico m) {
        MedicoResponse r = new MedicoResponse();
        r.setId(m.getId());
        r.setOrganizacionId(m.getOrganizacion().getId());
        r.setConsultorioId(m.getConsultorio().getId());
        r.setNombre(m.getNombre());
        r.setEspecialidad(m.getEspecialidad());
        r.setCedula(m.getCedula());
        r.setTelefono(m.getTelefono());
        r.setTarifaBase(m.getTarifaBase());
        r.setActivo(m.getActivo());
        return r;
    }
}
