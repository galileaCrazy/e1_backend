import React from "react";
import {
  AlertCircle,
  Building2,
  CalendarCheck,
  CalendarDays,
  CalendarX,
  CheckCircle2,
  Clock,
  Hourglass,
  MapPin,
  Stethoscope,
} from "lucide-react";
import { formatCitaHora12, UTC } from "../../lib/display";
import "./CitaPublica.css";

/**
 * Página pública /c/{token} (canal "enlace"): el paciente ve su cita y la confirma o cancela sin
 * iniciar sesión. Usa rutas relativas (/api/public/...) para funcionar por el proxy de Vite tanto
 * en Wi-Fi (http://<ip>:5173) como por el túnel, sin depender de VITE_API_URL.
 */
export default function CitaPublica({ token }) {
  const [cita, setCita] = React.useState(null);
  const [error, setError] = React.useState(null);
  const [cargando, setCargando] = React.useState(true);
  const [enviando, setEnviando] = React.useState(false);
  const [preguntarCancelar, setPreguntarCancelar] = React.useState(false);
  const [resultado, setResultado] = React.useState(null);

  React.useEffect(() => {
    document.title = "Tu cita · MedInFlow";
    let activo = true;
    llamar("GET", token)
      .then((data) => activo && setCita(data))
      .catch((err) => activo && setError(err))
      .finally(() => activo && setCargando(false));
    return () => {
      activo = false;
    };
  }, [token]);

  async function responder(accion) {
    setEnviando(true);
    try {
      const data = await llamar("POST", token, { accion });
      setCita(data);
      setResultado(data.resultado || (accion === "CONFIRMAR" ? "CONFIRMADA" : "CANCELADA"));
    } catch (err) {
      setError(err);
    } finally {
      setEnviando(false);
      setPreguntarCancelar(false);
    }
  }

  return (
    <div className="cp-pagina">
      <header className="cp-marca">
        <span className="cp-logo" aria-hidden="true">M</span>
        <span>MedInFlow</span>
      </header>

      <main className="cp-tarjeta" aria-live="polite">
        {cargando && (
          <div className="cp-estado">
            <Hourglass size={34} className="cp-icono muted" />
            <p>Cargando tu cita...</p>
          </div>
        )}

        {!cargando && error && <EstadoError error={error} />}

        {!cargando && !error && resultado && <EstadoResultado resultado={resultado} cita={cita} />}

        {!cargando && !error && !resultado && cita && (
          <>
            <h1>{cita.pacienteNombre ? `Hola, ${cita.pacienteNombre}` : "Hola"}</h1>
            <p className="cp-intro">Te recordamos tu próxima cita. ¿Nos confirmas si vas a asistir?</p>
            <DetalleCita cita={cita} />

            {cita.estado === "CONFIRMADA" && (
              <p className="cp-aviso ok">
                <CheckCircle2 size={16} /> Tu cita ya está confirmada.
              </p>
            )}

            {!preguntarCancelar ? (
              <div className="cp-acciones">
                {cita.estado !== "CONFIRMADA" && (
                  <button type="button" className="cp-btn primario" disabled={enviando} onClick={() => responder("CONFIRMAR")}>
                    <CalendarCheck size={18} />
                    {enviando ? "Enviando..." : "Confirmar asistencia"}
                  </button>
                )}
                <button type="button" className="cp-btn peligro" disabled={enviando} onClick={() => setPreguntarCancelar(true)}>
                  <CalendarX size={18} />
                  Cancelar cita
                </button>
              </div>
            ) : (
              <div className="cp-confirmar" role="group" aria-label="Confirmar cancelación">
                <p>¿Seguro que quieres cancelar tu cita? Este enlace solo se puede usar una vez.</p>
                <div className="cp-acciones">
                  <button type="button" className="cp-btn peligro" disabled={enviando} onClick={() => responder("CANCELAR")}>
                    {enviando ? "Cancelando..." : "Sí, cancelar mi cita"}
                  </button>
                  <button type="button" className="cp-btn" disabled={enviando} onClick={() => setPreguntarCancelar(false)}>
                    No, volver
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </main>

      <footer className="cp-pie">Este enlace es personal. No lo compartas.</footer>
    </div>
  );
}

function DetalleCita({ cita }) {
  return (
    <dl className="cp-detalle">
      {cita.clinica && (
        <Fila icono={Building2} etiqueta="Clínica" valor={cita.clinica} />
      )}
      <Fila
        icono={Stethoscope}
        etiqueta="Médico"
        valor={[cita.medico, cita.especialidad].filter(Boolean).join(" · ") || "Tu médico"}
      />
      <Fila icono={CalendarDays} etiqueta="Fecha" valor={fechaLarga(cita.fechaHora)} />
      <Fila icono={Clock} etiqueta="Hora" valor={formatCitaHora12(cita.fechaHora)} />
      {(cita.consultorio || cita.direccion) && (
        <Fila icono={MapPin} etiqueta="Lugar" valor={[cita.consultorio, cita.direccion].filter(Boolean).join(" · ")} />
      )}
    </dl>
  );
}

function Fila({ icono: Icono, etiqueta, valor }) {
  return (
    <div className="cp-fila">
      <Icono size={18} aria-hidden="true" />
      <dt>{etiqueta}</dt>
      <dd>{valor}</dd>
    </div>
  );
}

function EstadoResultado({ resultado, cita }) {
  const confirmada = resultado === "CONFIRMADA";
  return (
    <div className="cp-estado">
      {confirmada ? <CheckCircle2 size={44} className="cp-icono ok" /> : <CalendarX size={44} className="cp-icono peligro" />}
      <h1>{confirmada ? "¡Gracias! Tu cita quedó confirmada" : "Tu cita fue cancelada"}</h1>
      <p>
        {confirmada
          ? "Te esperamos. Si surge algún cambio, comunícate con la clínica."
          : "Avisamos a la clínica. Si deseas reagendar, comunícate con ellos."}
      </p>
      {cita && <DetalleCita cita={cita} />}
    </div>
  );
}

function EstadoError({ error }) {
  const usado = error.codigo === "USADO";
  return (
    <div className="cp-estado">
      {usado ? <CheckCircle2 size={44} className="cp-icono muted" /> : <AlertCircle size={44} className="cp-icono aviso" />}
      <h1>{TITULOS[error.codigo] || "No pudimos abrir tu cita"}</h1>
      <p>{error.mensaje}</p>
    </div>
  );
}

const TITULOS = {
  NO_ENCONTRADO: "Enlace no válido",
  USADO: "Ya recibimos tu respuesta",
  REEMPLAZADO: "Este enlace ya no está activo",
  EXPIRADO: "Este enlace expiró",
  NO_DISPONIBLE: "Esta cita ya no se puede modificar",
  DEMASIADOS_INTENTOS: "Demasiados intentos",
  SIN_CONEXION: "Sin conexión",
};

async function llamar(method, token, body) {
  let response;
  try {
    response = await fetch(`/api/public/citas/${encodeURIComponent(token)}`, {
      method,
      headers: body ? { "Content-Type": "application/json" } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw { codigo: "SIN_CONEXION", mensaje: "No pudimos conectar con la clínica. Revisa tu conexión e intenta de nuevo." };
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    throw {
      codigo: data?.codigo || "ERROR",
      mensaje: data?.mensaje || "Ocurrió un problema. Intenta de nuevo en unos minutos o comunícate con la clínica.",
    };
  }
  return data;
}

/** "martes 19 de noviembre de 2026" — parte UTC, convención de reloj de las citas. */
function fechaLarga(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleDateString("es-MX", { weekday: "long", day: "numeric", month: "long", year: "numeric", timeZone: UTC });
}
