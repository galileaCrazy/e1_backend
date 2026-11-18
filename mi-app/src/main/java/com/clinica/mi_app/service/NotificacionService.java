package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.NotificacionRequest;
import com.clinica.mi_app.dto.response.CampanaResponse;
import com.clinica.mi_app.dto.response.NotificacionRecienteResponse;
import com.clinica.mi_app.dto.response.NotificacionResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.mapper.NotificacionMapper;
import com.clinica.mi_app.model.Adjunto;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Notificacion;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.repository.AdjuntoRepository;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.NotificacionRepository;
import com.clinica.mi_app.repository.OrganizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ── Campana: cancelaciones del paciente (admin: toda la org; médico: solo sus citas) ──

    public static final String CANCELACION_PACIENTE = "CANCELACION_PACIENTE";

    @Transactional(readOnly = true)
    public CampanaResponse campana(UUID organizacionId, UUID medicoId) {
        List<Notificacion> items = medicoId == null
                ? repo.findTop20ByOrganizacionIdAndTipoOrderByCreadaEnDesc(organizacionId, CANCELACION_PACIENTE)
                : repo.findTop20ByOrganizacionIdAndTipoAndCita_Medico_IdOrderByCreadaEnDesc(organizacionId, CANCELACION_PACIENTE, medicoId);
        long noLeidas = medicoId == null
                ? repo.countByOrganizacionIdAndTipoAndLeidaFalse(organizacionId, CANCELACION_PACIENTE)
                : repo.countByOrganizacionIdAndTipoAndCita_Medico_IdAndLeidaFalse(organizacionId, CANCELACION_PACIENTE, medicoId);
        return new CampanaResponse(noLeidas, items.stream().map(NotificacionRecienteResponse::of).toList());
    }

    @Transactional
    public void marcarLeida(UUID organizacionId, UUID medicoId, UUID id) {
        Notificacion n = repo.findById(id)
                .filter(x -> x.getOrganizacion().getId().equals(organizacionId))
                .filter(x -> medicoId == null || x.getCita().getMedico().getId().equals(medicoId))
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", id.toString()));
        n.setLeida(true);
        repo.save(n);
    }

    @Transactional
    public int marcarTodasLeidas(UUID organizacionId, UUID medicoId) {
        List<Notificacion> pendientes = medicoId == null
                ? repo.findByOrganizacionIdAndTipoAndLeidaFalse(organizacionId, CANCELACION_PACIENTE)
                : repo.findByOrganizacionIdAndTipoAndCita_Medico_IdAndLeidaFalse(organizacionId, CANCELACION_PACIENTE, medicoId);
        pendientes.forEach(n -> n.setLeida(true));
        repo.saveAll(pendientes);
        return pendientes.size();
    }

    public List<NotificacionResponse> listarPorCita(UUID citaId) {
        return repo.findByCitaId(citaId).stream().map(NotificacionMapper::toResponse).collect(Collectors.toList());
    }

    public List<NotificacionResponse> listarPendientes() {
        return repo.findByEstado("PENDIENTE").stream().map(NotificacionMapper::toResponse).collect(Collectors.toList());
    }

    public List<NotificacionResponse> listarPorEstado(UUID organizacionId, String estado) {
        return repo.findByOrganizacionIdAndEstado(organizacionId, estado).stream().map(NotificacionMapper::toResponse).collect(Collectors.toList());
    }

    public NotificacionResponse buscarPorId(UUID id) {
        return repo.findById(id).map(NotificacionMapper::toResponse)
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
        return NotificacionMapper.toResponse(repo.save(n));
    }

    public NotificacionResponse actualizar(UUID id, NotificacionRequest req) {
        Notificacion n = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", id.toString()));
        n.setCanal(req.getCanal());
        n.setTipo(req.getTipo());
        return NotificacionMapper.toResponse(repo.save(n));
    }

    public void eliminar(UUID id) {
        repo.deleteById(id);
    }
}
