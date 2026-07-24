package com.clinica.mi_app.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final WebClient authWebClient;

    public AuthService(@Qualifier("authServiceWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    public ResponseEntity<String> login(String email, String password, String tenantSlug, String systemId) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("tenantSlug", tenantSlug);
        body.put("systemId", systemId);

        return proxy("/auth/login", body);
    }

    public ResponseEntity<String> registro(String email, String password, String tenantSlug, String systemId) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("tenantSlug", tenantSlug);
        body.put("systemId", systemId);

        return proxy("/auth/registro", body);
    }

    private ResponseEntity<String> proxy(String uri, Map<String, Object> body) {
        try {
            return authWebClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }
}
