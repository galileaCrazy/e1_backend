package com.clinica.mi_app.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.clinica.mi_app.dto.request.CitaRequest;
import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Consultorio;
import com.clinica.mi_app.model.Medico;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Paciente;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.ConsultorioRepository;
import com.clinica.mi_app.repository.MedicoRepository;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.PacienteRepository;

public class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private OrganizacionRepository organizacionRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private ConsultorioRepository consultorioRepository;

    @InjectMocks
    private CitaService citaService;

    private UUID organizacionId;
    private UUID pacienteId;
    private UUID medicoId;
    private UUID consultorioId;
    private UUID citaId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        organizacionId = UUID.randomUUID();
        pacienteId = UUID.randomUUID();
        medicoId = UUID.randomUUID();
        consultorioId = UUID.randomUUID();
        citaId = UUID.randomUUID();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String rol, UUID userId, UUID orgId) {
        var auth = new UsernamePasswordAuthenticationToken("test@clinic.com", null, List.of());
        auth.setDetails(Map.of("rol", rol, "userId", userId, "organizacionId", orgId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testCrearCita_Success() {
        // Arrange
        CitaRequest request = new CitaRequest();
        request.setOrganizacionId(organizacionId);
        request.setPacienteId(pacienteId);
        request.setMedicoId(medicoId);
        request.setConsultorioId(consultorioId);
        request.setFechaHora(OffsetDateTime.now().plusHours(1));
        request.setDuracionMin((short) 30);
        request.setMotivo("Consulta general");

        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        Medico medico = new Medico();
        medico.setId(medicoId);
        Consultorio consultorio = new Consultorio();
        consultorio.setId(consultorioId);

        Cita citaGuardada = new Cita();
        citaGuardada.setId(citaId);
        citaGuardada.setOrganizacion(org);
        citaGuardada.setPaciente(paciente);
        citaGuardada.setMedico(medico);
        citaGuardada.setConsultorio(consultorio);
        citaGuardada.setFechaHora(request.getFechaHora());
        citaGuardada.setDuracionMin(request.getDuracionMin());
        citaGuardada.setMotivo(request.getMotivo());

        when(organizacionRepository.findById(organizacionId)).thenReturn(Optional.of(org));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(consultorioRepository.findById(consultorioId)).thenReturn(Optional.of(consultorio));
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        // Act
        CitaResponse response = citaService.crear(request);

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals(pacienteId, response.getPacienteId());
        assertEquals(medicoId, response.getMedicoId());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    public void testCrearCita_OrganizacionNotFound() {
        // Arrange
        CitaRequest request = new CitaRequest();
        request.setOrganizacionId(organizacionId);
        request.setPacienteId(pacienteId);
        request.setMedicoId(medicoId);
        request.setConsultorioId(consultorioId);
        request.setFechaHora(OffsetDateTime.now().plusHours(1));
        request.setDuracionMin((short) 30);
        request.setMotivo("Consulta general");

        when(organizacionRepository.findById(organizacionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> citaService.crear(request));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    public void testBuscarPorId_Success() {
        // Arrange
        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        Medico medico = new Medico();
        medico.setId(medicoId);
        Consultorio consultorio = new Consultorio();
        consultorio.setId(consultorioId);

        Cita cita = new Cita();
        cita.setId(citaId);
        cita.setOrganizacion(org);
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setConsultorio(consultorio);
        cita.setFechaHora(OffsetDateTime.now().plusHours(1));
        cita.setDuracionMin((short) 30);
        cita.setMotivo("Consulta general");

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        autenticarComo("ADMIN", UUID.randomUUID(), organizacionId);

        // Act
        CitaResponse response = citaService.buscarPorId(citaId);

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals(medicoId, response.getMedicoId());
        verify(citaRepository, times(1)).findById(citaId);
    }

    @Test
    public void testBuscarPorId_NotFound() {
        // Arrange
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> citaService.buscarPorId(citaId));
    }

    @Test
    public void testListarPorOrganizacion_Success() {
        // Arrange
        Organizacion org1 = new Organizacion();
        org1.setId(organizacionId);
        Organizacion org2 = new Organizacion();
        org2.setId(organizacionId);

        Paciente paciente1 = new Paciente();
        paciente1.setId(pacienteId);
        Paciente paciente2 = new Paciente();
        paciente2.setId(UUID.randomUUID());

        Medico medico1 = new Medico();
        medico1.setId(medicoId);
        Medico medico2 = new Medico();
        medico2.setId(UUID.randomUUID());

        Consultorio consultorio1 = new Consultorio();
        consultorio1.setId(consultorioId);
        Consultorio consultorio2 = new Consultorio();
        consultorio2.setId(UUID.randomUUID());

        Cita cita1 = new Cita();
        cita1.setId(UUID.randomUUID());
        cita1.setOrganizacion(org1);
        cita1.setPaciente(paciente1);
        cita1.setMedico(medico1);
        cita1.setConsultorio(consultorio1);
        cita1.setFechaHora(OffsetDateTime.now().plusHours(1));

        Cita cita2 = new Cita();
        cita2.setId(UUID.randomUUID());
        cita2.setOrganizacion(org2);
        cita2.setPaciente(paciente2);
        cita2.setMedico(medico2);
        cita2.setConsultorio(consultorio2);
        cita2.setFechaHora(OffsetDateTime.now().plusHours(2));

        List<Cita> citas = List.of(cita1, cita2);

        when(citaRepository.findByOrganizacionId(organizacionId)).thenReturn(citas);

        // Act
        List<CitaResponse> responses = citaService.listarPorOrganizacion(organizacionId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(citaRepository, times(1)).findByOrganizacionId(organizacionId);
    }

    @Test
    public void testActualizarCita_Success() {
        // Arrange
        CitaRequest request = new CitaRequest();
        request.setFechaHora(OffsetDateTime.now().plusHours(2));
        request.setDuracionMin((short) 45);
        request.setMotivo("Consulta actualizada");

        Cita citaExistente = new Cita();
        citaExistente.setId(citaId);
        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        Medico medico = new Medico();
        medico.setId(medicoId);
        Consultorio consultorio = new Consultorio();
        consultorio.setId(consultorioId);

        citaExistente.setOrganizacion(org);
        citaExistente.setPaciente(paciente);
        citaExistente.setMedico(medico);
        citaExistente.setConsultorio(consultorio);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaExistente));
        when(citaRepository.save(any(Cita.class))).thenReturn(citaExistente);

        // Act
        CitaResponse response = citaService.actualizar(citaId, request);

        // Assert
        assertNotNull(response);
        verify(citaRepository, times(1)).save(any(Cita.class));
    }
}
