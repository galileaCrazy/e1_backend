package com.clinica.mi_app.service;

import java.math.BigDecimal;
import java.util.List;
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

import com.clinica.mi_app.dto.request.PagoRequest;
import com.clinica.mi_app.dto.response.PagoResponse;
import com.clinica.mi_app.exception.ResourceNotFoundException;
import com.clinica.mi_app.model.Cita;
import com.clinica.mi_app.model.Organizacion;
import com.clinica.mi_app.model.Pago;
import com.clinica.mi_app.repository.CitaRepository;
import com.clinica.mi_app.repository.OrganizacionRepository;
import com.clinica.mi_app.repository.PagoRepository;

public class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private OrganizacionRepository organizacionRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private PagoService pagoService;

    private UUID organizacionId;
    private UUID citaId;
    private UUID pagoId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        organizacionId = UUID.randomUUID();
        citaId = UUID.randomUUID();
        pagoId = UUID.randomUUID();
    }

    @Test
    public void testCrearPago_Success() {
        // Arrange
        PagoRequest request = new PagoRequest();
        request.setOrganizacionId(organizacionId);
        request.setCitaId(citaId);
        request.setMonto(new BigDecimal("100.50"));
        request.setMetodo("TARJETA");
        request.setConcepto("Consulta médica");
        request.setReferencia("REF-001");

        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Cita cita = new Cita();
        cita.setId(citaId);

        Pago pagoGuardado = new Pago();
        pagoGuardado.setId(pagoId);
        pagoGuardado.setOrganizacion(org);
        pagoGuardado.setCita(cita);
        pagoGuardado.setMonto(request.getMonto());
        pagoGuardado.setMetodo(request.getMetodo());
        pagoGuardado.setConcepto(request.getConcepto());
        pagoGuardado.setReferencia(request.getReferencia());

        when(organizacionRepository.findById(organizacionId)).thenReturn(Optional.of(org));
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);

        // Act
        PagoResponse response = pagoService.crear(request);

        // Assert
        assertNotNull(response);
        assertEquals(pagoId, response.getId());
        assertEquals(organizacionId, response.getOrganizacionId());
        assertEquals(citaId, response.getCitaId());
        assertEquals(new BigDecimal("100.50"), response.getMonto());
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    public void testCrearPago_OrganizacionNotFound() {
        // Arrange
        PagoRequest request = new PagoRequest();
        request.setOrganizacionId(organizacionId);
        request.setCitaId(citaId);
        request.setMonto(new BigDecimal("100.50"));
        request.setMetodo("TARJETA");

        when(organizacionRepository.findById(organizacionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pagoService.crear(request));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    public void testBuscarPorId_Success() {
        // Arrange
        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Cita cita = new Cita();
        cita.setId(citaId);

        Pago pago = new Pago();
        pago.setId(pagoId);
        pago.setOrganizacion(org);
        pago.setCita(cita);
        pago.setMonto(new BigDecimal("100.50"));
        pago.setMetodo("TARJETA");

        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        // Act
        PagoResponse response = pagoService.buscarPorId(pagoId);

        // Assert
        assertNotNull(response);
        assertEquals(pagoId, response.getId());
        assertEquals(new BigDecimal("100.50"), response.getMonto());
        verify(pagoRepository, times(1)).findById(pagoId);
    }

    @Test
    public void testBuscarPorId_NotFound() {
        // Arrange
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pagoService.buscarPorId(pagoId));
    }

    @Test
    public void testListarPorOrganizacion_Success() {
        // Arrange
        Organizacion org1 = new Organizacion();
        org1.setId(organizacionId);
        Organizacion org2 = new Organizacion();
        org2.setId(organizacionId);

        Cita cita1 = new Cita();
        cita1.setId(citaId);
        Cita cita2 = new Cita();
        cita2.setId(UUID.randomUUID());

        Pago pago1 = new Pago();
        pago1.setId(UUID.randomUUID());
        pago1.setOrganizacion(org1);
        pago1.setCita(cita1);
        pago1.setMonto(new BigDecimal("50.00"));

        Pago pago2 = new Pago();
        pago2.setId(UUID.randomUUID());
        pago2.setOrganizacion(org2);
        pago2.setCita(cita2);
        pago2.setMonto(new BigDecimal("75.00"));

        List<Pago> pagos = List.of(pago1, pago2);

        when(pagoRepository.findByOrganizacionId(organizacionId)).thenReturn(pagos);

        // Act
        List<PagoResponse> responses = pagoService.listarPorOrganizacion(organizacionId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(pagoRepository, times(1)).findByOrganizacionId(organizacionId);
    }

    @Test
    public void testListarPorEstado_Success() {
        // Arrange
        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Cita cita = new Cita();
        cita.setId(citaId);

        Pago pago1 = new Pago();
        pago1.setId(UUID.randomUUID());
        pago1.setOrganizacion(org);
        pago1.setCita(cita);
        pago1.setEstado("PAGADO");

        List<Pago> pagos = List.of(pago1);

        when(pagoRepository.findByOrganizacionIdAndEstado(organizacionId, "PAGADO")).thenReturn(pagos);

        // Act
        List<PagoResponse> responses = pagoService.listarPorEstado(organizacionId, "PAGADO");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(pagoRepository, times(1)).findByOrganizacionIdAndEstado(organizacionId, "PAGADO");
    }

    @Test
    public void testActualizarPago_Success() {
        // Arrange
        PagoRequest request = new PagoRequest();
        request.setMonto(new BigDecimal("120.00"));
        request.setMetodo("EFECTIVO");

        Organizacion org = new Organizacion();
        org.setId(organizacionId);
        Cita cita = new Cita();
        cita.setId(citaId);
        Pago pagoExistente = new Pago();
        pagoExistente.setId(pagoId);
        pagoExistente.setOrganizacion(org);
        pagoExistente.setCita(cita);

        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pagoExistente));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoExistente);

        // Act
        PagoResponse response = pagoService.actualizar(pagoId, request);

        // Assert
        assertNotNull(response);
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }
}
