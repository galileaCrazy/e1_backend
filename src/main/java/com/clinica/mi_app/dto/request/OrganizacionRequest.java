package com.clinica.mi_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class OrganizacionRequest {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 20)
    private String plan;

    private OffsetDateTime trialHasta;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public OffsetDateTime getTrialHasta() { return trialHasta; }
    public void setTrialHasta(OffsetDateTime trialHasta) { this.trialHasta = trialHasta; }
}
