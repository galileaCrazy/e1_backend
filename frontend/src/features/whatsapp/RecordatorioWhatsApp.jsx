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

/** Botón "Enviar recordatorio WhatsApp" + indicador del último estado (enviado / respondió / falló). */
export function RecordatorioWhatsApp({ context, cita, estado, onEnviado, compacto = false }) {
  const [enviando, setEnviando] = React.useState(false);
  const [confirmarReenvio, setConfirmarReenvio] = React.useState(false);
  const yaEnviado = estado && ["ENVIADO", "RESPONDIO"].includes(estado.estado);
  const puedeEnviar = cita.estado === "SIN_CONFIRMAR";

  async function enviar(reenviar) {
    setEnviando(true);
    setConfirmarReenvio(false);
    try {
      await context.api(`/api/admin/recordatorios/${cita.id}/enviar${reenviar ? "?reenviar=true" : ""}`, { method: "POST" });
      context.notify(reenviar ? "Recordatorio reenviado por WhatsApp" : "Recordatorio enviado por WhatsApp");
    } catch (err) {
      const mensaje = err.message === "HTTP 502"
        ? "El proveedor de WhatsApp no respondió; el intento quedó registrado como fallido."
        : err.message;
      context.notify(mensaje, "error");
    } finally {
      setEnviando(false);
      onEnviado?.();
    }
  }

  if (!estado && !puedeEnviar) return null;

  return (
    <div className={`wa-recordatorio ${compacto ? "compacto" : ""}`}>
      <IndicadorRecordatorio estado={estado} />
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

function IndicadorRecordatorio({ estado }) {
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
      <Check size={13} /> Recordatorio enviado{hora ? ` · ${hora}` : ""}
    </span>
  );
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
