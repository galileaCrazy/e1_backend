package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.UsuarioRequest;
import com.clinica.mi_app.dto.response.UsuarioResponse;
import com.clinica.mi_app.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/organizacion/{orgId}")
    @Operation(summary = "Listar usuarios de una organización")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public List<UsuarioResponse> listarPorOrganizacion(@PathVariable UUID orgId) {
        return service.listarPorOrganizacion(orgId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario", description = "Roles permitidos: ADMIN, MEDICO. La contraseña se almacena hasheada con BCrypt")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya existe"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse actualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "401", description = "Token requerido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable UUID id) {
        service.buscarPorId(id);
        service.eliminar(id);
    }
}
