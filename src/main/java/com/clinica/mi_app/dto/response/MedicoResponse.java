package com.clinica.mi_app.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class MedicoResponse {

    private UUID id;
    private UUID organizacionId;
    private UUID consultorioId;
    private String nombre;
    private String especialidad;
    private String cedula;
    private String telefono;
    private BigDecimal tarifaBase;
    private Boolean activo;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public UUID getConsultorioId() { return consultorioId; }
    public void setConsultorioId(UUID consultorioId) { this.consultorioId = consultorioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public BigDecimal getTarifaBase() { return tarifaBase; }
    public void setTarifaBase(BigDecimal tarifaBase) { this.tarifaBase = tarifaBase; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
