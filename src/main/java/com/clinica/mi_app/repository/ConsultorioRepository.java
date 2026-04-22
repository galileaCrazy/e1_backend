package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultorioRepository extends JpaRepository<Consultorio, UUID> {
    List<Consultorio> findByOrganizacionId(UUID organizacionId);
    List<Consultorio> findByOrganizacionIdAndActivoTrue(UUID organizacionId);
}