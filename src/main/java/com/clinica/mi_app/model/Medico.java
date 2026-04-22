package com.clinica.mi_app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id", nullable = false)
    private Consultorio consultorio;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 80)
    private String especialidad;

    @Column(length = 30)
    private String cedula;

    @Column(length = 20)
    private String telefono;

    @Column(name = "tarifa_base", precision = 10, scale = 2)
    private BigDecimal tarifaBase;

    @Column(nullable = false)
    private Boolean activo = true;

    // ---------- Getters y Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Organizacion getOrganizacion() { return organizacion; }
    public void setOrganizacion(Organizacion organizacion) { this.organizacion = organizacion; }

    public Consultorio getConsultorio() { return consultorio; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }

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