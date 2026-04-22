package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.request.LoginRequest;
import com.clinica.mi_app.dto.request.RegistroRequest;
import com.clinica.mi_app.dto.response.AuthResponse;
import com.clinica.mi_app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos de autenticación")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y retorna token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso, retorna token JWT"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud"),
        @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar paciente", description = "Crea un nuevo usuario con rol PACIENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya existe")
    })
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.ok(authService.registro(req));
    }
}