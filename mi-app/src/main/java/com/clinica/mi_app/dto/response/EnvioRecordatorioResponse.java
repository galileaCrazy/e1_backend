package com.clinica.mi_app.dto.response;

import com.clinica.mi_app.model.Notificacion;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnvioRecordatorioResponse(UUID notificacionId, UUID citaId, String tipo, String estado,
                                        OffsetDateTime enviadaEn, String proveedor) {

    public static EnvioRecordatorioResponse of(Notificacion n, String proveedor) {
        return new EnvioRecordatorioResponse(n.getId(), n.getCita().getId(), n.getTipo(), n.getEstado(),
                n.getEnviadaEn(), proveedor);
    }
}
