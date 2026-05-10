package com.clinica.mi_app.controller;

import com.clinica.mi_app.config.SecurityConfig;
import com.clinica.mi_app.dto.request.CitaRequest;
import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.security.JwtFilter;
import com.clinica.mi_app.security.JwtUtil;
import com.clinica.mi_app.security.UserDetailsServiceImpl;
import com.clinica.mi_app.service.CitaService;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CitaController.class)
@Import(SecurityConfig.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
	private JwtFilter jwtFilter;

	@MockitoBean
	private CitaService citaService;

	@MockitoBean
	private JwtUtil jwtUtil;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

    private static final UUID CITA_ID = UUID.randomUUID();
    private static final UUID PACIENTE_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.fromString("20352770-80b9-403b-9a3d-e55e02e98edd");

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    private CitaResponse buildCitaResponse() {
        CitaResponse r = new CitaResponse();
        r.setId(CITA_ID);
        r.setOrganizacionId(ORG_ID);
        r.setPacienteId(PACIENTE_ID);
        r.setMedicoId(UUID.randomUUID());
        r.setConsultorioId(UUID.randomUUID());
        r.setFechaHora(OffsetDateTime.now().plusDays(1));
        r.setDuracionMin((short) 30);
        r.setEstado("SIN_CONFIRMAR");
        return r;
    }

    @Test
    void test_listar_citas_autenticado() throws Exception {
        when(citaService.listarPorPaciente(PACIENTE_ID)).thenReturn(List.of(buildCitaResponse()));

        mockMvc.perform(get("/api/citas/paciente/" + PACIENTE_ID)
                        .with(user("medico").roles("MEDICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].estado").value("SIN_CONFIRMAR"));
    }

    @Test
    void test_crear_cita_admin() throws Exception {
        CitaRequest req = new CitaRequest();
        req.setOrganizacionId(ORG_ID);
        req.setPacienteId(PACIENTE_ID);
        req.setMedicoId(UUID.randomUUID());
        req.setConsultorioId(UUID.randomUUID());
        req.setFechaHora(OffsetDateTime.now().plusDays(1));
        req.setDuracionMin((short) 30);

        when(citaService.crear(any(CitaRequest.class))).thenReturn(buildCitaResponse());

        mockMvc.perform(post("/api/citas")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void test_eliminar_cita_paciente_denegado() throws Exception {
        mockMvc.perform(delete("/api/citas/" + CITA_ID)
                        .with(user("paciente").roles("PACIENTE")))
                .andExpect(status().isForbidden());
    }
}
