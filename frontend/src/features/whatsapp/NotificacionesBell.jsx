import React from "react";
import { Bell } from "lucide-react";
import { formatCitaFecha, formatCitaHora12, formatInstant } from "../../lib/display";
import "./WhatsApp.css";

const REFRESCO_MS = 30000;

/** Campana con las cancelaciones del paciente por WhatsApp (CANCELACION_PACIENTE) y contador de no leídas. */
export default function NotificacionesBell({ context }) {
  const [datos, setDatos] = React.useState({ noLeidas: 0, items: [] });
  const [abierto, setAbierto] = React.useState(false);
  const ref = React.useRef(null);

  const cargar = React.useCallback(async () => {
    try {
      setDatos((await context.api("/api/notificaciones/recientes")) || { noLeidas: 0, items: [] });
    } catch {
      // silencioso: la campana no debe romper el layout
    }
  }, [context]);

  React.useEffect(() => {
    cargar();
    const id = window.setInterval(cargar, REFRESCO_MS);
    return () => window.clearInterval(id);
  }, [cargar]);

  React.useEffect(() => {
    if (!abierto) return undefined;
    const cerrar = (event) => {
      if (ref.current && !ref.current.contains(event.target)) setAbierto(false);
    };
    const escape = (event) => event.key === "Escape" && setAbierto(false);
    document.addEventListener("mousedown", cerrar);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("mousedown", cerrar);
      document.removeEventListener("keydown", escape);
    };
  }, [abierto]);

  async function marcarLeida(item) {
    if (item.leida) return;
    try {
      await context.api(`/api/notificaciones/${item.id}/leida`, { method: "PATCH" });
      await cargar();
    } catch (err) {
      context.notify(err.message, "error");
    }
  }

  async function marcarTodas() {
    try {
      await context.api("/api/notificaciones/leidas", { method: "POST" });
      await cargar();
    } catch (err) {
      context.notify(err.message, "error");
    }
  }

  return (
    <div className="bell" ref={ref}>
      <button
        type="button"
        className="btn icon ghost bell-btn"
        aria-label={`Notificaciones${datos.noLeidas ? `, ${datos.noLeidas} sin leer` : ""}`}
        aria-expanded={abierto}
        onClick={() => setAbierto((v) => !v)}
      >
        <Bell size={19} />
        {datos.noLeidas > 0 && <span className="bell-contador">{datos.noLeidas > 9 ? "9+" : datos.noLeidas}</span>}
      </button>
      {abierto && (
        <div className="bell-panel" role="dialog" aria-label="Cancelaciones recientes">
          <div className="bell-head">
            <strong>Cancelaciones por WhatsApp</strong>
            {datos.noLeidas > 0 && (
              <button type="button" className="btn small ghost" onClick={marcarTodas}>Marcar todas</button>
            )}
          </div>
          {datos.items.length === 0 ? (
            <p className="bell-vacio">Sin cancelaciones recientes.</p>
          ) : (
            <ul className="bell-lista">
              {datos.items.map((item) => (
                <li key={item.id}>
                  <button type="button" className={`bell-item ${item.leida ? "" : "nueva"}`} onClick={() => marcarLeida(item)}>
                    <strong>{item.pacienteNombre || "Paciente"} canceló su cita</strong>
                    <span>
                      {item.citaFechaHora ? `${formatCitaFecha(item.citaFechaHora)} · ${formatCitaHora12(item.citaFechaHora)}` : ""}
                      {item.medicoNombre ? ` · ${item.medicoNombre}` : ""}
                    </span>
                    <small>{formatInstant(item.creadaEn)}</small>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
