import React from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  Bot,
  Building2,
  CalendarDays,
  Clock3,
  CreditCard,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  RefreshCw,
  Save,
  Search,
  Settings,
  Stethoscope,
  Trash2,
  Upload,
  UserCog,
  Users,
  X,
} from "lucide-react";
import { Empty, ErrorBox, Loading, Metric, Section, ViewHeader } from "./components/ui";
import MedicoAgenda from "./features/medico/agenda/MedicoAgenda";
import MedicoConsultaDetalle from "./features/medico/consulta/MedicoConsultaDetalle";
import MedicoDashboard from "./features/medico/dashboard/MedicoDashboard";
import MedicoDiagnosticosAdjuntos from "./features/medico/diagnosticos-adjuntos/MedicoDiagnosticosAdjuntos";
import MedicoExpedientePaciente from "./features/medico/expediente/MedicoExpedientePaciente";
import { displayName, formatCell, initials, sessionDisplayName, shortId } from "./lib/display";
import "./styles.css";

const STORAGE_KEY = "medinflow.session";
const API_KEY = "medinflow.apiUrl";
const DEFAULT_API_URL = normalizeApiUrl(import.meta.env.VITE_API_URL || "http://localhost:8081");

const iconMap = {
  dashboard: LayoutDashboard,
  citas: CalendarDays,
  pacientes: Users,
  medicos: Stethoscope,
  consultorios: Building2,
  horarios: Clock3,
  pagos: CreditCard,
  diagnosticos: FileText,
  adjuntos: Upload,
  notificaciones: Activity,
  usuarios: UserCog,
  organizaciones: Building2,
  medicoClinico: FileText,
  ia: Bot,
  settings: Settings,
};

