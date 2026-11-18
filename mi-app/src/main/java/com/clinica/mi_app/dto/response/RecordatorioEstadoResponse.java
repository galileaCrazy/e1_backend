package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;

/** Último estado del recordatorio de una cita: ENVIADO | FALLIDO | RESPONDIO. */
public record RecordatorioEstadoResponse(String estado, OffsetDateTime fecha, String respuesta) {}
