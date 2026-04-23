package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OrganizacionResponse {

    private UUID id;
    private String nombre;
    private String plan;
    private OffsetDateTime trialHasta;
    private Boolean activo;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public OffsetDateTime getTrialHasta() { return trialHasta; }
    public void setTrialHasta(OffsetDateTime trialHasta) { this.trialHasta = trialHasta; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
