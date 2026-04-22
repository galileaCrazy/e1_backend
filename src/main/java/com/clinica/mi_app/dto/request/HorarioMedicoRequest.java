package com.clinica.mi_app.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public class HorarioMedicoRequest {

    @NotNull
    private UUID medicoId;

    @NotNull
    @Min(0)
    @Max(6)
    private Short diaSemana;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    @NotNull
    private Short duracionConsulta;

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
