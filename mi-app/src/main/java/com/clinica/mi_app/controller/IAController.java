package com.clinica.mi_app.controller;

import com.clinica.mi_app.service.IAService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    @GetMapping("/disponibilidad/{orgId}")
    public ResponseEntity<String> disponibilidad(@PathVariable String orgId) {
        return ResponseEntity.ok(iaService.consultarDisponibilidad(orgId));
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> body) {
        String respuesta = iaService.enviarMensajeChat(body.get("mensaje"));
        return ResponseEntity.ok(respuesta);
    }
}
