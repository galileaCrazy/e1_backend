package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.NotificacionRequest;
import com.clinica.mi_app.dto.response.NotificacionResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Adjunto;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Notificacion;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.repository.AdjuntoRepository;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.NotificacionRepository;
import com.clinica.mi_app.repository.OrganizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private final NotificacionRepository repo;
    private final OrganizacionRepository orgRepo;
    private final CitaRepository citaRepo;
    private final AdjuntoRepository adjuntoRepo;

    public NotificacionService(NotificacionRepository repo, OrganizacionRepository orgRepo,
                               CitaRepository citaRepo, AdjuntoRepository adjuntoRepo) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.citaRepo = citaRepo;
        this.adjuntoRepo = adjuntoRepo;
    }

    public List<NotificacionResponse> listarPorCita(UUID citaId) {
        return repo.findByCitaId(citaId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificacionResponse> listarPendientes() {
        return repo.findByEstado("PENDIENTE").stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificacionResponse> listarPorEstado(UUID organizacionId, String estado) {
        return repo.findByOrganizacionIdAndEstado(organizacionId, estado).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public NotificacionResponse buscarPorId(UUID id) {
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", id.toString()));
    }

    public NotificacionResponse crear(NotificacionRequest req) {
        Organizacion org = orgRepo.findById(req.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));
        Cita cita = citaRepo.findById(req.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita", req.getCitaId().toString()));
        Notificacion n = new Notificacion();
        n.setOrganizacion(org);
        n.setCita(cita);
        n.setCanal(req.getCanal());
        n.setTipo(req.getTipo());
        if (req.getAdjuntoId() != null) {
            Adjunto adjunto = adjuntoRepo.findById(req.getAdjuntoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Adjunto", req.getAdjuntoId().toString()));
            n.setAdjunto(adjunto);
        }
        return toResponse(repo.save(n));
    }

    public NotificacionResponse actualizar(UUID id, NotificacionRequest req) {
        Notificacion n = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", id.toString()));
        n.setCanal(req.getCanal());
        n.setTipo(req.getTipo());
        return toResponse(repo.save(n));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private NotificacionResponse toResponse(Notificacion n) {
        NotificacionResponse r = new NotificacionResponse();
        r.setId(n.getId());
        r.setOrganizacionId(n.getOrganizacion().getId());
        r.setCitaId(n.getCita().getId());
        r.setAdjuntoId(n.getAdjunto() != null ? n.getAdjunto().getId() : null);
        r.setCanal(n.getCanal());
        r.setTipo(n.getTipo());
        r.setEstado(n.getEstado());
        r.setEnviadaEn(n.getEnviadaEn());
        r.setRespuesta(n.getRespuesta());
        return r;
    }
}
