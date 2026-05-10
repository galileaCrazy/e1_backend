package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.UsuarioResponse;
import com.clinica.mi_app.model.Usuario;

public class UsuarioMapper {

    public static UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setOrganizacionId(u.getOrganizacion().getId());
        r.setMedicoId(u.getMedico() != null ? u.getMedico().getId() : null);
        r.setEmail(u.getEmail());
        r.setRol(u.getRol());
        r.setActivo(u.getActivo());
        return r;
    }
}
