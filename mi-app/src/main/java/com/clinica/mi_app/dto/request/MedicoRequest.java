package com.clinica.mi_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public class MedicoRequest {

    @NotNull
    private UUID organizacionId;

    @NotNull
    private UUID consultorioId;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 80)
    private String especialidad;

    @Size(max = 30)
    private String cedula;

    @Size(max = 20)
    private String telefono;

    private BigDecimal tarifaBase;

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
}
