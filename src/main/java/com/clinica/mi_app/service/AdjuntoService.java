package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.AdjuntoRequest;
import com.clinica.mi_app.dto.response.AdjuntoResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.*;
import com.clinica.mi_app.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdjuntoService {

    private final AdjuntoRepository repo;
    private final OrganizacionRepository orgRepo;
    private final PacienteRepository pacienteRepo;
    private final CitaRepository citaRepo;
    private final UsuarioRepository usuarioRepo;

    public AdjuntoService(AdjuntoRepository repo, OrganizacionRepository orgRepo,
                          PacienteRepository pacienteRepo, CitaRepository citaRepo,
                          UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.pacienteRepo = pacienteRepo;
        this.citaRepo = citaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public List<AdjuntoResponse> listarPorPaciente(UUID pacienteId) {
        return repo.findByPacienteId(pacienteId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AdjuntoResponse> listarPorCita(UUID citaId) {
        return repo.findByCitaId(citaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AdjuntoResponse> listarPendientesDeNotificar(UUID organizacionId) {
        return repo.findByOrganizacionIdAndNotificarTrue(organizacionId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AdjuntoResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto", id.toString()));
    }

    public AdjuntoResponse crear(AdjuntoRequest req) {
        Organizacion org = orgRepo.findById(req.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));
        Paciente paciente = pacienteRepo.findById(req.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", req.getPacienteId().toString()));
        Usuario subidoPor = usuarioRepo.findById(req.getSubidoPorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", req.getSubidoPorId().toString()));
        Adjunto a = new Adjunto();
        a.setOrganizacion(org);
        a.setPaciente(paciente);
        a.setSubidoPor(subidoPor);
        a.setTipo(req.getTipo());
        a.setNombreArchivo(req.getNombreArchivo());
        a.setUrlArchivo(req.getUrlArchivo());
        a.setMimeType(req.getMimeType());
        a.setNotificar(req.getNotificar() != null ? req.getNotificar() : false);
        if (req.getCitaId() != null) {
            Cita cita = citaRepo.findById(req.getCitaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cita", req.getCitaId().toString()));
            a.setCita(cita);
        }
        return toResponse(repo.save(a));
    }

    public AdjuntoResponse actualizar(UUID id, AdjuntoRequest req) {
        Adjunto a = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto", id.toString()));
        a.setTipo(req.getTipo());
        a.setNombreArchivo(req.getNombreArchivo());
        a.setUrlArchivo(req.getUrlArchivo());
        a.setMimeType(req.getMimeType());
        a.setNotificar(req.getNotificar() != null ? req.getNotificar() : false);
        return toResponse(repo.save(a));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private AdjuntoResponse toResponse(Adjunto a) {
        AdjuntoResponse r = new AdjuntoResponse();
        r.setId(a.getId());
        r.setOrganizacionId(a.getOrganizacion().getId());
        r.setPacienteId(a.getPaciente().getId());
        r.setCitaId(a.getCita() != null ? a.getCita().getId() : null);
        r.setSubidoPorId(a.getSubidoPor().getId());
        r.setTipo(a.getTipo());
        r.setNombreArchivo(a.getNombreArchivo());
        r.setUrlArchivo(a.getUrlArchivo());
        r.setMimeType(a.getMimeType());
        r.setNotificar(a.getNotificar());
        r.setNotificadoEn(a.getNotificadoEn());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
