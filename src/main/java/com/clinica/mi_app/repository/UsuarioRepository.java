package com.clinica.mi_app.repository;

import com.clinica.mi_app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByOrganizacionId(UUID organizacionId);
    List<Usuario> findByOrganizacionIdAndActivoTrue(UUID organizacionId);
}