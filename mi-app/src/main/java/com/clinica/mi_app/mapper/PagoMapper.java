package com.clinica.mi_app.mapper;

import com.clinica.mi_app.dto.response.PagoResponse;
import com.clinica.mi_app.model.Pago;

public class PagoMapper {

    public static PagoResponse toResponse(Pago p) {
        PagoResponse r = new PagoResponse();
        r.setId(p.getId());
        r.setOrganizacionId(p.getOrganizacion().getId());
        r.setCitaId(p.getCita().getId());
        r.setMonto(p.getMonto());
        r.setMetodo(p.getMetodo());
        r.setConcepto(p.getConcepto());
        r.setEstado(p.getEstado());
        r.setReferencia(p.getReferencia());
        r.setPagadoEn(p.getPagadoEn());
        return r;
    }
}
