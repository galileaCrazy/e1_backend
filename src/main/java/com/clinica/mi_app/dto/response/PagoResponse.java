package com.clinica.mi_app.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PagoResponse {

    private UUID id;
    private UUID organizacionId;
    private UUID citaId;
    private BigDecimal monto;
    private String metodo;
    private String concepto;
    private String estado;
    private String referencia;
    private OffsetDateTime pagadoEn;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public UUID getCitaId() { return citaId; }
    public void setCitaId(UUID citaId) { this.citaId = citaId; }

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
