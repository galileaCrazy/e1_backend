package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CitaResponse {

    private UUID id;
    private UUID organizacionId;
    private UUID pacienteId;
    private UUID medicoId;
    private UUID consultorioId;
    private OffsetDateTime fechaHora;
    private Short duracionMin;
    private String estado;
    private String motivo;
    private OffsetDateTime canceladaEn;
    private String canceladaPor;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public UUID getPacienteId() { return pacienteId; }
    public void setPacienteId(UUID pacienteId) { this.pacienteId = pacienteId; }

    public UUID getMedicoId() { return medicoId; }
    public void setMedicoId(UUID medicoId) { this.medicoId = medicoId; }

    public UUID getConsultorioId() { return consultorioId; }
    public void setConsultorioId(UUID consultorioId) { this.consultorioId = consultorioId; }

    public OffsetDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Short getDuracionMin() { return duracionMin; }
    public void setDuracionMin(Short duracionMin) { this.duracionMin = duracionMin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public OffsetDateTime getCanceladaEn() { return canceladaEn; }
    public void setCanceladaEn(OffsetDateTime canceladaEn) { this.canceladaEn = canceladaEn; }

    public String getCanceladaPor() { return canceladaPor; }
    public void setCanceladaPor(String canceladaPor) { this.canceladaPor = canceladaPor; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
