package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {
    Optional<Pago> findByCitaId(UUID citaId);
    List<Pago> findByOrganizacionId(UUID organizacionId);
    List<Pago> findByOrganizacionIdAndEstado(UUID organizacionId, String estado);
}