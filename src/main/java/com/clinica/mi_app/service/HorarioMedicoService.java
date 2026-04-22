package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.HorarioMedicoRequest;
import com.clinica.mi_app.dto.response.HorarioMedicoResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.HorarioMedico;
import com.clinica.mi_app.model.Medico;
import com.clinica.mi_app.repository.HorarioMedicoRepository;
import com.clinica.mi_app.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HorarioMedicoService {

    private final HorarioMedicoRepository repo;
    private final MedicoRepository medicoRepo;

    public HorarioMedicoService(HorarioMedicoRepository repo, MedicoRepository medicoRepo) {
        this.repo = repo;
        this.medicoRepo = medicoRepo;
    }

    public List<HorarioMedicoResponse> listarPorMedico(UUID medicoId) {
        return repo.findByMedicoId(medicoId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<HorarioMedicoResponse> listarPorMedicoYDia(UUID medicoId, Short diaSemana) {
        return repo.findByMedicoIdAndDiaSemana(medicoId, diaSemana).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public HorarioMedicoResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("HorarioMedico", id.toString()));
    }

    public HorarioMedicoResponse crear(HorarioMedicoRequest req) {
        Medico medico = medicoRepo.findById(req.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medico", req.getMedicoId().toString()));
        HorarioMedico h = new HorarioMedico();
        h.setMedico(medico);
        h.setDiaSemana(req.getDiaSemana());
        h.setHoraInicio(req.getHoraInicio());
        h.setHoraFin(req.getHoraFin());
        h.setDuracionConsulta(req.getDuracionConsulta());
        return toResponse(repo.save(h));
    }

    public HorarioMedicoResponse actualizar(UUID id, HorarioMedicoRequest req) {
        HorarioMedico h = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HorarioMedico", id.toString()));
        h.setDiaSemana(req.getDiaSemana());
        h.setHoraInicio(req.getHoraInicio());
        h.setHoraFin(req.getHoraFin());
        h.setDuracionConsulta(req.getDuracionConsulta());
        return toResponse(repo.save(h));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private HorarioMedicoResponse toResponse(HorarioMedico h) {
        HorarioMedicoResponse r = new HorarioMedicoResponse();
        r.setId(h.getId());
        r.setMedicoId(h.getMedico().getId());
        r.setDiaSemana(h.getDiaSemana());
        r.setHoraInicio(h.getHoraInicio());
        r.setHoraFin(h.getHoraFin());
        r.setDuracionConsulta(h.getDuracionConsulta());
        return r;
    }
}
