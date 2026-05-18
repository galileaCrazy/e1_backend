package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.DiagnosticoResponse;
import com.clinica.mi_app.model.Diagnostico;

public class DiagnosticoMapper {

    public static DiagnosticoResponse toResponse(Diagnostico d) {
        DiagnosticoResponse r = new DiagnosticoResponse();
        r.setId(d.getId());
        r.setCitaId(d.getCita().getId());
        r.setCodigoCie10(d.getCodigoCie10());
        r.setDescripcion(d.getDescripcion());
        r.setTipo(d.getTipo());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }
}
