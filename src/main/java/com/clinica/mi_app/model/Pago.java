package com.clinica.mi_app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private String metodo; // EFECTIVO | TRANSFERENCIA | TARJETA

    @Column(length = 200)
    private String concepto;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE"; // PAGADO | PENDIENTE

    @Column(length = 100)
    private String referencia;

    @Column(name = "pagado_en")
    private OffsetDateTime pagadoEn;

    // ---------- Getters y Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Organizacion getOrganizacion() { return organizacion; }
    public void setOrganizacion(Organizacion organizacion) { this.organizacion = organizacion; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public OffsetDateTime getPagadoEn() { return pagadoEn; }
    public void setPagadoEn(OffsetDateTime pagadoEn) { this.pagadoEn = pagadoEn; }
}