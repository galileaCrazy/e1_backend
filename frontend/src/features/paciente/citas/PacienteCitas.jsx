import React from "react";
import { CalendarDays, CalendarPlus } from "lucide-react";
import { Empty, ErrorBox, Loading, Section, ViewHeader } from "../../../components/ui";
import { formatDate, formatTime, isFuture, shortId, statusVariant } from "../../../lib/display";
import "./PacienteCitas.css";

export default function PacienteCitas({ context }) {
  const [rows, setRows] = React.useState(null);
  const [medicos, setMedicos] = React.useState([]);
  const [filter, setFilter] = React.useState("todas");
  const [error, setError] = React.useState("");
  const [submitting, setSubmitting] = React.useState(false);

  React.useEffect(() => {
    let active = true;
    async function load() {
      if (!context.pacienteId) {
        setError("Tu usuario paciente no tiene un expediente vinculado.");
        setRows([]);
        return;
      }
      try {
        setError("");
        const [citas, activeMedicos] = await Promise.all([
          context.list(`/api/citas/paciente/${context.pacienteId}`),
          context.list(`/api/medicos/organizacion/${context.orgId}/activos`, true),
        ]);
        if (active) {
          setRows(sortAppointments(citas));
          setMedicos(activeMedicos);
        }
      } catch (err) {
        if (active) setError(err.message);
      }
    }
    load();
    return () => {
      active = false;
    };
  }, [context]);

  async function scheduleAppointment(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const medicoId = String(form.get("medicoId") || "");
    const medico = medicos.find((item) => String(item.id) === medicoId);
    if (!medico) {
      context.notify("Selecciona un medico disponible", "error");
      return;
    }

    setSubmitting(true);
    try {
      const created = await context.api("/api/citas", {
        method: "POST",
        body: JSON.stringify({
          organizacionId: context.orgId,
          pacienteId: context.pacienteId,
          medicoId,
          consultorioId: medico.consultorioId,
          fechaHora: new Date(form.get("fechaHora")).toISOString(),
          duracionMin: Number(form.get("duracionMin")),
          motivo: form.get("motivo"),
        }),
      });
      setRows((items) => sortAppointments([created, ...(items || [])]));
      event.currentTarget.reset();
      context.notify("Cita solicitada. Queda pendiente de confirmacion.");
    } catch (err) {
      context.notify(err.message, "error");
    } finally {
      setSubmitting(false);
    }
  }

  if (error) {
    return (
      <>
        <ViewHeader title="Mis citas" />
        <ErrorBox message={error} />
      </>
    );
  }
  if (!rows) return <Loading label="Cargando tus citas..." />;

  const visibleRows = rows.filter((cita) => {
    if (filter === "proximas") return isFuture(cita.fechaHora) && !["CANCELADA", "NO_ASISTIO"].includes(cita.estado);
    if (filter === "pasadas") return !isFuture(cita.fechaHora);
    return true;
  });

  return (
    <>
      <ViewHeader title="Mis citas" subtitle="Consulta tu agenda y el historial de atenciones." />
      <Section title="Agendar cita" badge="Solicitud">
        <form className="patient-schedule-form" onSubmit={scheduleAppointment}>
          <label className="field">
            <span>Medico</span>
            <select name="medicoId" required disabled={!medicos.length || submitting}>
              <option value="">Selecciona...</option>
              {medicos.map((medico) => (
                <option key={medico.id} value={medico.id}>
                  {medico.nombre}{medico.especialidad ? ` / ${medico.especialidad}` : ""}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Fecha y hora</span>
            <input name="fechaHora" type="datetime-local" min={minDatetime()} required disabled={submitting} />
          </label>
          <label className="field">
            <span>Duracion</span>
            <select name="duracionMin" defaultValue="30" required disabled={submitting}>
              <option value="20">20 min</option>
              <option value="30">30 min</option>
              <option value="45">45 min</option>
              <option value="60">60 min</option>
            </select>
          </label>
          <label className="field full">
            <span>Motivo</span>
            <textarea name="motivo" placeholder="Describe brevemente el motivo de tu consulta" disabled={submitting} />
          </label>
          <div className="actions full">
            <button className="btn primary" disabled={!medicos.length || submitting}>
              <CalendarPlus size={16} /> {submitting ? "Solicitando..." : "Solicitar cita"}
            </button>
          </div>
        </form>
      </Section>
      <section className="patient-tabs" aria-label="Filtros de citas">
        <button className={filter === "todas" ? "active" : ""} onClick={() => setFilter("todas")}>Todas</button>
        <button className={filter === "proximas" ? "active" : ""} onClick={() => setFilter("proximas")}>Proximas</button>
        <button className={filter === "pasadas" ? "active" : ""} onClick={() => setFilter("pasadas")}>Pasadas</button>
      </section>
      <Section title={`${visibleRows.length} citas`}>
        {visibleRows.length ? (
          <div className="patient-appointment-list">
            {visibleRows.map((cita) => (
              <article className="patient-appointment-card" key={cita.id}>
                <div className="patient-appointment-date">
                  <CalendarDays size={20} />
                  <strong>{formatTime(cita.fechaHora)}</strong>
                  <span>{formatDate(cita.fechaHora)}</span>
                </div>
                <div className="patient-appointment-info">
                  <strong>{cita.motivo || "Consulta medica"}</strong>
                  <span>Medico {shortId(cita.medicoId)}</span>
                  <small>{cita.duracionMin || 0} min</small>
                </div>
                <span className={`badge ${statusVariant(cita.estado)}`}>{cita.estado}</span>
              </article>
            ))}
          </div>
        ) : (
          <Empty label="No hay citas para este filtro." />
        )}
      </Section>
    </>
  );
}

function sortAppointments(citas) {
  return [...citas].sort((a, b) => new Date(b.fechaHora) - new Date(a.fechaHora));
}

function minDatetime() {
  const date = new Date(Date.now() + 30 * 60 * 1000);
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60 * 1000);
  return local.toISOString().slice(0, 16);
}
