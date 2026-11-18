import React from "react";
import {
  AlertTriangle,
  BellRing,
  CalendarCheck,
  CalendarClock,
  CalendarDays,
  CalendarX,
  CheckCheck,
  CheckCircle2,
  Inbox,
  Link2,
  MessageCircle,
  RefreshCw,
  Send,
  X,
  XCircle,
} from "lucide-react";
import { ErrorBox, ViewHeader } from "../../components/ui";
import { CLINIC_TIME_ZONE, UTC, citaDateKey, clinicToday, formatCitaHora12 } from "../../lib/display";
import { RecordatorioWhatsApp, useCanalWhatsApp, useRecordatorioEstados } from "../whatsapp/RecordatorioWhatsApp";
import "./Actividad.css";

const REFRESCO_MS = 30000;
/** Evento compartido con la campana: "algo cambió en las notificaciones" (leídas o nuevas). */
export const EVENTO_NOTIFICACIONES = "medinflow:notificaciones";

const FILTROS = [
  { key: "TODAS", label: "Todas", conteo: "todas" },
  { key: "SIN_LEER", label: "Sin leer", conteo: "sinLeer" },
  { key: "CONFIRMACIONES", label: "Confirmaciones", conteo: "confirmaciones" },
  { key: "CANCELACIONES", label: "Cancelaciones", conteo: "cancelaciones" },
  { key: "RECORDATORIOS", label: "Recordatorios", conteo: "recordatorios" },
];

const VACIOS = {
  TODAS: ["Aún no hay actividad", "Cuando envíes recordatorios o un paciente responda, lo verás aquí."],
  SIN_LEER: ["Todo al día", "No tienes avisos sin leer."],
  CONFIRMACIONES: ["Sin confirmaciones", "Ningún paciente ha confirmado en los últimos 30 días."],
  CANCELACIONES: ["Sin cancelaciones", "Ningún paciente canceló en los últimos 30 días. 🎉"],
  RECORDATORIOS: ["Sin recordatorios", "Aún no se han enviado recordatorios en los últimos 30 días."],
};

export function avisarCambioNotificaciones() {
  window.dispatchEvent(new CustomEvent(EVENTO_NOTIFICACIONES));
}

/**
 * "Actividad" (antes "Notificaciones"): resumen del próximo día con citas, envío de recordatorios
 * y lista de respuestas/recordatorios agrupada por día. MEDICO ve sus citas; ADMIN toda la clínica.
 */
