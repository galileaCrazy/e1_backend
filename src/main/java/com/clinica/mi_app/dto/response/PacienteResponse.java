package com.clinica.mi_app.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PacienteResponse {

    private UUID id;
    private UUID organizacionId;
    private String nombre;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String email;
    private String notas;
    private Boolean activo;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
