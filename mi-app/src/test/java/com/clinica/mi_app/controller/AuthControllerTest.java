package com.clinica.mi_app.controller;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.mi_app.dto.request.LoginRequest;
import com.clinica.mi_app.dto.request.RegistroRequest;
import com.clinica.mi_app.dto.response.AuthResponse;
import com.clinica.mi_app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(AuthController.class)
@TestPropertySource(properties = {"jwt.secret=ZmFrZXNlY3JldGtleWZha2VzZWNyZXQ=", "jwt.expiration=3600000"})
public class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return org.mockito.Mockito.mock(AuthService.class);
        }
        @Bean
        public com.clinica.mi_app.security.JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(com.clinica.mi_app.security.JwtUtil.class);
        }
        @Bean
        public com.clinica.mi_app.security.UserDetailsServiceImpl userDetailsServiceImpl() {
            return org.mockito.Mockito.mock(com.clinica.mi_app.security.UserDetailsServiceImpl.class);
        }
        @Bean
        public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }
    }

    @Autowired
    private ObjectMapper objectMapper;

    private UUID organizacionId;
    private UUID usuarioId;

    @BeforeEach
    public void setup() {
        organizacionId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = new AuthResponse(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            usuarioId, "user@example.com", "PACIENTE", organizacionId
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.rol", is("PACIENTE")));
    }

    @Test
    public void testLogin_InvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_EmptyEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegistro_Success() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setOrganizacionId(organizacionId);

        AuthResponse authResponse = new AuthResponse(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            usuarioId, "newuser@example.com", "PACIENTE", organizacionId
        );

        when(authService.registro(any(RegistroRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.rol", is("PACIENTE")));
    }

    @Test
    public void testRegistro_PasswordTooShort() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("short");
        request.setOrganizacionId(organizacionId);

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegistro_InvalidEmail() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setOrganizacionId(organizacionId);

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}