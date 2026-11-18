package com.clinica.mi_app.service;

import com.clinica.mi_app.config.ClinicaReloj;
import com.clinica.mi_app.exception.ConflictException;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Notificacion;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.NotificacionRepository;
import com.clinica.mi_app.whatsapp.TelefonoMx;
import com.clinica.mi_app.whatsapp.WhatsAppMensajeria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Envío de recordatorios de cita por WhatsApp, compartido por el ReminderJob y el botón manual.
 * Cada intento queda en notificacion con estado ENVIADO o FALLIDO.
 */
@Service
public class RecordatorioService {

    public static final String MANUAL = "RECORDATORIO_MANUAL";
    static final Set<String> TIPOS_RECORDATORIO = Set.of("RECORDATORIO_48H", "RECORDATORIO_24H", MANUAL);
    static final Set<String> ESTADOS_NO_ENVIABLES = Set.of("CANCELADA", "NO_ASISTIO", "REAGENDADA");

    private final CitaRepository citaRepo;
    private final NotificacionRepository notificacionRepo;
    private final WhatsAppMensajeria mensajeria;
    private final ClinicaReloj reloj;

    public RecordatorioService(CitaRepository citaRepo, NotificacionRepository notificacionRepo,
                               WhatsAppMensajeria mensajeria, ClinicaReloj reloj) {
        this.citaRepo = citaRepo;
        this.notificacionRepo = notificacionRepo;
        this.mensajeria = mensajeria;
        this.reloj = reloj;
    }

    /**
     * Botón "Enviar recordatorio WhatsApp". La cita debe ser de la organización del usuario (y del médico,
     * si quien envía es MEDICO); si no, 404 para no revelar citas ajenas.
     *
     * @throws ResourceNotFoundException cita inexistente o de otra organización/médico
     * @throws IllegalArgumentException  estado no enviable, cita pasada o paciente sin teléfono válido
     * @throws ConflictException         ya se envió un recordatorio y no se pidió reenviar
     */
    @Transactional
    public Notificacion enviarManual(UUID organizacionId, UUID medicoId, UUID citaId, boolean reenviar) {
        Cita cita = citaRepo.findById(citaId)
                .filter(c -> c.getOrganizacion().getId().equals(organizacionId))
                .filter(c -> medicoId == null || c.getMedico().getId().equals(medicoId))
                .orElseThrow(() -> new ResourceNotFoundException("Cita", citaId.toString()));
        validar(cita);
        if (!reenviar && yaEnviado(cita, TIPOS_RECORDATORIO)) {
            throw new ConflictException("Ya se envió un recordatorio para esta cita. Usa reenviar=true para mandarlo de nuevo.");
        }
        return enviar(cita, MANUAL);
    }

    /** ReminderJob: mismas validaciones, pero en silencio (sin excepciones) y sin duplicar por tipo. */
    @Transactional
    public Optional<Notificacion> enviarAutomatico(UUID citaId, String tipo) {
        Cita cita = citaRepo.findById(citaId).orElse(null);
        if (cita == null || motivoNoEnviable(cita) != null || yaEnviado(cita, Set.of(tipo))) {
            return Optional.empty();
        }
        return Optional.of(enviar(cita, tipo));
    }

    void validar(Cita cita) {
        String motivo = motivoNoEnviable(cita);
        if (motivo != null) throw new IllegalArgumentException(motivo);
    }

    String motivoNoEnviable(Cita cita) {
        if (ESTADOS_NO_ENVIABLES.contains(cita.getEstado())) {
            return "No se envían recordatorios a citas en estado " + cita.getEstado();
        }
        if (cita.getFechaHora().isBefore(reloj.ahora())) {
            return "La cita ya pasó; no se envía recordatorio";
        }
        if (TelefonoMx.aDiezDigitos(cita.getPaciente().getTelefono()) == null) {
            return "El paciente no tiene un teléfono válido de 10 dígitos";
        }
        return null;
    }

    private boolean yaEnviado(Cita cita, Set<String> tipos) {
        return notificacionRepo.findByCitaId(cita.getId()).stream()
                .anyMatch(n -> tipos.contains(n.getTipo()) && "ENVIADO".equals(n.getEstado()));
    }

    private Notificacion enviar(Cita cita, String tipo) {
        Optional<String> id = mensajeria.enviarRecordatorio(cita);
        Notificacion n = new Notificacion();
        n.setCita(cita);
        n.setOrganizacion(cita.getOrganizacion());
        n.setCanal("WHATSAPP");
        n.setTipo(tipo);
        n.setEstado(id.isPresent() ? "ENVIADO" : "FALLIDO");
        n.setEnviadaEn(id.isPresent() ? OffsetDateTime.now() : null);
        return notificacionRepo.save(n);
    }
}
