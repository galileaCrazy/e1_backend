package com.clinica.mi_app.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "horario_medico")
public class HorarioMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana; // 0=lunes ... 6=domingo

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "duracion_consulta", nullable = false)
    private Short duracionConsulta; // 20 | 30 | 45 | 60 min

    // ---------- Getters y Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Short getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Short diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Short getDuracionConsulta() { return duracionConsulta; }
    public void setDuracionConsulta(Short duracionConsulta) { this.duracionConsulta = duracionConsulta; }
}