export default function ActividadView({ context }) {
  const esAdmin = context.role === "ADMIN";
  const [filtro, setFiltro] = React.useState("TODAS");
  const [medicoId, setMedicoId] = React.useState("");
  const [medicos, setMedicos] = React.useState([]);
  const [datos, setDatos] = React.useState(null);
  const [manana, setManana] = React.useState(null);
  const [error, setError] = React.useState("");
  const [cargando, setCargando] = React.useState(true);
  const [tick, setTick] = React.useState(0);
  const [lote, setLote] = React.useState(false);

  const recargar = React.useCallback(() => setTick((t) => t + 1), []);
  const consulta = medicoId ? `medicoId=${encodeURIComponent(medicoId)}` : "";

  React.useEffect(() => {
    if (!esAdmin || !context.orgId) return;
    context.api(`/api/medicos/organizacion/${context.orgId}`)
      .then((data) => setMedicos((data || []).filter((m) => m.activo !== false)))
      .catch(() => setMedicos([]));
  }, [context, esAdmin]);

  React.useEffect(() => {
    let activo = true;
    setCargando(true);
    const params = [filtro !== "TODAS" ? `filtro=${filtro}` : "", consulta].filter(Boolean).join("&");
    Promise.all([
      context.api(`/api/notificaciones/actividad${params ? `?${params}` : ""}`),
      context.api(`/api/notificaciones/actividad/manana${consulta ? `?${consulta}` : ""}`),
    ])
      .then(([actividad, proximo]) => {
        if (!activo) return;
        setDatos(actividad || { items: [], conteos: {} });
        setManana(proximo);
        setError("");
      })
      .catch((err) => activo && setError(err.message))
      .finally(() => activo && setCargando(false));
    return () => {
      activo = false;
    };
  }, [context, filtro, consulta, tick]);

  // Refresco periódico, al volver a la pestaña (p. ej. después de enviar por WhatsApp) y cuando la
  // campana detecta cambios (respuesta nueva o avisos marcados como leídos).
  React.useEffect(() => {
    const id = window.setInterval(recargar, REFRESCO_MS);
    const alVolver = () => document.visibilityState === "visible" && recargar();
    document.addEventListener("visibilitychange", alVolver);
    window.addEventListener(EVENTO_NOTIFICACIONES, recargar);
    return () => {
      window.clearInterval(id);
      document.removeEventListener("visibilitychange", alVolver);
      window.removeEventListener(EVENTO_NOTIFICACIONES, recargar);
    };
  }, [recargar]);

  async function abrir(item) {
    try {
      if (!item.leida) {
        await context.api(`/api/notificaciones/${item.id}/leida`, { method: "PATCH" });
        avisarCambioNotificaciones();
      }
    } catch {
      // Si no se pudo marcar, igual se navega a la cita
    }
    if (!item.citaId) return;
    if (esAdmin) {
      context.go(`modulo/citas?cita=${item.citaId}`);
    } else {
      context.go(`medico/agenda/${citaDateKey(item.citaFechaHora)}/${item.citaId}`);
    }
  }

  async function marcarTodas() {
    try {
      const r = await context.api(`/api/notificaciones/actividad/leidas${consulta ? `?${consulta}` : ""}`, { method: "POST" });
      context.notify(r?.marcadas ? `${r.marcadas} avisos marcados como leídos` : "No había avisos sin leer");
      avisarCambioNotificaciones();
      recargar();
    } catch (err) {
      context.notify(err.message, "error");
    }
  }

  const conteos = datos?.conteos || {};
  const grupos = React.useMemo(() => agruparPorDia(datos?.items || []), [datos]);

  return (
    <>
      <ViewHeader
        title="Actividad"
        subtitle={esAdmin ? "Respuestas de pacientes y recordatorios de toda la clínica" : "Respuestas de tus pacientes y recordatorios enviados"}
      >
        {esAdmin && (
          <select className="act-medico" value={medicoId} onChange={(e) => setMedicoId(e.target.value)} aria-label="Filtrar por médico">
            <option value="">Todos los médicos</option>
            {medicos.map((m) => (
              <option key={m.id} value={m.id}>{m.nombre}</option>
            ))}
          </select>
        )}
        <button className="btn icon" title="Actualizar" onClick={recargar}>
          <RefreshCw size={18} />
        </button>
      </ViewHeader>

      {error && <ErrorBox message={error} />}

      <ProximoDia
        datos={manana}
        cargando={cargando && !manana}
        esAdmin={esAdmin}
        onEnviar={() => setLote(true)}
      />

      {lote && manana?.pendientes?.length > 0 && (
        <RecordatoriosLote
          context={context}
          pendientes={manana.pendientes}
          esAdmin={esAdmin}
          onCambio={recargar}
          onCerrar={() => {
            setLote(false);
            recargar();
          }}
        />
      )}

      <section className="act-panel" aria-label="Actividad reciente">
        <div className="act-toolbar">
          <div className="act-filtros" role="tablist" aria-label="Filtros de actividad">
            {FILTROS.map((f) => (
              <button
                key={f.key}
                type="button"
                role="tab"
                aria-selected={filtro === f.key}
                className={`act-filtro ${filtro === f.key ? "activo" : ""}`}
                onClick={() => setFiltro(f.key)}
              >
                {f.label}
                {conteos[f.conteo] > 0 && <span className={`act-cuenta ${f.key === "SIN_LEER" ? "alerta" : ""}`}>{conteos[f.conteo]}</span>}
              </button>
            ))}
          </div>
          {conteos.sinLeer > 0 && (
            <button type="button" className="btn small ghost" onClick={marcarTodas}>
              <CheckCheck size={15} /> Marcar todo como leído
            </button>
          )}
        </div>

        {cargando && !datos ? (
          <EsqueletoLista />
        ) : grupos.length === 0 ? (
          <EstadoVacio icono={filtro === "SIN_LEER" ? CheckCircle2 : Inbox} titulo={VACIOS[filtro][0]} texto={VACIOS[filtro][1]} />
        ) : (
          grupos.map((g) => (
            <div className="act-grupo" key={g.clave}>
              <h3>{g.titulo}</h3>
              <ul>
                {g.items.map((item) => (
                  <li key={item.id}>
                    <AvisoActividad item={item} esAdmin={esAdmin} onAbrir={() => abrir(item)} />
                  </li>
                ))}
              </ul>
            </div>
          ))
        )}
        {datos && <p className="act-pie">Últimos 30 días · máximo 200 avisos</p>}
      </section>
    </>
  );
}

// ── Encabezado "Mañana" ─────────────────────────────────────────────────────────

