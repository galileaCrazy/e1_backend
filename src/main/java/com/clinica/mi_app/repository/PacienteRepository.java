package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    List<Paciente> findByOrganizacionId(UUID organizacionId);
    List<Paciente> findByOrganizacionIdAndActivoTrue(UUID organizacionId);
    List<Paciente> findByOrganizacionIdAndNombreContainingIgnoreCase(UUID organizacionId, String nombre);
}