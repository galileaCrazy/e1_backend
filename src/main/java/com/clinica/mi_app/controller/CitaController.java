package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.CitaRequest;
import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Agendamiento de citas médicas")
@SecurityRequirement(name = "bearerAuth")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @GetMapping("/organizacion/{orgId}")
    @Operation(summary = "Listar todas las citas de una organización")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de citas"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<CitaResponse> listarPorOrganizacion(@PathVariable UUID orgId) {
        return service.listarPorOrganizacion(orgId);
    }

    @GetMapping("/medico/{medicoId}")
    @Operation(summary = "Listar citas de un médico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de citas"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<CitaResponse> listarPorMedico(@PathVariable UUID medicoId) {
        return service.listarPorMedico(medicoId);
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar citas de un paciente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de citas"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'PACIENTE')")
    public List<CitaResponse> listarPorPaciente(@PathVariable UUID pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }

    @GetMapping("/organizacion/{orgId}/estado/{estado}")
    @Operation(summary = "Listar citas por estado", description = "Estados válidos: SIN_CONFIRMAR, CONFIRMADA, CANCELADA, REAGENDADA, NO_ASISTIO")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de citas"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<CitaResponse> listarPorEstado(@PathVariable UUID orgId, @PathVariable String estado) {
        return service.listarPorEstado(orgId, estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cita por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cita encontrada"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public CitaResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agendar nueva cita", description = "La cita inicia con estado SIN_CONFIRMAR por defecto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cita creada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public CitaResponse crear(@Valid @RequestBody CitaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fecha, duración o motivo de una cita")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cita actualizada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public CitaResponse actualizar(@PathVariable UUID id, @Valid @RequestBody CitaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una cita")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cita eliminada"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public void eliminar(@PathVariable UUID id) {
        service.buscarPorId(id);
        service.eliminar(id);
    }
}
