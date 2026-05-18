package com.clinica.mi_app.service;

import com.clinica.mi_app.dto.request.LoginRequest;
import com.clinica.mi_app.dto.request.RegistroRequest;
import com.clinica.mi_app.dto.response.AuthResponse;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Usuario;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.UsuarioRepository;
import com.clinica.mi_app.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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

    private static final UUID ORG_ID = UUID.fromString("20352770-80b9-403b-9a3d-e55e02e98edd");
    private static final String RAW_PASSWORD = "Password123";
    private static final String HASHED_PASSWORD = "$2a$10$hashed.password.example";

    @Test
    void test_password_hasheado() {
        RegistroRequest req = new RegistroRequest();
        req.setEmail("nuevo@clinic.com");
        req.setPassword(RAW_PASSWORD);
        req.setOrganizacionId(ORG_ID);

        Organizacion org = new Organizacion();
        org.setId(ORG_ID);
        org.setNombre("Clínica San Rafael");
        org.setPlan("CLINICA");

        when(usuarioRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(organizacionRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(jwtUtil.generateToken(any(Usuario.class))).thenReturn("test-jwt-token");

        authService.registro(req);

        // Verificar que se llamó encode() con el password en texto plano
        verify(passwordEncoder).encode(RAW_PASSWORD);

        // Verificar que el usuario guardado tiene el hash, no el texto plano
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioGuardado = captor.getValue();

        assertNotEquals(RAW_PASSWORD, usuarioGuardado.getPasswordHash(),
                "El password guardado no debe ser texto plano");
        assertEquals(HASHED_PASSWORD, usuarioGuardado.getPasswordHash(),
                "El password guardado debe ser el hash BCrypt");
        assertEquals("PACIENTE", usuarioGuardado.getRol());
    }

    @Test
    void test_login_credenciales_incorrectas() {
        LoginRequest req = new LoginRequest();
        req.setEmail("usuario@clinic.com");
        req.setPassword("password_incorrecto");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req),
                "Login con credenciales incorrectas debe lanzar BadCredentialsException");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(usuarioRepository, jwtUtil);
    }
}
