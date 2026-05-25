package com.clinica.mi_app.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class MedicoPublicoResponse {

    private UUID id;
    private String nombre;
    private String especialidad;
    private BigDecimal tarifaBase;
    private UUID consultorioId;
    private String consultorioNombre;
    private String consultorioDireccion;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public BigDecimal getTarifaBase() { return tarifaBase; }
    public void setTarifaBase(BigDecimal tarifaBase) { this.tarifaBase = tarifaBase; }

    public UUID getConsultorioId() { return consultorioId; }
    public void setConsultorioId(UUID consultorioId) { this.consultorioId = consultorioId; }

    public String getConsultorioNombre() { return consultorioNombre; }
    public void setConsultorioNombre(String consultorioNombre) { this.consultorioNombre = consultorioNombre; }

    public String getConsultorioDireccion() { return consultorioDireccion; }
    public void setConsultorioDireccion(String consultorioDireccion) { this.consultorioDireccion = consultorioDireccion; }
}
