import React from "react";
import { AlertTriangle, Check, MessageSquareReply, Send } from "lucide-react";
import "./WhatsApp.css";

/**
 * Último estado del recordatorio por cita ({ [citaId]: { estado, fecha, respuesta } }).
 * Se vuelve a pedir cuando cambian los ids o el `tick` (refresco periódico de la vista).
 */
export function useRecordatorioEstados(context, citaIds, tick = 0) {
  const [estados, setEstados] = React.useState({});
  const clave = [...new Set(citaIds.filter(Boolean))].sort().join(",");

  const cargar = React.useCallback(async () => {
    if (!clave) {
      setEstados({});
      return;
    }
    try {
      const data = await context.api(`/api/whatsapp/recordatorios/estado?citaIds=${clave}`);
      setEstados(data || {});
    } catch {
      // El indicador es informativo: si falla, la agenda sigue funcionando
    }
  }, [context, clave]);

  React.useEffect(() => {
    cargar();
  }, [cargar, tick]);

  return [estados, cargar];
}

// Proveedor activo (simulado | twilio | meta | intoglobe | enlace); se pide una sola vez por carga de página
let canalCache = null;

export function useCanalWhatsApp(context) {
  const [canal, setCanal] = React.useState(() => (typeof canalCache === "string" ? canalCache : null));
  React.useEffect(() => {
    if (canal) return undefined;
    let activo = true;
    if (!canalCache) {
      canalCache = context
        .api("/api/whatsapp/canal")
        .then((data) => (canalCache = data?.proveedor || "?"))
        .catch(() => {
          canalCache = null;
          return null;
        });
    }
    Promise.resolve(canalCache).then((valor) => activo && valor && setCanal(valor));
    return () => {
      activo = false;
    };
  }, [context, canal]);
  return canal;
}

/** Botón "Enviar recordatorio WhatsApp" + indicador del último estado (enviado / respondió / falló). */
export function RecordatorioWhatsApp({ context, cita, estado, onEnviado, compacto = false }) {
  const canal = useCanalWhatsApp(context);
  const esEnlace = canal === "enlace";
  const [enviando, setEnviando] = React.useState(false);
  const [confirmarReenvio, setConfirmarReenvio] = React.useState(false);
  const yaEnviado = estado && ["ENVIADO", "RESPONDIO"].includes(estado.estado);
  const puedeEnviar = cita.estado === "SIN_CONFIRMAR";

  async function enviar(reenviar) {
    setEnviando(true);
    setConfirmarReenvio(false);
    // Canal "enlace": la pestaña se abre dentro del clic; si se abriera después del await,
    // el navegador la bloquearía como ventana emergente.
    const pestana = esEnlace ? window.open("", "_blank") : null;
    try {
      const r = await context.api(`/api/admin/recordatorios/${cita.id}/enviar${reenviar ? "?reenviar=true" : ""}`, { method: "POST" });
      if (r?.waUrl) {
        if (pestana && !pestana.closed) {
          pestana.opener = null;
          pestana.location.href = r.waUrl;
        } else {
          window.open(r.waUrl, "_blank", "noopener");
        }
        context.notify("Se abrió WhatsApp con el recordatorio y el enlace. Envíalo al paciente.");
      } else {
        pestana?.close();
        context.notify(reenviar ? "Recordatorio reenviado por WhatsApp" : "Recordatorio enviado por WhatsApp");
      }
    } catch (err) {
      pestana?.close();
      context.notify(mensajeErrorCanal(err.message), "error");
    } finally {
      setEnviando(false);
      onEnviado?.();
    }
  }

  if (!estado && !puedeEnviar) return null;

  return (
    <div className={`wa-recordatorio ${compacto ? "compacto" : ""}`}>
      <IndicadorRecordatorio estado={estado} esEnlace={esEnlace} />
      {puedeEnviar && !confirmarReenvio && (
        <button
          type="button"
          className="btn small wa-btn"
          disabled={enviando}
          onClick={() => (yaEnviado ? setConfirmarReenvio(true) : enviar(false))}
        >
          <Send size={13} />
          {enviando ? "Enviando..." : yaEnviado ? "Reenviar" : "Enviar recordatorio WhatsApp"}
        </button>
      )}
      {confirmarReenvio && (
        <span className="wa-confirmar" role="group" aria-label="Confirmar reenvío">
          ¿Reenviar?
          <button type="button" className="btn small primary" onClick={() => enviar(true)}>Sí</button>
          <button type="button" className="btn small" onClick={() => setConfirmarReenvio(false)}>No</button>
        </span>
      )}
    </div>
  );
}

function IndicadorRecordatorio({ estado, esEnlace }) {
  if (!estado) return null;
  const hora = estado.fecha ? formatHoraLocal(estado.fecha) : "";
  if (estado.estado === "RESPONDIO") {
    return (
      <span className="badge ok wa-indicador" title={`Respondió ${hora}`}>
        <MessageSquareReply size={13} /> Respondió{estado.respuesta ? `: "${recortar(estado.respuesta)}"` : ""}
      </span>
    );
  }
  if (estado.estado === "FALLIDO") {
    return (
      <span className="badge danger wa-indicador" title={hora}>
        <AlertTriangle size={13} /> Envío fallido
      </span>
    );
  }
  return (
    <span className="badge info wa-indicador" title={hora}>
      <Check size={13} /> {esEnlace ? "Enlace enviado" : "Recordatorio enviado"}{hora ? ` · ${hora}` : ""}
    </span>
  );
}

/** Mensajes de error del canal en español (los del backend ya vienen en español). */
function mensajeErrorCanal(mensaje) {
  if (mensaje === "HTTP 502") return "El proveedor de WhatsApp no respondió; el intento quedó registrado como fallido.";
  if (mensaje === "Failed to fetch") return "No hay conexión con el servidor. Revisa que el backend esté levantado.";
  if (/^HTTP \d+$/.test(mensaje || "")) return `No se pudo enviar el recordatorio (${mensaje}).`;
  return mensaje;
}

// fecha de envío/respuesta = instante real → hora local del navegador
export function formatHoraLocal(value) {
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleTimeString("es-MX", { hour: "numeric", minute: "2-digit" });
}

function recortar(texto) {
  return texto.length > 18 ? `${texto.slice(0, 18)}…` : texto;
}