function ProximoDia({ datos, cargando, esAdmin, onEnviar }) {
  if (cargando) {
    return (
      <section className="act-manana cargando" aria-busy="true">
        <div className="act-skel titulo" />
        <div className="act-tarjetas">
          {[0, 1, 2, 3].map((i) => <div className="act-skel tarjeta" key={i} />)}
        </div>
      </section>
    );
  }
  if (!datos) return null;
  if (!datos.fecha) {
    return (
      <section className="act-manana vacio">
        <CalendarDays size={28} />
        <div>
          <strong>Sin citas en los próximos 7 días</strong>
          <p>{esAdmin ? "La clínica no tiene citas agendadas esta semana." : "Disfruta la semana: no tienes citas agendadas."}</p>
        </div>
      </section>
    );
  }
  const etiqueta = datos.esManana ? "Mañana" : "Próximo día con citas";
  const n = datos.sinConfirmar;
  return (
    <section className="act-manana" aria-label={etiqueta}>
      <header>
        <div>
          <span className="act-eyebrow">{etiqueta}</span>
          <h2>{fechaLarga(datos.fecha)}</h2>
        </div>
        {n > 0 && (
          <button type="button" className="btn primary act-enviar" onClick={onEnviar}>
            <BellRing size={17} />
            {n === 1 ? "Enviar recordatorio a la cita sin confirmar" : `Enviar recordatorio a las ${n} sin confirmar`}
          </button>
        )}
      </header>
      <div className="act-tarjetas">
        <Tarjeta icono={CalendarDays} etiqueta="Citas" valor={datos.total} />
        <Tarjeta icono={CalendarCheck} etiqueta="Confirmadas" valor={datos.confirmadas} variante="ok" />
        <Tarjeta icono={CalendarClock} etiqueta="Sin confirmar" valor={datos.sinConfirmar} variante="aviso" />
        <Tarjeta icono={CalendarX} etiqueta="Canceladas" valor={datos.canceladas} variante="peligro" />
      </div>
      {n === 0 && datos.total > datos.canceladas && (
        <p className="act-nota"><CheckCircle2 size={15} /> Todas las citas de ese día ya están confirmadas.</p>
      )}
    </section>
  );
}

function Tarjeta({ icono: Icono, etiqueta, valor, variante = "" }) {
  return (
    <div className={`act-tarjeta ${variante}`}>
      <Icono size={18} aria-hidden="true" />
      <strong>{valor}</strong>
      <span>{etiqueta}</span>
    </div>
  );
}

// ── Envío de recordatorios del próximo día ─────────────────────────────────────

/**
 * Canal "enlace": un clic por paciente (cada clic abre una pestaña de wa.me; el navegador bloquea
 * varias pestañas abiertas a la vez). Otros canales: "Enviar a todas" en secuencia, sin reenviar.
 * Las citas que ya tenían recordatorio se muestran como "Ya enviado" con "Reenviar" explícito.
 */
