package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.PacienteRequest;
import com.clinica.mi_app.dto.response.PacienteResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Paciente;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final PacienteRepository repo;
    private final OrganizacionRepository orgRepo;

    public PacienteService(PacienteRepository repo, OrganizacionRepository orgRepo) {
        this.repo = repo;
        this.orgRepo = orgRepo;
    }

    public List<PacienteResponse> listarPorOrganizacion(UUID organizacionId) {
        return repo.findByOrganizacionId(organizacionId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PacienteResponse> listarActivosPorOrganizacion(UUID organizacionId) {
        return repo.findByOrganizacionIdAndActivoTrue(organizacionId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PacienteResponse> buscarPorNombre(UUID organizacionId, String nombre) {
        return repo.findByOrganizacionIdAndNombreContainingIgnoreCase(organizacionId, nombre)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PacienteResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id.toString()));
    }

    public PacienteResponse crear(PacienteRequest req) {
        Organizacion org = orgRepo.findById(req.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));
        Paciente p = new Paciente();
        p.setOrganizacion(org);
        p.setNombre(req.getNombre());
        p.setTelefono(req.getTelefono());
        p.setFechaNacimiento(req.getFechaNacimiento());
        p.setSexo(req.getSexo());
        p.setEmail(req.getEmail());
        p.setNotas(req.getNotas());
        return toResponse(repo.save(p));
    }

    public PacienteResponse actualizar(UUID id, PacienteRequest req) {
        Paciente p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id.toString()));
        p.setNombre(req.getNombre());
        p.setTelefono(req.getTelefono());
        p.setFechaNacimiento(req.getFechaNacimiento());
        p.setSexo(req.getSexo());
        p.setEmail(req.getEmail());
        p.setNotas(req.getNotas());
        return toResponse(repo.save(p));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private PacienteResponse toResponse(Paciente p) {
        PacienteResponse r = new PacienteResponse();
        r.setId(p.getId());
        r.setOrganizacionId(p.getOrganizacion().getId());
        r.setNombre(p.getNombre());
        r.setTelefono(p.getTelefono());
        r.setFechaNacimiento(p.getFechaNacimiento());
        r.setSexo(p.getSexo());
        r.setEmail(p.getEmail());
        r.setNotas(p.getNotas());
        r.setActivo(p.getActivo());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
