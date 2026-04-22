package com.clinica.mi_app.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ConsultorioResponse {

    private UUID id;
    private UUID organizacionId;
    private String nombre;
    private String direccion;
    private String telefono;
    private Boolean activo;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
