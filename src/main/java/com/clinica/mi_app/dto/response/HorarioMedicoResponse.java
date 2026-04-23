package com.clinica.mi_app.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public class HorarioMedicoResponse {

    private UUID id;
    private UUID medicoId;
    private Short diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Short duracionConsulta;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMedicoId() { return medicoId; }
    public void setMedicoId(UUID medicoId) { this.medicoId = medicoId; }

    public Short getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Short diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Short getDuracionConsulta() { return duracionConsulta; }
    public void setDuracionConsulta(Short duracionConsulta) { this.duracionConsulta = duracionConsulta; }
}
