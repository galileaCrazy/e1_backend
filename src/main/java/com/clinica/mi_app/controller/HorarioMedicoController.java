package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.HorarioMedicoRequest;
import com.clinica.mi_app.dto.response.HorarioMedicoResponse;
import com.clinica.mi_app.service.HorarioMedicoService;
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
@RequestMapping("/api/horarios")
@Tag(name = "Horarios", description = "Horarios de atención médica")
@SecurityRequirement(name = "bearerAuth")
public class HorarioMedicoController {

    private final HorarioMedicoService service;

    public HorarioMedicoController(HorarioMedicoService service) {
        this.service = service;
    }

    @GetMapping("/medico/{medicoId}")
    @Operation(summary = "Listar todos los horarios de un médico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de horarios"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<HorarioMedicoResponse> listarPorMedico(@PathVariable UUID medicoId) {
        return service.listarPorMedico(medicoId);
    }

    @GetMapping("/medico/{medicoId}/dia/{dia}")
    @Operation(summary = "Listar horarios de un médico por día", description = "Día de la semana: 0=Domingo, 1=Lunes, ..., 6=Sábado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de horarios"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<HorarioMedicoResponse> listarPorMedicoYDia(@PathVariable UUID medicoId, @PathVariable Short dia) {
        return service.listarPorMedicoYDia(medicoId, dia);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar horario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Horario encontrado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public HorarioMedicoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar horario de atención", description = "Duración de consulta permitida: 20, 30, 45 o 60 minutos")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Horario creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public HorarioMedicoResponse crear(@Valid @RequestBody HorarioMedicoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar horario de atención")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Horario actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public HorarioMedicoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody HorarioMedicoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar horario de atención")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Horario eliminado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable UUID id) {
        service.buscarPorId(id);
        service.eliminar(id);
    }
}
