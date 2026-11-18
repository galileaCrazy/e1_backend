package com.clinica.mi_app.service;

import com.clinica.mi_app.client.AuditClient;
import com.clinica.mi_app.config.ClinicaReloj;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Notificacion;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.NotificacionRepository;
import com.clinica.mi_app.whatsapp.RecordatorioPlantilla;
import com.clinica.mi_app.whatsapp.WhatsAppMensajeria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Aplica la respuesta del paciente a una cita ya resuelta por WhatsAppWebhookProcessor:
 * "1" confirma, "2" cancela (y genera CANCELACION_PACIENTE para la campana); en ambos casos se le
 * contesta al paciente. Cualquier otro texto recibe el mensaje de ayuda.
 */
@Service
public class WhatsAppRespuestaService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppRespuestaService.class);

    static final Set<String> CONFIRMAR = Set.of("1", "confirmar", "confirmo");
    static final Set<String> CANCELAR = Set.of("2", "cancelar", "cancelo");

    private final CitaRepository citaRepo;
    private final NotificacionRepository notificacionRepo;
    private final WhatsAppMensajeria mensajeria;
    private final AuditClient auditClient;
    private final ClinicaReloj reloj;

    public WhatsAppRespuestaService(CitaRepository citaRepo, NotificacionRepository notificacionRepo,
                                    WhatsAppMensajeria mensajeria, AuditClient auditClient, ClinicaReloj reloj) {
        this.citaRepo = citaRepo;
        this.notificacionRepo = notificacionRepo;
        this.mensajeria = mensajeria;
        this.auditClient = auditClient;
        this.reloj = reloj;
    }

    public void procesarRespuesta(Cita cita, String telefono, String mensaje) {
        String texto = mensaje == null ? "" : mensaje.trim().toLowerCase();

        if (CONFIRMAR.contains(texto)) {
            cita.setEstado("CONFIRMADA");
            citaRepo.save(cita);
            mensajeria.enviarTexto(cita.getOrganizacion(), cita, telefono, RecordatorioPlantilla.confirmada(cita));
        } else if (CANCELAR.contains(texto)) {
            OffsetDateTime ahora = reloj.ahora();
            cita.setEstado("CANCELADA");
            cita.setCanceladaPor("PACIENTE");
            cita.setCanceladaEn(ahora);
            citaRepo.save(cita);

            Notificacion notif = new Notificacion();
            notif.setTipo("CANCELACION_PACIENTE");
            notif.setCanal("WHATSAPP");
            notif.setEstado("ENVIADO");
            notif.setRespuesta(mensaje);
            notif.setEnviadaEn(OffsetDateTime.now());
            notif.setCita(cita);
            notif.setOrganizacion(cita.getOrganizacion());
            notificacionRepo.save(notif);

            mensajeria.enviarTexto(cita.getOrganizacion(), cita, telefono, RecordatorioPlantilla.cancelada(cita));
        } else {
            mensajeria.enviarTexto(cita.getOrganizacion(), cita, telefono, RecordatorioPlantilla.ayuda());
        }

        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("telefono", telefono);
            metadata.put("mensaje", mensaje);
            metadata.put("canal", mensajeria.proveedor());
            auditClient.audit("appointment.updated", "CITA", cita.getId(), cita.getOrganizacion().getId(), null, metadata);
        } catch (Exception ex) {
            log.warn("audit appointment.updated: {}", ex.getMessage());
        }
    }
}
