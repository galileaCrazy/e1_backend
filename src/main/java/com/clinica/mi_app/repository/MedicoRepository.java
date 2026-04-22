package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, UUID> {
    List<Medico> findByOrganizacionId(UUID organizacionId);
    List<Medico> findByOrganizacionIdAndActivoTrue(UUID organizacionId);
    List<Medico> findByConsultorioId(UUID consultorioId);
}