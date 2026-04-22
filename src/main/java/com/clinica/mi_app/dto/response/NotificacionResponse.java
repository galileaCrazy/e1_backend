package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NotificacionResponse {

    private UUID id;
    private UUID organizacionId;
    private UUID citaId;
    private UUID adjuntoId;
    private String canal;
    private String tipo;
    private String estado;
    private OffsetDateTime enviadaEn;
    private String respuesta;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public OffsetDateTime getEnviadaEn() { return enviadaEn; }
    public void setEnviadaEn(OffsetDateTime enviadaEn) { this.enviadaEn = enviadaEn; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
}
