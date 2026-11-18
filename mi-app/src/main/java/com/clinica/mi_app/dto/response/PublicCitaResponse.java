package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;

/**
 * Datos mínimos de la cita para la página pública /c/{token}: sin teléfono ni datos clínicos.
 * {@code fechaHora} sigue la convención de reloj de la clínica en UTC. {@code resultado} solo
 * viene después de responder (CONFIRMADA | CANCELADA).
 */
public record PublicCitaResponse(String pacienteNombre, String clinica, String medico, String especialidad,
                                 String consultorio, String direccion, OffsetDateTime fechaHora,
                                 String estado, String resultado) {}
