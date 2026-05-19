package com.clinica.mi_app.service;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.clinica.mi_app.dto.request.LoginRequest;
import com.clinica.mi_app.dto.request.RegistroRequest;
import com.clinica.mi_app.dto.response.AuthResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Usuario;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.UsuarioRepository;
import com.clinica.mi_app.security.JwtUtil;

public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OrganizacionRepository organizacionRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private UUID organizacionId;
    private UUID usuarioId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        organizacionId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
    }

    @Test
    public void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Organizacion org = new Organizacion();
        org.setId(organizacionId);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEmail("user@example.com");
        usuario.setRol("PACIENTE");
        usuario.setOrganizacion(org);

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(usuario)).thenReturn(token);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(usuarioId, response.getUserId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("PACIENTE", response.getRol());
        assertEquals(organizacionId, response.getOrganizacionId());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    public void testLogin_UsuarioNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }

    @Test
    public void testRegistro_Success() {
        // Arrange
        RegistroRequest request = new RegistroRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setOrganizacionId(organizacionId);

        Organizacion org = new Organizacion();
        org.setId(organizacionId);

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setId(usuarioId);
        usuarioGuardado.setEmail("newuser@example.com");
        usuarioGuardado.setRol("PACIENTE");
        usuarioGuardado.setOrganizacion(org);
        usuarioGuardado.setActivo(true);

        String encryptedPassword = "$2a$10$encryptedPassword123...";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(usuarioRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(organizacionRepository.findById(organizacionId)).thenReturn(Optional.of(org));
        when(passwordEncoder.encode("password123")).thenReturn(encryptedPassword);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtUtil.generateToken(any(Usuario.class))).thenReturn(token);

        // Act
        AuthResponse response = authService.registro(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(usuarioId, response.getUserId());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("PACIENTE", response.getRol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    public void testRegistro_EmailAlreadyExists() {
        // Arrange
        RegistroRequest request = new RegistroRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setOrganizacionId(organizacionId);

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setEmail("existing@example.com");

        when(usuarioRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.registro(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    public void testRegistro_OrganizacionNotFound() {
        // Arrange
        RegistroRequest request = new RegistroRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setOrganizacionId(organizacionId);

        when(usuarioRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(organizacionRepository.findById(organizacionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.registro(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
