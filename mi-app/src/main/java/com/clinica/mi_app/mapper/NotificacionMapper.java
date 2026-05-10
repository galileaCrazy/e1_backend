package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.NotificacionResponse;
import com.clinica.mi_app.model.Notificacion;

public class NotificacionMapper {

    public static NotificacionResponse toResponse(Notificacion n) {
        NotificacionResponse r = new NotificacionResponse();
        r.setId(n.getId());
        r.setOrganizacionId(n.getOrganizacion().getId());
        r.setCitaId(n.getCita().getId());
        r.setAdjuntoId(n.getAdjunto() != null ? n.getAdjunto().getId() : null);
        r.setCanal(n.getCanal());
        r.setTipo(n.getTipo());
        r.setEstado(n.getEstado());
        r.setEnviadaEn(n.getEnviadaEn());
        r.setRespuesta(n.getRespuesta());
        return r;
    }
}
