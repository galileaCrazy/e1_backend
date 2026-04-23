package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.DiagnosticoRequest;
import com.clinica.mi_app.dto.response.DiagnosticoResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Diagnostico;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.DiagnosticoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository repo;
    private final CitaRepository citaRepo;

    public DiagnosticoService(DiagnosticoRepository repo, CitaRepository citaRepo) {
        this.repo = repo;
        this.citaRepo = citaRepo;
    }

    public List<DiagnosticoResponse> listarPorCita(UUID citaId) {
        return repo.findByCitaId(citaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DiagnosticoResponse> listarPorCitaYTipo(UUID citaId, String tipo) {
        return repo.findByCitaIdAndTipo(citaId, tipo).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DiagnosticoResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostico", id.toString()));
    }

    public DiagnosticoResponse crear(DiagnosticoRequest req) {
        Cita cita = citaRepo.findById(req.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita", req.getCitaId().toString()));
        Diagnostico d = new Diagnostico();
        d.setCita(cita);
        d.setCodigoCie10(req.getCodigoCie10());
        d.setDescripcion(req.getDescripcion());
        d.setTipo(req.getTipo());
        return toResponse(repo.save(d));
    }

    public DiagnosticoResponse actualizar(UUID id, DiagnosticoRequest req) {
        Diagnostico d = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostico", id.toString()));
        d.setCodigoCie10(req.getCodigoCie10());
        d.setDescripcion(req.getDescripcion());
        d.setTipo(req.getTipo());
        return toResponse(repo.save(d));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private DiagnosticoResponse toResponse(Diagnostico d) {
        DiagnosticoResponse r = new DiagnosticoResponse();
        r.setId(d.getId());
        r.setCitaId(d.getCita().getId());
        r.setCodigoCie10(d.getCodigoCie10());
        r.setDescripcion(d.getDescripcion());
        r.setTipo(d.getTipo());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }
}
