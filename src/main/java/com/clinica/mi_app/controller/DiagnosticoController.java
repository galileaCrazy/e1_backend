package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.DiagnosticoRequest;
import com.clinica.mi_app.dto.response.DiagnosticoResponse;
import com.clinica.mi_app.service.DiagnosticoService;
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
@RequestMapping("/api/diagnosticos")
@Tag(name = "Diagnósticos", description = "Diagnósticos clínicos")
@SecurityRequirement(name = "bearerAuth")
public class DiagnosticoController {

    private final DiagnosticoService service;

    public DiagnosticoController(DiagnosticoService service) {
        this.service = service;
    }

    @GetMapping("/cita/{citaId}")
    @Operation(summary = "Listar diagnósticos de una cita")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de diagnósticos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<DiagnosticoResponse> listarPorCita(@PathVariable UUID citaId) {
        return service.listarPorCita(citaId);
    }

    @GetMapping("/cita/{citaId}/tipo/{tipo}")
    @Operation(summary = "Listar diagnósticos de una cita por tipo", description = "Tipos válidos: PRINCIPAL, SECUNDARIO")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de diagnósticos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<DiagnosticoResponse> listarPorCitaYTipo(@PathVariable UUID citaId, @PathVariable String tipo) {
        return service.listarPorCitaYTipo(citaId, tipo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar diagnóstico por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Diagnóstico encontrado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "404", description = "Diagnóstico no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public DiagnosticoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar diagnóstico de una cita", description = "El código CIE-10 es opcional. Tipos permitidos: PRINCIPAL, SECUNDARIO")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Diagnóstico creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public DiagnosticoResponse crear(@Valid @RequestBody DiagnosticoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un diagnóstico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Diagnóstico actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Diagnóstico no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public DiagnosticoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody DiagnosticoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un diagnóstico")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Diagnóstico eliminado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Diagnóstico no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public void eliminar(@PathVariable UUID id) {
        service.buscarPorId(id);
        service.eliminar(id);
    }
}
