package com.clinica.mi_app.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 1)
    private String sexo; // M | F | O

    @Column(length = 180)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ---------- Getters y Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Organizacion getOrganizacion() { return organizacion; }
    public void setOrganizacion(Organizacion organizacion) { this.organizacion = organizacion; }

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