package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.OrganizacionRequest;
import com.clinica.mi_app.dto.response.OrganizacionResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.repository.OrganizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizacionService {

    private final OrganizacionRepository repo;

    public OrganizacionService(OrganizacionRepository repo) {
        this.repo = repo;
    }

    public List<OrganizacionResponse> listarTodas() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrganizacionResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", id.toString()));
    }

    public OrganizacionResponse crear(OrganizacionRequest req) {
        Organizacion org = new Organizacion();
        org.setNombre(req.getNombre());
        org.setPlan(req.getPlan());
        org.setTrialHasta(req.getTrialHasta());
        return toResponse(repo.save(org));
    }

    public OrganizacionResponse actualizar(UUID id, OrganizacionRequest req) {
        Organizacion org = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", id.toString()));
        org.setNombre(req.getNombre());
        org.setPlan(req.getPlan());
        org.setTrialHasta(req.getTrialHasta());
        return toResponse(repo.save(org));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private OrganizacionResponse toResponse(Organizacion o) {
        OrganizacionResponse r = new OrganizacionResponse();
        r.setId(o.getId());
        r.setNombre(o.getNombre());
        r.setPlan(o.getPlan());
        r.setTrialHasta(o.getTrialHasta());
        r.setActivo(o.getActivo());
        r.setCreatedAt(o.getCreatedAt());
        return r;
    }
}
