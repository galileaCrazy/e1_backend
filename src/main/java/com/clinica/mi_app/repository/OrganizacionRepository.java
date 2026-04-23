package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, UUID> {
}