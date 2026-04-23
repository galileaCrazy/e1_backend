package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.OrganizacionRequest;
import com.clinica.mi_app.dto.response.OrganizacionResponse;
import com.clinica.mi_app.service.OrganizacionService;
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
@RequestMapping("/api/organizaciones")
@Tag(name = "Organizaciones", description = "Gestión de organizaciones/clínicas")
@SecurityRequirement(name = "bearerAuth")
public class OrganizacionController {

    private final OrganizacionService service;

    public OrganizacionController(OrganizacionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las organizaciones")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de organizaciones"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrganizacionResponse> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar organización por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organización encontrada"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Organización no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizacionResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nueva organización", description = "Planes disponibles: SOLO, CLINICA, ENTERPRISE")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Organización creada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizacionResponse crear(@Valid @RequestBody OrganizacionRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de una organización")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organización actualizada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Organización no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizacionResponse actualizar(@PathVariable UUID id, @Valid @RequestBody OrganizacionRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una organización")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Organización eliminada"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Organización no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable UUID id) {
        service.buscarPorId(id);
        service.eliminar(id);
    }
}
