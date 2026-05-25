package com.clinica.mi_app.dto.response;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private UUID userId;
    private String email;
    private String rol;
    private UUID organizacionId;
    private UUID medicoId;

    public AuthResponse(String token, UUID userId, String email, String rol, UUID organizacionId) {
        this(token, userId, email, rol, organizacionId, null);
    }

    public AuthResponse(String token, UUID userId, String email, String rol, UUID organizacionId, UUID medicoId) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.rol = rol;
        this.organizacionId = organizacionId;
        this.medicoId = medicoId;
    }

    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public UUID getOrganizacionId() { return organizacionId; }
    public UUID getMedicoId() { return medicoId; }
}
