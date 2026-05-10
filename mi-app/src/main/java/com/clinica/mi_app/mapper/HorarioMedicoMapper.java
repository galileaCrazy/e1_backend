package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.HorarioMedicoResponse;
import com.clinica.mi_app.model.HorarioMedico;

public class HorarioMedicoMapper {

    public static HorarioMedicoResponse toResponse(HorarioMedico h) {
        HorarioMedicoResponse r = new HorarioMedicoResponse();
        r.setId(h.getId());
        r.setMedicoId(h.getMedico().getId());
        r.setDiaSemana(h.getDiaSemana());
        r.setHoraInicio(h.getHoraInicio());
        r.setHoraFin(h.getHoraFin());
        r.setDuracionConsulta(h.getDuracionConsulta());
        return r;
    }
}