const modules = {
  citas: {
    label: "Citas",
    roles: ["ADMIN", "MEDICO", "PACIENTE"],
    description: "Historial de citas por paciente y estado.",
    list: ({ orgId, filters, role, medicoId }) => {
      if (role === "MEDICO") return medicoId ? `/api/citas/medico/${medicoId}` : null;
      if (filters.pacienteId) return `/api/citas/paciente/${filters.pacienteId}`;
      if (filters.medicoId) return `/api/citas/medico/${filters.medicoId}`;
      if (filters.estado) return `/api/citas/organizacion/${orgId}/estado/${filters.estado}`;
      return role === "PACIENTE" ? null : `/api/citas/organizacion/${orgId}`;
    },
    clientFilter: (rows, filters, role) => {
      if (role !== "MEDICO") return rows;
      return rows.filter((row) => {
        if (filters.pacienteId && row.pacienteId !== filters.pacienteId) return false;
        if (filters.estado && row.estado !== filters.estado) return false;
        return true;
      });
    },
    endpoint: "/api/citas",
    columns: [
      ["fechaHora", "Fecha", "datetime"],
      ["pacienteId", "Paciente", "patient"],
      ["medicoId", "Medico", "doctor"],
      ["estado", "Estado", "status"],
      ["motivo", "Motivo"],
    ],
    filters: [
      { key: "pacienteId", label: "Paciente", source: "pacientes" },
      { key: "medicoId", label: "Medico", source: "medicos", roles: ["ADMIN", "PACIENTE"] },
      { key: "estado", label: "Estado", options: ["", "SIN_CONFIRMAR", "CONFIRMADA", "CANCELADA", "REAGENDADA", "NO_ASISTIO"] },
    ],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "pacienteId", label: "Paciente", source: "pacientes", required: true },
      { key: "medicoId", type: "hiddenMedico", required: true, roles: ["MEDICO"] },
      { key: "medicoId", label: "Medico", source: "medicos", required: true, roles: ["ADMIN", "PACIENTE"] },
      { key: "consultorioId", label: "Consultorio", source: "consultorios", required: true },
      { key: "fechaHora", label: "Fecha y hora", type: "datetime-local", required: true },
      { key: "duracionMin", label: "Duracion", type: "select", options: ["20", "30", "45", "60"], required: true },
      { key: "estado", label: "Estado", type: "select", options: ["SIN_CONFIRMAR", "CONFIRMADA", "CANCELADA", "REAGENDADA", "NO_ASISTIO"] },
      { key: "motivo", label: "Motivo", type: "textarea", full: true },
    ],
  },
  pacientes: {
    label: "Pacientes",
    roles: ["ADMIN", "MEDICO"],
    description: "Expedientes basicos de pacientes por organizacion.",
    list: ({ orgId, filters }) => {
      if (filters.nombre) return `/api/pacientes/organizacion/${orgId}/buscar?nombre=${encodeURIComponent(filters.nombre)}`;
      if (filters.activos) return `/api/pacientes/organizacion/${orgId}/activos`;
      return `/api/pacientes/organizacion/${orgId}`;
    },
    endpoint: "/api/pacientes",
    columns: [
      ["nombre", "Paciente"],
      ["telefono", "Telefono"],
      ["email", "Correo"],
      ["sexo", "Sexo"],
      ["activo", "Activo", "bool"],
    ],
    filters: [
      { key: "nombre", label: "Buscar nombre", type: "text" },
      { key: "activos", label: "Solo activos", type: "checkbox" },
    ],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "nombre", label: "Nombre", required: true, value: (item) => patientNamePart(item.nombre, 0) },
      { key: "apellidoPaterno", label: "Apellido paterno", value: (item) => patientNamePart(item.nombre, 1) },
      { key: "apellidoMaterno", label: "Apellido materno", value: (item) => patientNamePart(item.nombre, 2) },
      { key: "telefono", label: "Telefono" },
      { key: "fechaNacimiento", label: "Nacimiento", type: "date" },
      { key: "sexo", label: "Sexo", type: "select", options: ["", "M", "F", "O"] },
      { key: "email", label: "Correo", type: "email" },
      { key: "notas", label: "Notas", type: "textarea", full: true },
    ],
    transformPayload: (payload) => {
      payload.nombre = [payload.nombre, payload.apellidoPaterno, payload.apellidoMaterno]
        .map((value) => String(value || "").trim())
        .filter(Boolean)
        .join(" ");
      delete payload.apellidoPaterno;
      delete payload.apellidoMaterno;
      return payload;
    },
  },
  medicos: {
    label: "Medicos",
    roles: ["ADMIN"],
    description: "Directorio medico y tarifas base.",
    list: ({ orgId, filters }) => {
      if (filters.consultorioId) return `/api/medicos/consultorio/${filters.consultorioId}`;
      if (filters.activos) return `/api/medicos/organizacion/${orgId}/activos`;
      return `/api/medicos/organizacion/${orgId}`;
    },
    endpoint: "/api/medicos",
    columns: [
      ["nombre", "Medico"],
      ["especialidad", "Especialidad"],
      ["consultorioId", "Consultorio", "office"],
      ["tarifaBase", "Tarifa", "money"],
      ["activo", "Activo", "bool"],
    ],
    filters: [
      { key: "consultorioId", label: "Consultorio", source: "consultorios" },
      { key: "activos", label: "Solo activos", type: "checkbox" },
    ],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "consultorioId", label: "Consultorio", source: "consultorios", required: true },
      { key: "nombre", label: "Nombre", required: true },
      { key: "especialidad", label: "Especialidad" },
      { key: "cedula", label: "Cedula" },
      { key: "telefono", label: "Telefono" },
      { key: "tarifaBase", label: "Tarifa base", type: "number" },
    ],
  },
  consultorios: {
    label: "Consultorios",
    roles: ["ADMIN"],
    description: "Sedes, consultorios y telefonos de contacto.",
    list: ({ orgId, filters }) =>
      filters.activos ? `/api/consultorios/organizacion/${orgId}/activos` : `/api/consultorios/organizacion/${orgId}`,
    endpoint: "/api/consultorios",
    columns: [
      ["nombre", "Consultorio"],
      ["direccion", "Direccion"],
      ["telefono", "Telefono"],
      ["activo", "Activo", "bool"],
    ],
    filters: [{ key: "activos", label: "Solo activos", type: "checkbox" }],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "nombre", label: "Nombre", required: true },
      { key: "direccion", label: "Direccion", type: "textarea", full: true },
      { key: "telefono", label: "Telefono" },
    ],
  },
  horarios: {
    label: "Horarios",
    roles: ["ADMIN", "MEDICO"],
    description: "Disponibilidad semanal por medico.",
    list: ({ filters, role, medicoId }) => {
      const selectedMedicoId = filters.medicoId || (role === "MEDICO" ? medicoId : "");
      return selectedMedicoId ? `/api/horarios/medico/${selectedMedicoId}` : null;
    },
    endpoint: "/api/horarios",
    columns: [
      ["medicoId", "Medico", "doctor"],
      ["diaSemana", "Dia", "day"],
      ["horaInicio", "Inicio"],
      ["horaFin", "Fin"],
      ["duracionConsulta", "Duracion"],
    ],
    filters: [{ key: "medicoId", label: "Medico", source: "medicos", required: true }],
    fields: [
      { key: "medicoId", label: "Medico", source: "medicos", required: true },
      { key: "diaSemana", label: "Dia", type: "select", options: ["0", "1", "2", "3", "4", "5", "6"], required: true },
      { key: "horaInicio", label: "Hora inicio", type: "time", required: true },
      { key: "horaFin", label: "Hora fin", type: "time", required: true },
      { key: "duracionConsulta", label: "Duracion", type: "select", options: ["20", "30", "45", "60"], required: true },
    ],
  },
  pagos: {
    label: "Pagos",
    roles: ["ADMIN", "MEDICO", "PACIENTE"],
    description: "Cobros, metodos de pago y referencias.",
    list: ({ orgId, filters, role }) => {
      if (filters.citaId) return `/api/pagos/cita/${filters.citaId}`;
      if (filters.estado) return `/api/pagos/organizacion/${orgId}/estado/${filters.estado}`;
      return role === "PACIENTE" ? null : `/api/pagos/organizacion/${orgId}`;
    },
    endpoint: "/api/pagos",
    columns: [
      ["citaId", "Cita", "appointment"],
      ["monto", "Monto", "money"],
      ["metodo", "Metodo"],
      ["estado", "Estado", "status"],
      ["referencia", "Referencia"],
    ],
    filters: [
      { key: "citaId", label: "Cita", source: "citas" },
      { key: "estado", label: "Estado", options: ["", "PAGADO", "PENDIENTE"] },
    ],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "citaId", label: "Cita", source: "citas", required: true },
      { key: "monto", label: "Monto", type: "number", required: true },
      { key: "metodo", label: "Metodo", type: "select", options: ["EFECTIVO", "TRANSFERENCIA", "TARJETA"], required: true },
      { key: "concepto", label: "Concepto" },
      { key: "referencia", label: "Referencia" },
    ],
  },
  diagnosticos: {
    label: "Diagnosticos",
    roles: ["ADMIN", "PACIENTE"],
    description: "Diagnosticos clinicos asociados a citas.",
    list: ({ filters }) => {
      if (!filters.citaId) return null;
      return filters.tipo ? `/api/diagnosticos/cita/${filters.citaId}/tipo/${filters.tipo}` : `/api/diagnosticos/cita/${filters.citaId}`;
    },
    endpoint: "/api/diagnosticos",
    columns: [
      ["citaId", "Cita", "appointment"],
      ["codigoCie10", "CIE-10"],
      ["descripcion", "Descripcion"],
      ["tipo", "Tipo", "status"],
      ["createdAt", "Creado", "datetime"],
    ],
    filters: [
      { key: "citaId", label: "Cita", source: "citas", required: true },
      { key: "tipo", label: "Tipo", options: ["", "PRINCIPAL", "SECUNDARIO"] },
    ],
    fields: [
      { key: "citaId", label: "Cita", source: "citas", required: true },
      { key: "codigoCie10", label: "Codigo CIE-10" },
      { key: "tipo", label: "Tipo", type: "select", options: ["PRINCIPAL", "SECUNDARIO"] },
      { key: "descripcion", label: "Descripcion", type: "textarea", full: true, required: true },
    ],
  },
  adjuntos: {
    label: "Adjuntos",
    roles: ["ADMIN", "PACIENTE"],
    description: "Metadatos de archivos clinicos y documentos.",
    list: ({ filters }) => {
      if (filters.citaId) return `/api/adjuntos/cita/${filters.citaId}`;
      if (filters.pacienteId) return `/api/adjuntos/paciente/${filters.pacienteId}`;
      return null;
    },
    endpoint: "/api/adjuntos",
    columns: [
      ["nombreArchivo", "Archivo"],
      ["tipo", "Tipo"],
      ["pacienteId", "Paciente", "patient"],
      ["mimeType", "MIME"],
      ["notificar", "Notificar", "bool"],
    ],
    filters: [
      { key: "pacienteId", label: "Paciente", source: "pacientes", required: true },
      { key: "citaId", label: "Cita", source: "citas" },
    ],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "pacienteId", label: "Paciente", source: "pacientes", required: true },
      { key: "citaId", label: "Cita", source: "citas" },
      { key: "subidoPorId", type: "hiddenUser", required: true },
      { key: "tipo", label: "Tipo", required: true },
      { key: "nombreArchivo", label: "Nombre del archivo", required: true },
      { key: "urlArchivo", label: "URL del archivo", full: true, required: true },
      { key: "mimeType", label: "MIME" },
      { key: "notificar", label: "Notificar", type: "checkbox" },
    ],
  },
  notificaciones: {
    label: "Notificaciones",
    roles: ["ADMIN", "MEDICO"],
    description: "Recordatorios y avisos ligados a citas.",
    list: ({ filters, role }) => {
      if (filters.citaId) return `/api/notificaciones/cita/${filters.citaId}`;
      return role === "ADMIN" ? "/api/notificaciones/pendientes" : null;
    },
    endpoint: "/api/notificaciones",
    columns: [
      ["citaId", "Cita", "appointment"],
      ["canal", "Canal"],
      ["tipo", "Tipo"],
      ["estado", "Estado", "status"],
      ["enviadaEn", "Enviada", "datetime"],
    ],
    filters: [{ key: "citaId", label: "Cita", source: "citas" }],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "citaId", label: "Cita", source: "citas", required: true },
      { key: "adjuntoId", label: "Adjunto", source: "adjuntos" },
      { key: "canal", label: "Canal", type: "select", options: ["WHATSAPP"], required: true },
      { key: "tipo", label: "Tipo", type: "select", options: ["RECORDATORIO_48H", "RECORDATORIO_24H", "ADJUNTO"], required: true },
    ],
  },
  usuarios: {
    label: "Usuarios",
    roles: ["ADMIN"],
    description: "Cuentas internas para administradores y medicos.",
    list: ({ orgId }) => `/api/usuarios/organizacion/${orgId}`,
    endpoint: "/api/usuarios",
    columns: [
      ["email", "Correo"],
      ["rol", "Rol", "status"],
      ["medicoId", "Medico", "doctor"],
      ["activo", "Activo", "bool"],
    ],
    fields: [
      { key: "organizacionId", type: "hiddenOrg", required: true },
      { key: "medicoId", label: "Medico", source: "medicos" },
      { key: "email", label: "Correo", type: "email", required: true },
      { key: "rol", label: "Rol", type: "select", options: ["ADMIN", "MEDICO"], required: true },
      { key: "password", label: "Password", type: "password", required: true },
    ],
  },
  organizaciones: {
    label: "Organizaciones",
    roles: ["ADMIN"],
    description: "Clinicas, planes y periodo de prueba.",
    list: () => "/api/organizaciones",
    endpoint: "/api/organizaciones",
    columns: [
      ["nombre", "Nombre"],
      ["plan", "Plan", "status"],
      ["trialHasta", "Trial", "datetime"],
      ["activo", "Activo", "bool"],
    ],
    fields: [
      { key: "nombre", label: "Nombre", required: true },
      { key: "plan", label: "Plan", type: "select", options: ["SOLO", "CLINICA", "ENTERPRISE"], required: true },
      { key: "trialHasta", label: "Trial hasta", type: "datetime-local" },
    ],
  },
};

