package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.PagoRequest;
import com.clinica.mi_app.dto.response.PagoResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.mapper.PagoMapper;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Pago;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PagoService {

    private final PagoRepository repo;
    private final OrganizacionRepository orgRepo;
    private final CitaRepository citaRepo;

    public PagoService(PagoRepository repo, OrganizacionRepository orgRepo, CitaRepository citaRepo) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.citaRepo = citaRepo;
    }

    public Optional<PagoResponse> buscarPorCita(UUID citaId) {
        return repo.findByCitaId(citaId).map(PagoMapper::toResponse);
    }

    public List<PagoResponse> listarPorOrganizacion(UUID organizacionId) {
        return repo.findByOrganizacionId(organizacionId).stream().map(PagoMapper::toResponse).collect(Collectors.toList());
    }

    public List<PagoResponse> listarPorEstado(UUID organizacionId, String estado) {
        return repo.findByOrganizacionIdAndEstado(organizacionId, estado).stream().map(PagoMapper::toResponse).collect(Collectors.toList());
    }

    public PagoResponse buscarPorId(UUID id) {
        return repo.findById(id).map(PagoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id.toString()));
    }

    public PagoResponse crear(PagoRequest req) {
        Organizacion org = orgRepo.findById(req.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));
        Cita cita = citaRepo.findById(req.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita", req.getCitaId().toString()));
        Pago p = new Pago();
        p.setOrganizacion(org);
        p.setCita(cita);
        p.setMonto(req.getMonto());
        p.setMetodo(req.getMetodo());
        p.setConcepto(req.getConcepto());
        p.setReferencia(req.getReferencia());
        return PagoMapper.toResponse(repo.save(p));
    }

    public PagoResponse actualizar(UUID id, PagoRequest req) {
        Pago p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id.toString()));
        p.setMonto(req.getMonto());
        p.setMetodo(req.getMetodo());
        p.setConcepto(req.getConcepto());
        p.setReferencia(req.getReferencia());
        return PagoMapper.toResponse(repo.save(p));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }
}
