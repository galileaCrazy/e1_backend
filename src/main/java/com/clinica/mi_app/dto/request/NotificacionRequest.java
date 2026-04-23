package com.clinica.mi_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class NotificacionRequest {

    @NotNull
    private UUID organizacionId;

    @NotNull
    private UUID citaId;

    private UUID adjuntoId;

    @NotBlank
    @Size(max = 20)
    private String canal;

    @NotBlank
    @Size(max = 40)
    private String tipo;

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public UUID getCitaId() { return citaId; }
    public void setCitaId(UUID citaId) { this.citaId = citaId; }

    public UUID getAdjuntoId() { return adjuntoId; }
    public void setAdjuntoId(UUID adjuntoId) { this.adjuntoId = adjuntoId; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
