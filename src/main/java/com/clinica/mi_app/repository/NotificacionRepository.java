package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByCitaId(UUID citaId);
    List<Notificacion> findByOrganizacionIdAndEstado(UUID organizacionId, String estado);
    List<Notificacion> findByEstado(String estado);
}