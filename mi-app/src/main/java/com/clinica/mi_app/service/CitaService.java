package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.CitaRequest;
import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.mapper.CitaMapper;
import com.clinica.mi_app.model.*;
import com.clinica.mi_app.repository.*;
import com.clinica.mi_app.security.AuthenticatedUser;
import com.clinica.mi_app.security.Roles;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("SIN_CONFIRMAR", "CONFIRMADA", "CANCELADA", "REAGENDADA", "NO_ASISTIO");

    private final CitaRepository repo;
    private final OrganizacionRepository orgRepo;
    private final PacienteRepository pacienteRepo;
    private final MedicoRepository medicoRepo;
    private final ConsultorioRepository consultorioRepo;

    public CitaService(CitaRepository repo, OrganizacionRepository orgRepo,
                       PacienteRepository pacienteRepo, MedicoRepository medicoRepo,
                       ConsultorioRepository consultorioRepo) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.pacienteRepo = pacienteRepo;
        this.medicoRepo = medicoRepo;
        this.consultorioRepo = consultorioRepo;
    }

    public List<CitaResponse> listarPorOrganizacion(UUID organizacionId) {
        return repo.findByOrganizacionId(organizacionId).stream().map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorMedico(UUID medicoId) {
        return repo.findByMedicoId(medicoId).stream().map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorPaciente(UUID pacienteId) {
        if (Roles.PACIENTE.equals(AuthenticatedUser.getRol())) {
            Paciente paciente = pacienteRepo.findById(pacienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cita", pacienteId.toString()));
            if (!AuthenticatedUser.getEmail().equals(paciente.getEmail())) {
                throw new ResourceNotFoundException("Cita", pacienteId.toString());
            }
        }
        return repo.findByPacienteId(pacienteId).stream().map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorEstado(UUID organizacionId, String estado) {
        return repo.findByOrganizacionIdAndEstado(organizacionId, estado).stream().map(CitaMapper::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorMedicoEnRango(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin) {
        return repo.findByMedicoIdAndFechaHoraBetween(medicoId, inicio, fin).stream().map(CitaMapper::toResponse).collect(Collectors.toList());
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

    public CitaResponse crear(CitaRequest req) {
        Organizacion org = orgRepo.findById(req.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));
        Paciente paciente = pacienteRepo.findById(req.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", req.getPacienteId().toString()));
        Medico medico = medicoRepo.findById(req.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medico", req.getMedicoId().toString()));
        Consultorio consultorio = consultorioRepo.findById(req.getConsultorioId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultorio", req.getConsultorioId().toString()));
        Cita c = new Cita();
        c.setOrganizacion(org);
        c.setPaciente(paciente);
        c.setMedico(medico);
        c.setConsultorio(consultorio);
        c.setFechaHora(req.getFechaHora());
        c.setDuracionMin(req.getDuracionMin());
        aplicarEstado(c, req.getEstado());
        c.setMotivo(req.getMotivo());
        return CitaMapper.toResponse(repo.save(c));
    }

    public CitaResponse actualizar(UUID id, CitaRequest req) {
        Cita c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));
        c.setFechaHora(req.getFechaHora());
        c.setDuracionMin(req.getDuracionMin());
        aplicarEstado(c, req.getEstado());
        c.setMotivo(req.getMotivo());
        return CitaMapper.toResponse(repo.save(c));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private void aplicarEstado(Cita cita, String estado) {
        if (estado == null || estado.isBlank()) {
            return;
        }
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("Estado de cita invalido: " + estado);
        }
        cita.setEstado(estado);
    }
}
