package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DiagnosticoResponse {

    private UUID id;
    private UUID citaId;
    private String codigoCie10;
    private String descripcion;
    private String tipo;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCitaId() { return citaId; }
    public void setCitaId(UUID citaId) { this.citaId = citaId; }

    public String getCodigoCie10() { return codigoCie10; }
    public void setCodigoCie10(String codigoCie10) { this.codigoCie10 = codigoCie10; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
