package com.clinica.mi_app.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class IAService {

    private final WebClient fastapiWebClient;

    public IAService(WebClient fastapiWebClient) {
        this.fastapiWebClient = fastapiWebClient;
    }

    @CircuitBreaker(name = "fastapi-ia", fallbackMethod = "fallbackDisponibilidad")
    public String consultarDisponibilidad(String orgId) {
        return fastapiWebClient.get()
                .uri("/api/ia/disponibilidad/{orgId}", orgId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String fallbackDisponibilidad(String orgId, Exception ex) {
        return "{\"mensaje\": \"Servicio de IA no disponible. Contacte directamente a la clínica.\"}";
    }

    @CircuitBreaker(name = "fastapi-ia", fallbackMethod = "fallbackChat")
    public String enviarMensajeChat(String mensaje) {
        var body = Map.of("mensaje", mensaje);
        return fastapiWebClient.post()
                .uri("/api/ia/chat")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String fallbackChat(String mensaje, Exception ex) {
        return "{\"respuesta\": \"El asistente de IA no está disponible en este momento.\"}";
    }
}
