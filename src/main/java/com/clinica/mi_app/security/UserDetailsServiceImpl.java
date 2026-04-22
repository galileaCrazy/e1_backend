package com.clinica.mi_app.security;

import com.clinica.mi_app.model.Usuario;
import com.clinica.mi_app.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Carga el usuario desde la BD por email para que Spring Security lo use en la autenticación.
// Mapea el rol (ADMIN, MEDICO, PACIENTE) a una GrantedAuthority con prefijo ROLE_.
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getActivo(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }
}
