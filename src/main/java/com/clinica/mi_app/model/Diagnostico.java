package com.clinica.mi_app.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagnostico")
public class Diagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(name = "codigo_cie10", length = 10)
    private String codigoCie10;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 20)
    private String tipo; // PRINCIPAL | SECUNDARIO

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ---------- Getters y Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public String getCodigoCie10() { return codigoCie10; }
    public void setCodigoCie10(String codigoCie10) { this.codigoCie10 = codigoCie10; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}