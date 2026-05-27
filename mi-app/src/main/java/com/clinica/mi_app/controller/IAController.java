package com.clinica.mi_app.controller;

import com.clinica.mi_app.dto.response.CitaResponse;
import com.clinica.mi_app.dto.response.MedicoPublicoResponse;
import com.clinica.mi_app.service.IAService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    @GetMapping("/medicos/{orgId}")
    public ResponseEntity<List<MedicoPublicoResponse>> medicosParaIA(@PathVariable UUID orgId) {
        return ResponseEntity.ok(iaService.getMedicosPublicos(orgId));
    }

    @GetMapping("/horarios/{medicoId}")
    public ResponseEntity<Map<String, Object>> horariosParaIA(
            @PathVariable UUID medicoId,
            @RequestParam String fecha) {
        List<String> slots = iaService.getSlotsDisponibles(medicoId, LocalDate.parse(fecha));
        return ResponseEntity.ok(Map.of("slots", slots));
    }

    @GetMapping("/disponibilidad/{orgId}")
    public ResponseEntity<String> disponibilidad(@PathVariable String orgId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(iaService.consultarDisponibilidad(orgId, authHeader));
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String mensaje = body.getOrDefault("mensaje", "").toString();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> historial = (List<Map<String, String>>) body.get("historial");
        return ResponseEntity.ok(iaService.enviarMensajeChat(mensaje, historial, authHeader));
    }

    @PostMapping("/agendar")
    public ResponseEntity<?> agendar(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            CitaResponse cita = iaService.agendarCitaDesdeIA(body, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
