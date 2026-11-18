package com.clinica.mi_app.dto.response;

import com.clinica.mi_app.model.WhatsAppMensaje;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WhatsAppMensajeResponse(UUID id, UUID citaId, String telefono, String direccion, String texto,
                                      String estado, OffsetDateTime createdAt) {

    public static WhatsAppMensajeResponse of(WhatsAppMensaje m) {
        return new WhatsAppMensajeResponse(m.getId(), m.getCita() == null ? null : m.getCita().getId(),
                m.getTelefono(), m.getDireccion(), m.getTexto(), m.getEstado(), m.getCreatedAt());
    }
}
