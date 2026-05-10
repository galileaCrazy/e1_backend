package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.model.Cita;

public class CitaMapper {

    public static CitaResponse toResponse(Cita c) {
        CitaResponse r = new CitaResponse();
        r.setId(c.getId());
        r.setOrganizacionId(c.getOrganizacion().getId());
        r.setPacienteId(c.getPaciente().getId());
        r.setMedicoId(c.getMedico().getId());
        r.setConsultorioId(c.getConsultorio().getId());
        r.setFechaHora(c.getFechaHora());
        r.setDuracionMin(c.getDuracionMin());
        r.setEstado(c.getEstado());
        r.setMotivo(c.getMotivo());
        r.setCanceladaEn(c.getCanceladaEn());
        r.setCanceladaPor(c.getCanceladaPor());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