function RecordatoriosLote({ context, pendientes, esAdmin, onCambio, onCerrar }) {
  const canal = useCanalWhatsApp(context);
  const esEnlace = canal === "enlace";
  const [tick, setTick] = React.useState(0);
  const ids = React.useMemo(() => pendientes.map((p) => p.citaId), [pendientes]);
  const [estados] = useRecordatorioEstados(context, ids, tick);
  const [enviandoTodas, setEnviandoTodas] = React.useState(false);
  const [resumen, setResumen] = React.useState(null);

  const yaEnviada = (p) => ["ENVIADO", "RESPONDIO"].includes(estados[p.citaId]?.estado);
  const enviables = pendientes.filter((p) => p.telefonoValido);
  const hechas = enviables.filter(yaEnviada).length;
  const siguiente = enviables.find((p) => !yaEnviada(p));

  const alCambiar = React.useCallback(() => {
    setTick((t) => t + 1);
    onCambio();
  }, [onCambio]);

  async function enviarTodas() {
    setEnviandoTodas(true);
    const r = { enviados: 0, yaEnviados: 0, fallidos: 0 };
    for (const p of enviables.filter((x) => !yaEnviada(x))) {
      try {
        await context.api(`/api/admin/recordatorios/${p.citaId}/enviar`, { method: "POST" });
        r.enviados += 1;
      } catch (err) {
        if (/ya se envi/i.test(err.message || "")) r.yaEnviados += 1;
        else r.fallidos += 1;
      }
    }
    setResumen(r);
    setEnviandoTodas(false);
    alCambiar();
  }

  return (
    <section className="act-lote" aria-label="Enviar recordatorios">
      <header>
        <div>
          <strong>Recordatorios por WhatsApp</strong>
          <span>
            {esEnlace
              ? "Da clic en cada paciente: se abre WhatsApp con el mensaje y el enlace. Envíalo y regresa por el siguiente."
              : "Puedes enviarlos todos a la vez o uno por uno."}
          </span>
        </div>
        <button type="button" className="btn icon ghost" onClick={onCerrar} aria-label="Cerrar">
          <X size={18} />
        </button>
      </header>

      <div className="act-progreso" aria-live="polite">
        <div className="act-barra"><span style={{ width: `${enviables.length ? (hechas / enviables.length) * 100 : 0}%` }} /></div>
        <small>{hechas} de {enviables.length} enviados</small>
      </div>

      {!esEnlace && canal && (
        <div className="act-lote-acciones">
          <button type="button" className="btn primary small" disabled={enviandoTodas || !siguiente} onClick={enviarTodas}>
            <Send size={14} /> {enviandoTodas ? "Enviando..." : "Enviar a todas las pendientes"}
          </button>
          {resumen && (
            <small>
              {resumen.enviados} enviados · {resumen.yaEnviados} ya tenían recordatorio · {resumen.fallidos} fallidos
            </small>
          )}
        </div>
      )}

      <ul className="act-lote-lista">
        {pendientes.map((p) => (
          <li key={p.citaId} className={siguiente?.citaId === p.citaId ? "siguiente" : ""}>
            <div className="act-lote-paciente">
              <strong>{p.pacienteNombre || "Paciente"}</strong>
              <span>
                {hora12(p.fechaHora)}
                {esAdmin && p.medicoNombre ? ` · ${p.medicoNombre}` : ""}
              </span>
            </div>
            {!p.telefonoValido ? (
              <span className="badge danger"><AlertTriangle size={13} /> Sin teléfono válido</span>
            ) : (
              <>
                {yaEnviada(p) && <span className="act-ya">Ya enviado</span>}
                <RecordatorioWhatsApp
                  context={context}
                  cita={{ id: p.citaId, estado: p.estado }}
                  estado={estados[p.citaId]}
                  onEnviado={alCambiar}
                  compacto
                />
              </>
            )}
          </li>
        ))}
      </ul>
      {hechas === enviables.length && enviables.length > 0 && (
        <p className="act-nota"><CheckCircle2 size={15} /> Listo: todas las citas pendientes tienen recordatorio.</p>
      )}
    </section>
  );
}

// ── Aviso de la lista ───────────────────────────────────────────────────────────

function AvisoActividad({ item, esAdmin, onAbrir }) {
  const { Icono, variante, frase, detalle } = describir(item);
  return (
    <button type="button" className={`act-aviso ${item.leida ? "" : "nuevo"}`} onClick={onAbrir}>
      <span className={`act-icono ${variante}`} aria-hidden="true"><Icono size={18} /></span>
      <span className="act-texto">
        <span className="act-frase">{frase}</span>
        <span className="act-detalle">
          {detalle}
          {item.origen === "ENLACE" && <em className="act-chip enlace"><Link2 size={12} /> Desde el enlace</em>}
          {item.origen === "WHATSAPP" && <em className="act-chip whatsapp"><MessageCircle size={12} /> Por WhatsApp</em>}
          {esAdmin && item.medicoNombre && <em className="act-chip">{item.medicoNombre}</em>}
        </span>
      </span>
      <time className="act-hora" dateTime={item.creadaEn} title={new Date(item.creadaEn).toLocaleString("es-MX")}>
        {haceCuanto(item.creadaEn)}
      </time>
      {!item.leida && <span className="act-punto" aria-label="Sin leer" />}
    </button>
  );
}

function describir(item) {
  const corto = nombreCorto(item.pacienteNombre);
  const completo = item.pacienteNombre || "el paciente";
  const cuando = item.citaFechaHora ? `${relativoCita(item.citaFechaHora)} a las ${hora12(item.citaFechaHora)}` : "";
  if (item.categoria === "CONFIRMACION") {
    return {
      Icono: CheckCircle2,
      variante: "ok",
      frase: <><strong>{corto}</strong> confirmó su cita {cuando}</>,
      detalle: null,
    };
  }
  if (item.categoria === "CANCELACION") {
    return {
      Icono: XCircle,
      variante: "peligro",
      frase: <><strong>{corto}</strong> canceló su cita {cuando}</>,
      detalle: null,
    };
  }
  if (item.estado === "FALLIDO") {
    return {
      Icono: AlertTriangle,
      variante: "aviso",
      frase: <>No se pudo enviar el recordatorio a <strong>{completo}</strong></>,
      detalle: cuando ? `Cita ${cuando}` : null,
    };
  }
  return {
    Icono: item.canalEnvio === "enlace" ? Link2 : Send,
    variante: "info",
    frase: <>{item.canalEnvio === "enlace" ? "Enlace enviado" : "Recordatorio enviado"} a <strong>{completo}</strong></>,
    detalle: cuando ? `Cita ${cuando}${item.citaEstado && item.citaEstado !== "SIN_CONFIRMAR" ? ` · ${estadoLegible(item.citaEstado)}` : ""}` : null,
  };
}

