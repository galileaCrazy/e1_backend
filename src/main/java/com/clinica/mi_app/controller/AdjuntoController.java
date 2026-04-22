package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.AdjuntoRequest;
import com.clinica.mi_app.dto.response.AdjuntoResponse;
import com.clinica.mi_app.service.AdjuntoService;
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
@RequestMapping("/api/adjuntos")
@Tag(name = "Adjuntos", description = "Gestión de archivos y documentos")
@SecurityRequirement(name = "bearerAuth")
public class AdjuntoController {

    private final AdjuntoService service;

    public AdjuntoController(AdjuntoService service) {
        this.service = service;
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar archivos de un paciente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de archivos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<AdjuntoResponse> listarPorPaciente(@PathVariable UUID pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }

    @GetMapping("/cita/{citaId}")
    @Operation(summary = "Listar archivos asociados a una cita")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de archivos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<AdjuntoResponse> listarPorCita(@PathVariable UUID citaId) {
        return service.listarPorCita(citaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar adjunto por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo encontrado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public AdjuntoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un adjunto", description = "Almacena la URL del archivo y sus metadatos. El archivo debe subirse previamente a un servicio de almacenamiento")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Archivo creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public AdjuntoResponse crear(@Valid @RequestBody AdjuntoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un adjunto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public AdjuntoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody AdjuntoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un adjunto")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Archivo eliminado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public void eliminar(@PathVariable UUID id) {
        service.buscarPorId(id);
        service.eliminar(id);
    }
}
