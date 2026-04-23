package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, UUID> {
    List<Diagnostico> findByCitaId(UUID citaId);
    List<Diagnostico> findByCitaIdAndTipo(UUID citaId, String tipo);
}