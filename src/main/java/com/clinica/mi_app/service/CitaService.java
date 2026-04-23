package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.CitaRequest;
import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.*;
import com.clinica.mi_app.repository.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CitaService {

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
        return repo.findByOrganizacionId(organizacionId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorMedico(UUID medicoId) {
        return repo.findByMedicoId(medicoId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorPaciente(UUID pacienteId) {
        return repo.findByPacienteId(pacienteId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorEstado(UUID organizacionId, String estado) {
        return repo.findByOrganizacionIdAndEstado(organizacionId, estado).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CitaResponse> listarPorMedicoEnRango(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin) {
        return repo.findByMedicoIdAndFechaHoraBetween(medicoId, inicio, fin).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CitaResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));
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
        c.setMotivo(req.getMotivo());
        return toResponse(repo.save(c));
    }

    public CitaResponse actualizar(UUID id, CitaRequest req) {
        Cita c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));
        c.setFechaHora(req.getFechaHora());
        c.setDuracionMin(req.getDuracionMin());
        c.setMotivo(req.getMotivo());
        return toResponse(repo.save(c));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private CitaResponse toResponse(Cita c) {
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