function readJson(key) {
  try {
    return JSON.parse(localStorage.getItem(key));
  } catch {
    return null;
  }
}

function decodeJwt(token) {
  if (!token?.includes(".")) return {};
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(payload)
        .split("")
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return {};
  }
}

function requestValue(value) {
  if (value === "" || value == null) return undefined;
  return value;
}

function normalizeApiUrl(value) {
  return String(value || "").replace(/\/$/, "");
}

function App() {
  const [session, setSession] = React.useState(() => readJson(STORAGE_KEY));
  const [apiUrl, setApiUrlState] = React.useState(() => localStorage.getItem(API_KEY) || DEFAULT_API_URL);
  const [route, setRoute] = React.useState(() => {
    const path = location.pathname.replace(/^\//, "");
    return path || (session ? "dashboard" : "login");
  });
  const [toast, setToast] = React.useState(null);

  React.useEffect(() => {
    const onPop = () => {
      const path = location.pathname.replace(/^\//, "");
      setRoute(path || (session ? "dashboard" : "login"));
    };
    window.addEventListener("popstate", onPop);
    return () => window.removeEventListener("popstate", onPop);
  }, [session]);

  const role = session?.rol || session?.claims?.rol || "PACIENTE";
  const orgId = session?.organizacionId || session?.claims?.organizacionId || "";
  const userId = session?.userId || session?.claims?.userId || session?.claims?.usuarioId || "";
  const medicoId = session?.medicoId || session?.claims?.medicoId || "";
  const userName = sessionDisplayName(session);

  const notify = React.useCallback((message, kind = "ok") => {
    setToast({ message, kind });
    window.setTimeout(() => setToast(null), 4200);
  }, []);

  const setApiUrl = React.useCallback((value) => {
    const clean = normalizeApiUrl(value);
    localStorage.setItem(API_KEY, clean);
    setApiUrlState(clean);
  }, []);

  const api = React.useCallback(
    async (pathname, options = {}) => {
      const response = await fetch(`${apiUrl}${pathname}`, {
        ...options,
        headers: {
          Accept: "application/json",
          ...(options.body ? { "Content-Type": "application/json" } : {}),
          ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
          ...(options.headers || {}),
        },
      });
      const contentType = response.headers.get("content-type") || "";
      const body = contentType.includes("application/json") ? await response.json().catch(() => null) : await response.text();
      if (!response.ok) {
        throw new Error(body?.message || body?.detail || body?.error || body || `HTTP ${response.status}`);
      }
      return body;
    },
    [apiUrl, session?.token]
  );

  const list = React.useCallback(
    async (pathname, silent = false) => {
      try {
        const result = await api(pathname);
        if (!result) return [];
        return Array.isArray(result) ? result : [result];
      } catch (error) {
        if (silent) return [];
        throw error;
      }
    },
    [api]
  );

  const go = React.useCallback((nextRoute) => {
    history.pushState(null, "", `/${nextRoute}`);
    setRoute(nextRoute);
  }, []);

  const onLogin = async (payload, mode) => {
    const result = await api(mode === "registro" ? "/api/auth/registro" : "/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    const next = { ...result, claims: decodeJwt(result.token) };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    setSession(next);
    notify("Sesion iniciada");
    go("dashboard");
  };

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY);
    setSession(null);
    go("login");
  };

  const context = React.useMemo(
    () => ({ api, list, notify, go, role, orgId, userId, medicoId, userName, session, apiUrl, setApiUrl }),
    [api, list, notify, go, role, orgId, userId, medicoId, userName, session, apiUrl, setApiUrl]
  );

  if (!session) {
    return (
      <>
        <AuthPage mode={route === "registro" ? "registro" : "login"} onSubmit={onLogin} go={go} notify={notify} />
        <Toast toast={toast} />
      </>
    );
  }

  return (
    <>
      <Shell context={context} route={route} logout={logout}>
        <CurrentView route={route} context={context} />
      </Shell>
      <Toast toast={toast} />
    </>
  );
}

function AuthPage({ mode, onSubmit, go, notify }) {
  const isRegister = mode === "registro";
  const [submitting, setSubmitting] = React.useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    const form = new FormData(event.currentTarget);
    try {
      await onSubmit(
        {
          email: form.get("email"),
          password: form.get("password"),
          ...(isRegister ? { organizacionId: form.get("organizacionId") } : {}),
        },
        mode
      );
    } catch (error) {
      notify(error.message, "error");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-panel">
        <div className="auth-card">
          <div className="brand">
            <h1>MedInFlow</h1>
            <p>Plataforma medica integral</p>
          </div>
          <form className="stack" onSubmit={handleSubmit}>
            <Field label="Correo electronico" name="email" type="email" placeholder="admin@clinica.com" required />
            <Field label="Password" name="password" type="password" placeholder="Minimo 8 caracteres" required />
            {isRegister && <Field label="Organizacion ID" name="organizacionId" placeholder="UUID de la organizacion" required />}
            <button className="btn primary wide" disabled={submitting}>
              {submitting ? "Conectando..." : isRegister ? "Crear cuenta paciente" : "Iniciar sesion"}
            </button>
          </form>
          <button className="btn ghost wide" onClick={() => go(isRegister ? "login" : "registro")}>
            {isRegister ? "Ya tengo cuenta" : "Registrar paciente"}
          </button>
        </div>
      </section>
      <section className="auth-art">
        <div className="visual-window">
          <div className="monitor-card">
            <span className="badge info">OPERACION CLINICA</span>
            <h2>Agenda, expedientes y pagos en un solo panel.</h2>
            <div className="metric-lines">
              <span style={{ width: "86%" }} />
              <span style={{ width: "64%", background: "var(--secondary)" }} />
              <span style={{ width: "42%", background: "var(--tertiary)" }} />
            </div>
          </div>
        </div>
        <h2>Precision clinica para operar el dia.</h2>
        <p>El diseno de Stitch queda como base visual; esta version ya vive en React y habla con los endpoints reales.</p>
      </section>
    </main>
  );
}

function Shell({ context, route, logout, children }) {
  const [open, setOpen] = React.useState(false);
  const [search, setSearch] = React.useState("");
  const allowed = Object.entries(modules).filter(([, config]) => config.roles.includes(context.role));
  const active = route.startsWith("modulo/") ? route.split("/")[1] : route;

  React.useEffect(() => {
    document.querySelectorAll("tbody tr").forEach((row) => {
      row.hidden = Boolean(search) && !row.textContent.toLowerCase().includes(search.toLowerCase());
    });
  }, [search, children]);

  return (
    <div className="app-shell">
      <aside className={`sidebar ${open ? "open" : ""}`}>
        <div className="sidebar-header">
          <div className="brand-mark">MedInFlow</div>
          <p>{context.userName}</p>
        </div>
        <nav className="nav-list">
          <NavButton id="dashboard" label="Dashboard" active={active === "dashboard"} onClick={() => context.go("dashboard")} />
          {allowed.map(([key, config]) => (
            <NavButton key={key} id={key} label={config.label} active={active === key} onClick={() => context.go(`modulo/${key}`)} />
          ))}
          {context.role === "MEDICO" && (
            <NavButton
              id="medicoClinico"
              label="Diagnosticos y Adjuntos"
              active={route === "medico/diagnosticos-adjuntos"}
              onClick={() => context.go("medico/diagnosticos-adjuntos")}
            />
          )}
          <NavButton id="ia" label="IA Chat" active={active === "ia"} onClick={() => context.go("ia")} />
        </nav>
        <div className="nav-footer">
          <NavButton id="settings" label="Ajustes" active={active === "settings"} onClick={() => context.go("settings")} />
          <button className="nav-item" onClick={logout}>
            <LogOut size={18} />
            <span>Salir</span>
          </button>
        </div>
      </aside>
      <header className="topbar">
        <button className="btn icon mobile-menu" onClick={() => setOpen((value) => !value)} aria-label="Menu">
          <Menu size={18} />
        </button>
        <label className="search">
          <Search size={17} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Buscar en la vista actual..." />
        </label>
        <div className="user-chip">
          <div className="user-meta">
            <strong>{context.userName}</strong>
            <span>{context.role}</span>
          </div>
          <div className="avatar">{initials(context.userName)}</div>
        </div>
      </header>
      <main className="content">
        <div className="content-inner">{children}</div>
      </main>
    </div>
  );
}

function NavButton({ id, label, active, onClick }) {
  const Icon = iconMap[id] || Activity;
  return (
    <button className={`nav-item ${active ? "active" : ""}`} onClick={onClick}>
      <Icon size={18} />
      <span>{label}</span>
    </button>
  );
}

function CurrentView({ route, context }) {
  if (route === "dashboard") return <Dashboard context={context} />;
  if (route === "medico/agenda") {
    return context.role === "MEDICO" ? <MedicoAgenda context={context} /> : <ErrorBox message="Tu rol no tiene acceso a esta vista." />;
  }
  if (route.startsWith("medico/consulta/")) {
    const citaId = route.split("/")[2];
    return context.role === "MEDICO" ? <MedicoConsultaDetalle context={context} citaId={citaId} /> : <ErrorBox message="Tu rol no tiene acceso a esta vista." />;
  }
  if (route.startsWith("medico/expediente/")) {
    const pacienteId = route.split("/")[2];
    return context.role === "MEDICO" ? <MedicoExpedientePaciente context={context} pacienteId={pacienteId} /> : <ErrorBox message="Tu rol no tiene acceso a esta vista." />;
  }
  if (route === "medico/diagnosticos-adjuntos") {
    return context.role === "MEDICO" ? <MedicoDiagnosticosAdjuntos context={context} /> : <ErrorBox message="Tu rol no tiene acceso a esta vista." />;
  }
  if (route === "ia") return <Chat context={context} />;
  if (route === "settings") return <SettingsView context={context} />;
  if (route.startsWith("modulo/")) {
    const key = route.split("/")[1];
    const config = modules[key];
    if (!config?.roles.includes(context.role)) return <ErrorBox message="Tu rol no tiene acceso a este modulo." />;
    return <ModuleView moduleKey={key} config={config} context={context} />;
  }
  return <Dashboard context={context} />;
}

function Dashboard({ context }) {
  return context.role === "MEDICO" ? <MedicoDashboard context={context} /> : <OperationalDashboard context={context} />;
}

function OperationalDashboard({ context }) {
  const [data, setData] = React.useState(null);
  const [error, setError] = React.useState("");

  React.useEffect(() => {
    let active = true;
    async function load() {
      try {
        const [pacientes, medicos, citas, pagos] = await Promise.all([
          context.role === "PACIENTE" ? [] : context.list(`/api/pacientes/organizacion/${context.orgId}`, true),
          context.role === "PACIENTE" ? [] : context.list(`/api/medicos/organizacion/${context.orgId}`, true),
          context.role === "PACIENTE"
            ? []
            : context.list(context.role === "MEDICO" && context.medicoId ? `/api/citas/medico/${context.medicoId}` : `/api/citas/organizacion/${context.orgId}`, true),
          context.role === "PACIENTE" ? [] : context.list(`/api/pagos/organizacion/${context.orgId}`, true),
        ]);
        if (active) setData({ pacientes, medicos, citas, pagos });
      } catch (err) {
        if (active) setError(err.message);
      }
    }
    load();
    return () => {
      active = false;
    };
  }, [context]);

  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading label="Cargando resumen operativo..." />;

  const unconfirmed = data.citas.filter((cita) => cita.estado === "SIN_CONFIRMAR").length;
  const pendingPayments = data.pagos.filter((pago) => pago.estado === "PENDIENTE").length;

  return (
    <>
      <ViewHeader title="Panel de Control" subtitle={`Resumen operativo conectado a la organizacion ${shortId(context.orgId)}.`}>
        <button className="btn primary" onClick={() => context.go("modulo/citas")}>
          Nueva cita
        </button>
      </ViewHeader>
      <section className="metrics">
        <Metric label="Citas" value={data.citas.length} />
        <Metric label="Medicos activos" value={data.medicos.filter((item) => item.activo !== false).length} variant="secondary" />
        <Metric label="Pagos pendientes" value={pendingPayments} variant="warning" />
        <Metric label="Sin confirmar" value={unconfirmed} variant="danger" />
      </section>
      <Section title="Proximas citas">
        <DataTable rows={data.citas.slice(0, 6)} columns={modules.citas.columns} support={data} />
      </Section>
    </>
  );
}

function ModuleView({ moduleKey, config, context }) {
  const [support, setSupport] = React.useState({});
  const [filters, setFilters] = React.useState({});
  const [rows, setRows] = React.useState([]);
  const [formState, setFormState] = React.useState(null);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState("");

  const supportNames = React.useMemo(() => requiredSupport(config, context.role), [config, context.role]);

  const loadSupport = React.useCallback(async () => {
    const entries = await Promise.all(
      supportNames.map(async (name) => {
        const endpoint = supportEndpoint(name, context);
        return [name, endpoint ? await context.list(endpoint, true) : []];
      })
    );
    return Object.fromEntries(entries);
  }, [context, supportNames]);

  const loadRows = React.useCallback(
    async (nextFilters = filters, nextSupport = support) => {
      setLoading(true);
      setError("");
      try {
        const path = config.list({ orgId: context.orgId, filters: nextFilters, role: context.role, medicoId: context.medicoId });
        const loadedRows = path ? await context.list(path) : [];
        setRows(config.clientFilter ? config.clientFilter(loadedRows, nextFilters, context.role) : loadedRows);
        setSupport(nextSupport);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    },
    [config, context, filters, support]
  );

  React.useEffect(() => {
    let active = true;
    async function boot() {
      setLoading(true);
      const loadedSupport = await loadSupport();
      const defaults = { ...filters };
      for (const filter of config.filters || []) {
        if (filter.required && !defaults[filter.key] && loadedSupport[filter.source]?.[0]?.id) {
          defaults[filter.key] = loadedSupport[filter.source][0].id;
        }
      }
      if (!active) return;
      setSupport(loadedSupport);
      setFilters(defaults);
      await loadRows(defaults, loadedSupport);
    }
    boot();
    return () => {
      active = false;
    };
  }, [moduleKey]);

  async function saveEntity(payload) {
    try {
      const editing = formState?.mode === "edit";
      await context.api(editing ? `${config.endpoint}/${formState.item.id}` : config.endpoint, {
        method: editing ? "PUT" : "POST",
        body: JSON.stringify(payload),
      });
      context.notify(editing ? "Registro actualizado" : "Registro creado");
      setFormState(null);
      await loadRows();
    } catch (err) {
      context.notify(err.message, "error");
    }
  }

  async function deleteEntity(id) {
    if (!window.confirm("Eliminar este registro?")) return;
    try {
      await context.api(`${config.endpoint}/${id}`, { method: "DELETE" });
      context.notify("Registro eliminado");
      await loadRows();
    } catch (err) {
      context.notify(err.message, "error");
    }
  }

  return (
    <>
      <ViewHeader title={config.label} subtitle={config.description}>
        <button className="btn" onClick={() => loadRows()}>
          <RefreshCw size={16} /> Actualizar
        </button>
        <button className="btn primary" onClick={() => setFormState({ mode: "new", item: {} })}>
          Nuevo
        </button>
      </ViewHeader>
      <FilterBar config={config} filters={filters} support={support} role={context.role} onApply={(next) => { setFilters(next); loadRows(next); }} />
      <div className="split">
        <Section title={loading ? "Cargando..." : `${rows.length} registros`} badge={context.role}>
          {error ? (
            <ErrorBox message={error} />
          ) : (
            <DataTable
              rows={rows}
              columns={config.columns}
              support={support}
              onOpen={moduleKey === "pacientes" && context.role === "MEDICO" ? (item) => context.go(`medico/expediente/${item.id}`) : null}
              onEdit={(item) => setFormState({ mode: "edit", item })}
              onDelete={deleteEntity}
            />
          )}
        </Section>
        <Section title={formState?.mode === "edit" ? "Editar" : "Captura"}>
          {formState ? (
            <EntityForm config={config} item={formState.item} context={context} support={support} onCancel={() => setFormState(null)} onSubmit={saveEntity} />
          ) : (
            <Empty label="Selecciona Nuevo o Editar para capturar datos." />
          )}
        </Section>
      </div>
    </>
  );
}

function FilterBar({ config, filters, support, role, onApply }) {
  const fields = visibleFields(config.filters, role);
  if (!fields.length) return null;
  return (
    <section className="section filter-section">
      <form
        key={JSON.stringify(filters)}
        className="filters"
        onSubmit={(event) => {
          event.preventDefault();
          onApply(formPayload(event.currentTarget, fields, {}, support, true));
        }}
      >
        {fields.map((field) => (
          <FormControl key={field.key} field={field} defaultValue={filters[field.key]} support={support} />
        ))}
        <div className="actions">
          <button className="btn" type="button" onClick={() => onApply({})}>
            Limpiar
          </button>
          <button className="btn primary">Aplicar</button>
        </div>
      </form>
    </section>
  );
}

function EntityForm({ config, item, context, support, onCancel, onSubmit }) {
  const fields = visibleFields(config.fields, context.role);
  return (
    <form
      className="form-grid"
      onSubmit={(event) => {
        event.preventDefault();
        const payload = formPayload(event.currentTarget, fields, context, support);
        onSubmit(config.transformPayload ? config.transformPayload(payload) : payload);
      }}
    >
      {fields.map((field) => (
        <FormControl key={field.key} field={field} defaultValue={fieldValue(field, item, context)} support={support} />
      ))}
      <div className="actions full">
        <button className="btn" type="button" onClick={onCancel}>
          <X size={16} /> Cancelar
        </button>
        <button className="btn primary">
          <Save size={16} /> Guardar
        </button>
      </div>
    </form>
  );
}

function FormControl({ field, defaultValue, support }) {
  if (field.type === "hiddenOrg" || field.type === "hiddenUser" || field.type === "hiddenMedico") {
    return <input type="hidden" name={field.key} defaultValue={defaultValue || ""} />;
  }

  const label = field.label || field.key;
  const className = `field ${field.full ? "full" : ""}`;

  if (field.type === "textarea") {
    return (
      <label className={className}>
        <span>{label}</span>
        <textarea name={field.key} defaultValue={defaultValue || ""} required={field.required} />
      </label>
    );
  }

  if (field.type === "checkbox") {
    return (
      <label className={`${className} checkbox-field`}>
        <input name={field.key} type="checkbox" value="true" defaultChecked={defaultValue === true || defaultValue === "true"} />
        <span>{label}</span>
      </label>
    );
  }

  if (field.source) {
    const rows = support[field.source] || [];
    if (!rows.length) {
      return (
        <label className={className}>
          <span>{label}</span>
          <input name={field.key} defaultValue={defaultValue || ""} placeholder={`UUID de ${label.toLowerCase()}`} required={field.required} />
          <small>No hay registros cargados; puedes pegar el UUID manualmente.</small>
        </label>
      );
    }
    return (
      <label className={className}>
        <span>{label}</span>
        <select name={field.key} defaultValue={defaultValue || ""} required={field.required}>
          <option value="">Selecciona...</option>
          {rows.map((row) => (
            <option key={row.id} value={row.id}>
              {displayName(row, field.source, support)}
            </option>
          ))}
        </select>
      </label>
    );
  }

  if (field.options) {
    return (
      <label className={className}>
        <span>{label}</span>
        <select name={field.key} defaultValue={defaultValue || ""} required={field.required}>
          {field.options.map((option) => (
            <option key={option || "empty"} value={option}>
              {option || "Todos"}
            </option>
          ))}
        </select>
      </label>
    );
  }

  return (
    <label className={className}>
      <span>{label}</span>
      <input name={field.key} type={field.type || "text"} defaultValue={defaultValue || ""} required={field.required} />
    </label>
  );
}

function Chat({ context }) {
  const [messages, setMessages] = React.useState([
    { role: "assistant", text: "Hola, soy el asistente de MedInFlow.\n\n¿Con qué especialidad médica te puedo ayudar hoy?" },
  ]);
  const [loading, setLoading] = React.useState(false);
  const bottomRef = React.useRef(null);

  React.useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  async function submit(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const text = String(form.get("mensaje") || "").trim();
    if (!text) return;
    event.currentTarget.reset();
    setMessages((items) => [...items, { role: "user", text }]);
    setLoading(true);
    try {
      const historial = messages.map((m) => ({
        role: m.role === "ai" ? "assistant" : m.role,
        content: m.text,
      }));
      const result = await context.api("/api/ia/chat", {
        method: "POST",
        body: JSON.stringify({ mensaje: text, historial, organizacion_id: context.orgId }),
      });
      const respuesta = typeof result === "string" ? result : (result?.respuesta ?? "Sin respuesta");
      setMessages((items) => [...items, { role: "assistant", text: respuesta }]);
    } catch (err) {
      console.error("[MedInFlow IA]", err);
      const fallbacks = [
        "Hola, disculpa la interrupción. En este momento estoy atendiendo varias solicitudes a la vez.\n\nPor favor intenta nuevamente en unos segundos, con gusto te ayudo.",
        "Hola, disculpa. En este momento tengo un pequeño problema de conexión.\n\nPor favor intenta nuevamente en un momento, estaré disponible para ayudarte.",
        "Hola, perdona el inconveniente. Parece que estoy experimentando una falla temporal.\n\nIntenta de nuevo en unos instantes, no tardará.",
        "Hola, disculpa la demora. Estoy procesando varias solicitudes al mismo tiempo.\n\nGracias por tu paciencia, en un momento estaré contigo.",
      ];
      const msg = fallbacks[Math.floor(Math.random() * fallbacks.length)];
      setMessages((items) => [...items, { role: "assistant", text: msg }]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <ViewHeader title="Asistente de Citas" subtitle="Recepcionista virtual de MedInFlow." />
      <section className="chat-shell">
        <aside className="chat-list">
          <span className="badge info">MedInFlow IA</span>
          <h3>Conversación activa</h3>
          <p className="muted">Puedo ayudarte a agendar tu cita médica.</p>
        </aside>
        <div className="chat-thread">
          <div className="messages">
            {messages.map((message, index) => (
              <div
                key={index}
                className={`message ${message.role === "assistant" ? "ai" : "user"}`}
                style={{ whiteSpace: "pre-wrap" }}
              >
                {message.text}
              </div>
            ))}
            {loading && (
              <div className="message ai" style={{ whiteSpace: "pre-wrap" }}>
                Pensando...
              </div>
            )}
            <div ref={bottomRef} />
          </div>
          <form className="chat-form" onSubmit={submit}>
            <input name="mensaje" placeholder="Escribe tu mensaje..." autoComplete="off" required />
            <button className="btn primary">Enviar</button>
          </form>
        </div>
      </section>
    </>
  );
}

function SettingsView({ context }) {
  const [value, setValue] = React.useState(context.apiUrl);
  return (
    <>
      <ViewHeader title="Ajustes" subtitle="Configuracion local del cliente web." />
      <Section>
        <form
          className="form-grid"
          onSubmit={(event) => {
            event.preventDefault();
            context.setApiUrl(value);
            context.notify("Ajustes guardados");
          }}
        >
          <label className="field full">
            <span>API Spring Boot</span>
            <input value={value} onChange={(event) => setValue(event.target.value)} required />
          </label>
          <label className="field">
            <span>Rol</span>
            <input value={context.role} disabled />
          </label>
          <label className="field">
            <span>Organizacion</span>
            <input value={context.orgId} disabled />
          </label>
          <label className="field full">
            <span>Token</span>
            <textarea value={context.session.token || ""} disabled />
          </label>
          <div className="actions full">
            <button className="btn primary">Guardar ajustes</button>
          </div>
        </form>
      </Section>
    </>
  );
}

function DataTable({ rows, columns, support = {}, onOpen, onEdit, onDelete }) {
  if (!rows?.length) return <Empty label="No hay registros para mostrar." />;
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map(([, label]) => (
              <th key={label}>{label}</th>
            ))}
            {(onOpen || onEdit || onDelete) && <th className="right">Acciones</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              {columns.map(([key, , type]) => (
                <td key={key}>{formatCell(row[key], type, support)}</td>
              ))}
              {(onOpen || onEdit || onDelete) && (
                <td>
                  <div className="row-actions">
                    {onOpen && (
                      <button className="btn icon" onClick={() => onOpen(row)} title="Abrir expediente">
                        <FileText size={15} />
                      </button>
                    )}
                    {onEdit && (
                      <button className="btn icon" onClick={() => onEdit(row)} title="Editar">
                        <Save size={15} />
                      </button>
                    )}
                    {onDelete && (
                      <button className="btn icon danger" onClick={() => onDelete(row.id)} title="Eliminar">
                        <Trash2 size={15} />
                      </button>
                    )}
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Field({ label, name, ...props }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input name={name} {...props} />
    </label>
  );
}

function Toast({ toast }) {
  if (!toast) return null;
  return <div className={`toast ${toast.kind === "error" ? "error" : ""}`}>{toast.message}</div>;
}

function supportEndpoint(name, context) {
  const map = {
    pacientes: `/api/pacientes/organizacion/${context.orgId}`,
    medicos: `/api/medicos/organizacion/${context.orgId}`,
    consultorios: `/api/consultorios/organizacion/${context.orgId}`,
    citas: context.role === "MEDICO" && context.medicoId ? `/api/citas/medico/${context.medicoId}` : `/api/citas/organizacion/${context.orgId}`,
    usuarios: `/api/usuarios/organizacion/${context.orgId}`,
  };
  return map[name];
}

function visibleFields(fields = [], role) {
  return fields.filter((field) => !field.roles || field.roles.includes(role));
}

function requiredSupport(config, role) {
  const names = new Set();
  [...visibleFields(config.fields, role), ...visibleFields(config.filters, role)].forEach((field) => {
    if (field.source && field.source !== "adjuntos") names.add(field.source);
  });
  (config.columns || []).forEach(([, , type]) => {
    if (type === "patient") names.add("pacientes");
    if (type === "doctor") names.add("medicos");
  });
  return [...names];
}

function formPayload(form, fields, context = {}, support = {}, keepEmpty = false) {
  const data = new FormData(form);
  const payload = {};
  fields.forEach((field) => {
    let value = data.get(field.key);
    if (field.type === "hiddenOrg") value = context.orgId;
    if (field.type === "hiddenUser") value = context.userId;
    if (field.type === "hiddenMedico") value = context.medicoId;
    if (field.type === "checkbox") value = value === "true";
    if (field.type === "number" && value !== "") value = Number(value);
    if (field.type === "datetime-local" && value) value = new Date(value).toISOString();
    if (!keepEmpty && !field.required) value = requestValue(value);
    if (value !== undefined) payload[field.key] = value;
  });
  return payload;
}

function fieldValue(field, item, context) {
  if (field.value) return field.value(item || {});
  if (field.type === "hiddenOrg") return context.orgId;
  if (field.type === "hiddenUser") return context.userId;
  if (field.type === "hiddenMedico") return context.medicoId;
  if (field.type === "datetime-local") return toDatetimeInput(item[field.key]);
  return item[field.key] ?? "";
}

function patientNamePart(value, index) {
  const parts = String(value || "").trim().split(/\s+/).filter(Boolean);
  if (index === 0) return parts[0] || "";
  if (index === 1) return parts[1] || "";
  return parts.slice(2).join(" ");
}

function toDatetimeInput(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60 * 1000);
  return local.toISOString().slice(0, 16);
}

createRoot(document.getElementById("app")).render(<App />);
