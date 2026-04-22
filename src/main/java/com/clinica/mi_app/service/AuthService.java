package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.LoginRequest;
import com.clinica.mi_app.dto.request.RegistroRequest;
import com.clinica.mi_app.dto.response.AuthResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Usuario;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.UsuarioRepository;
import com.clinica.mi_app.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       OrganizacionRepository organizacionRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.organizacionRepository = organizacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", req.getEmail()));
        String token = jwtUtil.generateToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getEmail(),
            usuario.getRol(), usuario.getOrganizacion().getId());
    }

    public AuthResponse registro(RegistroRequest req) {
        if (usuarioRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        Organizacion org = organizacionRepository.findById(req.getOrganizacionId())
            .orElseThrow(() -> new ResourceNotFoundException("Organizacion", req.getOrganizacionId().toString()));

        Usuario usuario = new Usuario();
        usuario.setEmail(req.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        usuario.setRol("PACIENTE");
        usuario.setOrganizacion(org);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getEmail(),
            usuario.getRol(), usuario.getOrganizacion().getId());
    }
}