// ── Estados de carga y vacío ────────────────────────────────────────────────────

function EsqueletoLista() {
  return (
    <div className="act-esqueleto" aria-busy="true" aria-label="Cargando actividad">
      {[0, 1, 2, 3, 4].map((i) => (
        <div className="act-skel fila" key={i}>
          <span className="act-skel circulo" />
          <span className="act-skel linea" />
        </div>
      ))}
    </div>
  );
}

function EstadoVacio({ icono: Icono, titulo, texto }) {
  return (
    <div className="act-vacio">
      <span><Icono size={30} /></span>
      <strong>{titulo}</strong>
      <p>{texto}</p>
    </div>
  );
}

// ── Fechas y textos ─────────────────────────────────────────────────────────────

const DIA_MS = 86400000;

/** Fecha "YYYY-MM-DD" de un instante real en la zona de la clínica. */
function claveDiaClinica(instante) {
  return new Intl.DateTimeFormat("en-CA", { timeZone: CLINIC_TIME_ZONE, year: "numeric", month: "2-digit", day: "2-digit" })
    .format(new Date(instante));
}

function diasEntre(desdeKey, hastaKey) {
  return Math.round((Date.parse(`${hastaKey}T00:00:00Z`) - Date.parse(`${desdeKey}T00:00:00Z`)) / DIA_MS);
}

function agruparPorDia(items) {
  const hoy = clinicToday();
  const grupos = [];
  for (const item of items) {
    const clave = claveDiaClinica(item.creadaEn);
    let grupo = grupos[grupos.length - 1];
    if (!grupo || grupo.clave !== clave) {
      const d = diasEntre(clave, hoy);
      const titulo = d === 0 ? "Hoy" : d === 1 ? "Ayer" : capitalizar(fechaLarga(clave));
      grupo = { clave, titulo, items: [] };
      grupos.push(grupo);
    }
    grupo.items.push(item);
  }
  return grupos;
}

/** "de hoy" / "de mañana" / "de ayer" / "del jueves" / "del 3 de diciembre" (convención de reloj en UTC). */
function relativoCita(fechaHora) {
  const d = diasEntre(clinicToday(), citaDateKey(fechaHora));
  if (d === 0) return "de hoy";
  if (d === 1) return "de mañana";
  if (d === -1) return "de ayer";
  const fecha = new Date(fechaHora);
  if (d > 1 && d < 7) return `del ${fecha.toLocaleDateString("es-MX", { weekday: "long", timeZone: UTC })}`;
  return `del ${fecha.toLocaleDateString("es-MX", { day: "numeric", month: "long", timeZone: UTC })}`;
}

/** "lunes 23 de noviembre" a partir de "YYYY-MM-DD". */
function fechaLarga(clave) {
  return new Date(`${clave}T12:00:00Z`).toLocaleDateString("es-MX", { weekday: "long", day: "numeric", month: "long", timeZone: UTC });
}

function haceCuanto(instante) {
  const min = Math.floor((Date.now() - new Date(instante).getTime()) / 60000);
  if (min < 1) return "ahora";
  if (min < 60) return `hace ${min} min`;
  if (min < 24 * 60 && claveDiaClinica(instante) === clinicToday()) return `hace ${Math.floor(min / 60)} h`;
  return new Date(instante).toLocaleTimeString("es-MX", { hour: "numeric", minute: "2-digit", timeZone: CLINIC_TIME_ZONE });
}

/** Hora de la cita con el formato de 12 h de la app (formatCitaHora12), sin cero inicial: "1:30 p.m.". */
function hora12(fechaHora) {
  return formatCitaHora12(fechaHora).replace(/^0(\d)/, "$1");
}

/** "Ana Sofía Ramírez Mendoza" → "Ana Sofía" (dos primeras palabras). */
function nombreCorto(nombre) {
  if (!nombre) return "Un paciente";
  return nombre.trim().split(/\s+/).slice(0, 2).join(" ");
}

function estadoLegible(estado) {
  return { CONFIRMADA: "confirmada", CANCELADA: "cancelada", NO_ASISTIO: "no asistió", REAGENDADA: "reagendada" }[estado] || estado.toLowerCase();
}

function capitalizar(texto) {
  return texto ? texto[0].toUpperCase() + texto.slice(1) : texto;
}
