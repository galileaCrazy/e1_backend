package com.clinica.mi_app.controller;

import com.clinica.mi_app.config.SecurityConfig;
import com.clinica.mi_app.dto.request.LoginRequest;
import com.clinica.mi_app.dto.request.RegistroRequest;
import com.clinica.mi_app.dto.response.AuthResponse;
import com.clinica.mi_app.security.JwtFilter;
import com.clinica.mi_app.security.JwtUtil;
import com.clinica.mi_app.security.UserDetailsServiceImpl;
import com.clinica.mi_app.service.AuthService;
import com.clinica.mi_app.service.PacienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, PacienteController.class})
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
	private JwtFilter jwtFilter;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private PacienteService pacienteService;

	@MockitoBean
	private JwtUtil jwtUtil;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

    private static final UUID ORG_ID = UUID.fromString("20352770-80b9-403b-9a3d-e55e02e98edd");
    private static final UUID PACIENTE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // JwtFilter passes through — let Spring Security handle auth
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void test_registro_exitoso() throws Exception {
        RegistroRequest req = new RegistroRequest();
        req.setEmail("nuevo@clinic.com");
        req.setPassword("Password123");
        req.setOrganizacionId(ORG_ID);

        AuthResponse response = new AuthResponse("jwt-token", UUID.randomUUID(),
                "nuevo@clinic.com", "PACIENTE", ORG_ID);
        when(authService.registro(any(RegistroRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void test_login_exitoso() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@clinic.com");
        req.setPassword("Password123");

        AuthResponse response = new AuthResponse("jwt-token", UUID.randomUUID(),
                "admin@clinic.com", "ADMIN", ORG_ID);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void test_sin_token() throws Exception {
        mockMvc.perform(get("/api/pacientes/" + PACIENTE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_token_invalido() throws Exception {
        // jwtUtil.isTokenValid() returns false by default (Mockito default)
        mockMvc.perform(get("/api/pacientes/" + PACIENTE_ID)
                        .header("Authorization", "Bearer token.inventado.invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_rol_insuficiente() throws Exception {
        mockMvc.perform(delete("/api/pacientes/" + PACIENTE_ID)
                        .with(user("paciente").roles("PACIENTE")))
                .andExpect(status().isForbidden());
    }
}
