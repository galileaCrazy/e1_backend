package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Notificacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByCitaId(UUID citaId);
    List<Notificacion> findByOrganizacionIdAndEstado(UUID organizacionId, String estado);
    List<Notificacion> findByEstado(String estado);

    List<Notificacion> findByOrganizacionIdAndCitaIdIn(UUID organizacionId, Collection<UUID> citaIds);

    // Campana de notificaciones (admin: toda la organización; médico: solo sus citas)
    @EntityGraph(attributePaths = {"cita", "cita.paciente", "cita.medico"})
    List<Notificacion> findTop20ByOrganizacionIdAndTipoOrderByCreadaEnDesc(UUID organizacionId, String tipo);

    @EntityGraph(attributePaths = {"cita", "cita.paciente", "cita.medico"})
    List<Notificacion> findTop20ByOrganizacionIdAndTipoAndCita_Medico_IdOrderByCreadaEnDesc(
            UUID organizacionId, String tipo, UUID medicoId);

    long countByOrganizacionIdAndTipoAndLeidaFalse(UUID organizacionId, String tipo);

    long countByOrganizacionIdAndTipoAndCita_Medico_IdAndLeidaFalse(UUID organizacionId, String tipo, UUID medicoId);

    List<Notificacion> findByOrganizacionIdAndTipoAndLeidaFalse(UUID organizacionId, String tipo);

    List<Notificacion> findByOrganizacionIdAndTipoAndCita_Medico_IdAndLeidaFalse(
            UUID organizacionId, String tipo, UUID medicoId);
}