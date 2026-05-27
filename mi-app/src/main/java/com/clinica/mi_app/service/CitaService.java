package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.CitaRequest;
import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.dto.response.DisponibilidadSlot;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.mapper.CitaMapper;
import com.clinica.mi_app.model.*;
import com.clinica.mi_app.repository.*;
import com.clinica.mi_app.security.AuthenticatedUser;
import com.clinica.mi_app.security.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaRepository repo;
    private final OrganizacionRepository orgRepo;
    private final PacienteRepository pacienteRepo;
    private final MedicoRepository medicoRepo;
    private final ConsultorioRepository consultorioRepo;
    private final HorarioMedicoRepository horarioRepo;

    public CitaService(CitaRepository repo, OrganizacionRepository orgRepo,
                       PacienteRepository pacienteRepo, MedicoRepository medicoRepo,
                       ConsultorioRepository consultorioRepo, HorarioMedicoRepository horarioRepo) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.pacienteRepo = pacienteRepo;
        this.medicoRepo = medicoRepo;
        this.consultorioRepo = consultorioRepo;
        this.horarioRepo = horarioRepo;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<CitaResponse> listarPorOrganizacion(UUID organizacionId) {
        return repo.findByOrganizacionId(organizacionId).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorMedico(UUID medicoId) {
        return repo.findByMedicoId(medicoId).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarMisCitas() {
        String email = AuthenticatedUser.getEmail();
        UUID orgId = AuthenticatedUser.getOrganizacionId();
        Paciente paciente = pacienteRepo.findByOrganizacionIdAndEmail(orgId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", email));
        return repo.findByPacienteId(paciente.getId()).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorPaciente(UUID pacienteId) {
        if (Roles.PACIENTE.equals(AuthenticatedUser.getRol())) {
            Paciente paciente = pacienteRepo.findById(pacienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cita", pacienteId.toString()));
            if (!AuthenticatedUser.getEmail().equals(paciente.getEmail())) {
                throw new ResourceNotFoundException("Cita", pacienteId.toString());
            }
        }
        return repo.findByPacienteId(pacienteId).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorEstado(UUID organizacionId, String estado) {
        return repo.findByOrganizacionIdAndEstado(organizacionId, estado).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorPacienteYEstado(UUID pacienteId, String estado) {
        if (Roles.PACIENTE.equals(AuthenticatedUser.getRol())) {
            Paciente paciente = pacienteRepo.findById(pacienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cita", pacienteId.toString()));
            if (!AuthenticatedUser.getEmail().equals(paciente.getEmail())) {
                throw new ResourceNotFoundException("Cita", pacienteId.toString());
            }
        }
        return repo.findByPacienteIdAndEstado(pacienteId, estado).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorMedicoEnRango(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin) {
        return repo.findByMedicoIdAndFechaHoraBetween(medicoId, inicio, fin).stream()
                .map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public CitaResponse buscarPorId(UUID id) {
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));
        if (Roles.PACIENTE.equals(AuthenticatedUser.getRol())) {
            if (!AuthenticatedUser.getEmail().equals(cita.getPaciente().getEmail())) {
                throw new ResourceNotFoundException("Cita", id.toString());
            }
        }
        return CitaMapper.toResponse(cita);
    }

    // ── Disponibilidad (método centralizado) ──────────────────────────────────

    /**
     * Calcula los slots disponibles para un médico en una fecha.
     * Pasa excludeId para ignorar una cita existente al actualizar.
     * Convención diaSemana: 0=Dom, 1=Lun, ..., 6=Sab (igual que JS Date.getDay()).
     */
    @Transactional(readOnly = true)
    public List<DisponibilidadSlot> calcularDisponibilidad(UUID medicoId, LocalDate fecha, UUID excludeId) {
        // Java DayOfWeek: MON=1..SUN=7  →  JS convention: SUN=0, MON=1..SAT=6
        int jsDay = fecha.getDayOfWeek().getValue() % 7;
        List<HorarioMedico> horarios = horarioRepo.findByMedicoIdAndDiaSemana(medicoId, (short) jsDay);
        if (horarios.isEmpty()) return List.of();

        // Generar todos los slots del día
        List<DisponibilidadSlot> todos = new ArrayList<>();
        for (HorarioMedico h : horarios) {
            LocalTime current = h.getHoraInicio();
            while (!current.plusMinutes(h.getDuracionConsulta()).isAfter(h.getHoraFin())) {
                LocalTime fin = current.plusMinutes(h.getDuracionConsulta());
                todos.add(new DisponibilidadSlot(
                        current.format(HM),
                        fin.format(HM),
                        h.getDuracionConsulta().intValue()
                ));
                current = fin;
            }
        }

        // Obtener citas activas ese día (excluir CANCELADAS y, al actualizar, la cita actual)
        OffsetDateTime startOfDay = fecha.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay   = fecha.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        Set<String> ocupados = repo.findByMedicoIdAndFechaHoraBetween(medicoId, startOfDay, endOfDay)
                .stream()
                .filter(c -> !"CANCELADA".equals(c.getEstado()))
                .filter(c -> excludeId == null || !excludeId.equals(c.getId()))
                .map(c -> c.getFechaHora().withOffsetSameInstant(ZoneOffset.UTC).toLocalTime().format(HM))
                .collect(Collectors.toSet());

        return todos.stream().filter(s -> !ocupados.contains(s.getHora())).collect(Collectors.toList());
    }

    // ── Validaciones internas ─────────────────────────────────────────────────

    private void validarDisponibilidad(UUID medicoId, OffsetDateTime fechaHora, UUID excludeId) {
        if (fechaHora.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new IllegalArgumentException("No se puede agendar una cita en fecha y hora pasada");
        }
        LocalDate fecha = fechaHora.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
        String horaSlot = fechaHora.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime().format(HM);
        List<DisponibilidadSlot> disponibles = calcularDisponibilidad(medicoId, fecha, excludeId);
        boolean libre = disponibles.stream().anyMatch(s -> s.getHora().equals(horaSlot));
        if (!libre) {
            throw new IllegalArgumentException("El horario seleccionado no está disponible para este médico");
        }
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    @Transactional
    public CitaResponse crear(CitaRequest req) {
        Organizacion org = orgRepo.findById(req.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));
        Paciente paciente = pacienteRepo.findById(req.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", req.getPacienteId().toString()));
        Medico medico = medicoRepo.findById(req.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medico", req.getMedicoId().toString()));
        Consultorio consultorio = consultorioRepo.findById(req.getConsultorioId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultorio", req.getConsultorioId().toString()));

        // Validar disponibilidad dentro de la transacción para evitar condiciones de carrera
        validarDisponibilidad(req.getMedicoId(), req.getFechaHora(), null);

        Cita c = new Cita();
        c.setOrganizacion(org);
        c.setPaciente(paciente);
        c.setMedico(medico);
        c.setConsultorio(consultorio);
        c.setFechaHora(req.getFechaHora());
        c.setDuracionMin(req.getDuracionMin());
        c.setMotivo(req.getMotivo());
        return CitaMapper.toResponse(repo.save(c));
    }

    @Transactional
    public CitaResponse actualizar(UUID id, CitaRequest req) {
        Cita c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));

        // Excluir la cita actual del check de ocupación para permitir re-agendar al mismo horario
        validarDisponibilidad(c.getMedico().getId(), req.getFechaHora(), id);

        c.setFechaHora(req.getFechaHora());
        c.setDuracionMin(req.getDuracionMin());
        c.setMotivo(req.getMotivo());
        return CitaMapper.toResponse(repo.save(c));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    @Transactional
    public CitaResponse cambiarEstado(UUID id, String nuevoEstado) {
        List<String> validos = List.of("SIN_CONFIRMAR", "CONFIRMADA", "CANCELADA", "REAGENDADA", "NO_ASISTIO");
        if (!validos.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Estado no válido: " + nuevoEstado);
        }
        Cita c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));
        c.setEstado(nuevoEstado);
        return CitaMapper.toResponse(repo.save(c));
    }
}
