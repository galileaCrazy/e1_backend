import React from "react";
import { CalendarPlus, ChevronLeft, ChevronRight, RefreshCw, Save, X } from "lucide-react";
import { Empty, ErrorBox, Loading, Metric, Section, ViewHeader } from "../../../components/ui";
import { formatDate, formatTime, nameById, statusVariant } from "../../../lib/display";
import "./MedicoAgenda.css";

const STATUS_OPTIONS = ["", "SIN_CONFIRMAR", "CONFIRMADA", "CANCELADA", "REAGENDADA", "NO_ASISTIO"];

export default function MedicoAgenda({ context }) {
  const [data, setData] = React.useState(null);
  const [error, setError] = React.useState("");
  const [selectedDate, setSelectedDate] = React.useState(todayInput());
  const [statusFilter, setStatusFilter] = React.useState("");
  const [creating, setCreating] = React.useState(false);

  const loadAgenda = React.useCallback(async () => {
    if (!context.medicoId) {
      setData(null);
      setError("Tu usuario medico no tiene un medico asociado.");
      return;
    }

    try {
      setError("");
      const [citas, pacientes, consultorios] = await Promise.all([
        context.list(`/api/citas/medico/${context.medicoId}`, true),
        context.list(`/api/pacientes/organizacion/${context.orgId}`, true),
        context.list(`/api/consultorios/organizacion/${context.orgId}`, true),
      ]);
      setData({ citas: sortAppointments(citas), pacientes, consultorios });
    } catch (err) {
      setError(err.message);
    }
  }, [context]);

  React.useEffect(() => {
    loadAgenda();
  }, [loadAgenda]);

  async function createAppointment(payload) {
    try {
      await context.api("/api/citas", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      context.notify("Cita creada");
      setCreating(false);
      await loadAgenda();
    } catch (err) {
      context.notify(err.message, "error");
    }
  }

  if (error) {
    return (
      <>
        <ViewHeader title="Mi agenda" />
        <ErrorBox message={error} />
      </>
    );
  }
  if (!data) return <Loading label="Cargando agenda..." />;

  const support = { pacientes: data.pacientes, consultorios: data.consultorios };
  const citasDelDia = data.citas.filter((cita) => sameDate(cita.fechaHora, selectedDate));
  const visibles = statusFilter ? citasDelDia.filter((cita) => cita.estado === statusFilter) : citasDelDia;
  const proximas = data.citas.filter((cita) => new Date(cita.fechaHora).getTime() >= Date.now()).slice(0, 6);
  const confirmadas = citasDelDia.filter((cita) => cita.estado === "CONFIRMADA").length;
  const pendientes = citasDelDia.filter((cita) => cita.estado === "SIN_CONFIRMAR").length;
  const minutos = citasDelDia.reduce((total, cita) => total + Number(cita.duracionMin || 0), 0);

  return (
    <>
      <ViewHeader title="Mi agenda">
        <button className="btn" onClick={loadAgenda}>
          <RefreshCw size={16} /> Actualizar
        </button>
        <button className="btn primary" onClick={() => setCreating(true)}>
          <CalendarPlus size={16} /> Nueva cita
        </button>
      </ViewHeader>

      <section className="metrics">
        <Metric label="Citas del dia" value={citasDelDia.length} />
        <Metric label="Confirmadas" value={confirmadas} variant="secondary" />
        <Metric label="Sin confirmar" value={pendientes} variant="warning" />
        <Metric label="Minutos agendados" value={minutos} variant="danger" />
      </section>

      <section className="section agenda-controls">
        <button className="btn icon" onClick={() => setSelectedDate(shiftDate(selectedDate, -1))} title="Dia anterior">
          <ChevronLeft size={18} />
        </button>
        <label className="field">
          <span>Dia</span>
          <input type="date" value={selectedDate} onChange={(event) => setSelectedDate(event.target.value)} />
        </label>
        <button className="btn icon" onClick={() => setSelectedDate(shiftDate(selectedDate, 1))} title="Dia siguiente">
          <ChevronRight size={18} />
        </button>
        <label className="field">
          <span>Estado</span>
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
            <option value="">Todos</option>
            {STATUS_OPTIONS.filter(Boolean).map((estado) => (
              <option key={estado} value={estado}>{estado}</option>
            ))}
          </select>
        </label>
      </section>

      <div className="agenda-grid">
        <section>
          <Section title="Dia seleccionado" badge={formatAgendaDate(selectedDate)}>
            <AgendaTimeline citas={visibles} support={support} />
          </Section>
        </section>

        <aside>
          {creating && (
            <Section title="Nueva cita">
              <AgendaForm
                context={context}
                pacientes={data.pacientes}
                consultorios={data.consultorios}
                selectedDate={selectedDate}
                onCancel={() => setCreating(false)}
                onSubmit={createAppointment}
              />
            </Section>
          )}
          <Section title="Proximas citas">
            <UpcomingList citas={proximas} support={support} />
          </Section>
        </aside>
      </div>
    </>
  );
}

function AgendaTimeline({ citas, support }) {
  if (!citas.length) return <Empty label="No hay citas para este dia." />;
  return (
    <div className="agenda-timeline">
      {citas.map((cita) => (
        <article className="agenda-item" key={cita.id}>
          <div className="agenda-time">
            <strong>{formatTime(cita.fechaHora)}</strong>
            <span>{cita.duracionMin} min</span>
          </div>
          <div className="agenda-detail">
            <strong>{nameById("pacientes", cita.pacienteId, support)}</strong>
            <span>{cita.motivo || "Consulta medica"}</span>
            <small>{nameById("consultorios", cita.consultorioId, support)}</small>
          </div>
          <span className={`badge ${statusVariant(cita.estado)}`}>{cita.estado}</span>
        </article>
      ))}
    </div>
  );
}

function UpcomingList({ citas, support }) {
  if (!citas.length) return <Empty label="No hay citas proximas." />;
  return (
    <div className="agenda-upcoming">
      {citas.map((cita) => (
        <article className="upcoming-item" key={cita.id}>
          <div>
            <strong>{nameById("pacientes", cita.pacienteId, support)}</strong>
            <span>{formatDate(cita.fechaHora)}</span>
          </div>
          <span className={`badge ${statusVariant(cita.estado)}`}>{cita.estado}</span>
        </article>
      ))}
    </div>
  );
}

function AgendaForm({ context, pacientes, consultorios, selectedDate, onCancel, onSubmit }) {
  const canCreate = pacientes.length > 0 && consultorios.length > 0;

  return (
    <form
      className="agenda-form"
      onSubmit={(event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        onSubmit({
          organizacionId: context.orgId,
          medicoId: context.medicoId,
          pacienteId: data.get("pacienteId"),
          consultorioId: data.get("consultorioId"),
          fechaHora: new Date(`${data.get("fecha")}T${data.get("hora")}`).toISOString(),
          duracionMin: Number(data.get("duracionMin")),
          motivo: data.get("motivo") || null,
        });
      }}
    >
      {!canCreate && <ErrorBox message="Necesitas al menos un paciente y un consultorio para crear una cita." />}
      <label className="field full">
        <span>Paciente</span>
        <select name="pacienteId" required disabled={!pacientes.length}>
          {pacientes.map((paciente) => (
            <option key={paciente.id} value={paciente.id}>{paciente.nombre}</option>
          ))}
        </select>
      </label>
      <label className="field full">
        <span>Consultorio</span>
        <select name="consultorioId" required disabled={!consultorios.length}>
          {consultorios.map((consultorio) => (
            <option key={consultorio.id} value={consultorio.id}>{consultorio.nombre}</option>
          ))}
        </select>
      </label>
      <label className="field">
        <span>Fecha</span>
        <input name="fecha" type="date" defaultValue={selectedDate} required />
      </label>
      <label className="field">
        <span>Hora</span>
        <input name="hora" type="time" defaultValue="09:00" required />
      </label>
      <label className="field">
        <span>Duracion</span>
        <select name="duracionMin" defaultValue="30" required>
          <option value="20">20 min</option>
          <option value="30">30 min</option>
          <option value="45">45 min</option>
          <option value="60">60 min</option>
        </select>
      </label>
      <label className="field full">
        <span>Motivo</span>
        <textarea name="motivo" rows="4" />
      </label>
      <div className="actions full">
        <button className="btn" type="button" onClick={onCancel}>
          <X size={16} /> Cancelar
        </button>
        <button className="btn primary" disabled={!canCreate}>
          <Save size={16} /> Guardar
        </button>
      </div>
    </form>
  );
}

function sortAppointments(citas) {
  return [...citas].sort((a, b) => new Date(a.fechaHora) - new Date(b.fechaHora));
}

function todayInput() {
  return toDateInput(new Date());
}

function toDateInput(value) {
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

function sameDate(value, selectedDate) {
  return toDateInput(value) === selectedDate;
}

function shiftDate(value, days) {
  const date = new Date(`${value}T12:00:00`);
  date.setDate(date.getDate() + days);
  return toDateInput(date);
}

function formatAgendaDate(value) {
  const date = new Date(`${value}T12:00:00`);
  return date.toLocaleDateString("es-MX", { weekday: "long", day: "numeric", month: "long" });
}
