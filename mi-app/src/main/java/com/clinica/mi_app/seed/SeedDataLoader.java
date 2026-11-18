package com.clinica.mi_app.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

/**
 * Carga datos realistas de una clínica para desarrollo local (app.seed.enabled=true).
 *
 * - Idempotente: si ya existe el usuario admin seed no hace nada.
 * - Las fechas se generan relativas a HOY, así la agenda y el dashboard siempre tienen
 *   citas pasadas, del día y próximas.
 * - Convención de horas del sistema: la hora de consulta se guarda como "hora de reloj"
 *   de la clínica en UTC (10:00 -> 10:00Z), igual que HorarioMedico y CitaService.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);
    public static final String PASSWORD = "MedInFlow2026!";
    public static final String ADMIN_EMAIL = "admin@clinicasantalucia.mx";
    private static final ZoneId CLINICA_TZ = ZoneId.of("America/Mexico_City");

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Random rnd = new Random(20261110L);

    public SeedDataLoader(JdbcTemplate jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cargar() {
        Integer existe = jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE email = ?", Integer.class, ADMIN_EMAIL);
        if (existe != null && existe > 0) {
            log.info("Seed: datos ya cargados, se omite");
            return;
        }
        tx.executeWithoutResult(s -> generar());
    }

    // ---------------------------------------------------------------- catálogos
    private record Doc(String nombre, String especialidad, String cedula, String tel, BigDecimal tarifa, String email,
                       String[] motivos, String[][] cie10) {}

    private static final Doc[] DOCS = {
        new Doc("Dra. Mariana López Ruiz", "Medicina General", "8456123", "9515550101", new BigDecimal("550.00"),
                "mariana.lopez@clinicasantalucia.mx",
                new String[]{"Dolor de garganta y fiebre", "Control de presión arterial", "Dolor abdominal", "Chequeo general anual",
                        "Tos persistente", "Revisión de resultados de laboratorio", "Dolor de cabeza frecuente", "Control de diabetes"},
                new String[][]{{"J02.9", "Faringitis aguda, no especificada"}, {"I10", "Hipertensión esencial (primaria)"},
                        {"K29.7", "Gastritis, no especificada"}, {"Z00.0", "Examen médico general"}, {"J20.9", "Bronquitis aguda, no especificada"},
                        {"E11.9", "Diabetes mellitus tipo 2 sin complicaciones"}, {"R51", "Cefalea"}, {"J06.9", "Infección aguda de vías respiratorias superiores"}}),
        new Doc("Dr. Carlos Hernández Vásquez", "Pediatría", "9123874", "9515550102", new BigDecimal("650.00"),
                "carlos.hernandez@clinicasantalucia.mx",
                new String[]{"Control de niño sano", "Fiebre y mocos", "Vacunación", "Diarrea", "Revisión de crecimiento", "Dolor de oído", "Erupción en la piel"},
                new String[][]{{"Z00.1", "Control de salud de rutina del niño"}, {"J00", "Rinofaringitis aguda (resfriado común)"},
                        {"Z23", "Necesidad de inmunización"}, {"A09", "Diarrea y gastroenteritis de presunto origen infeccioso"},
                        {"H66.9", "Otitis media, no especificada"}, {"L20.9", "Dermatitis atópica, no especificada"}}),
        new Doc("Dra. Sofía Martínez Cruz", "Nutrición y Endocrinología", "10234567", "9515550103", new BigDecimal("700.00"),
                "sofia.martinez@clinicasantalucia.mx",
                new String[]{"Plan de alimentación", "Control de peso", "Seguimiento de tiroides", "Valoración nutricional", "Resistencia a la insulina"},
                new String[][]{{"E66.9", "Obesidad, no especificada"}, {"E03.9", "Hipotiroidismo, no especificado"},
                        {"E78.5", "Hiperlipidemia, no especificada"}, {"R73.0", "Anormalidades en la prueba de tolerancia a la glucosa"},
                        {"E55.9", "Deficiencia de vitamina D, no especificada"}}),
    };

    private static final String[] NOMBRES_F = {"Ana", "María José", "Guadalupe", "Fernanda", "Valeria", "Daniela", "Lucía",
            "Ximena", "Paola", "Rosa", "Carmen", "Isabel", "Andrea", "Natalia", "Elena"};
    private static final String[] NOMBRES_M = {"José Luis", "Miguel Ángel", "Juan Pablo", "Diego", "Alejandro", "Ricardo",
            "Fernando", "Eduardo", "Javier", "Héctor", "Mateo", "Santiago", "Emiliano", "Roberto", "Arturo"};
    private static final String[] APELLIDOS = {"García", "Ramírez", "Santiago", "Jiménez", "Pérez", "Cruz", "Mendoza",
            "Vásquez", "Ruiz", "Morales", "Ortiz", "Castillo", "Reyes", "Aquino", "Gómez", "Hernández", "Martínez",
            "López", "Sánchez", "Bautista", "Luna", "Velasco", "Toledo", "Matus"};

    // ---------------------------------------------------------------- generación
    private void generar() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = enc.encode(PASSWORD);

        UUID org = UUID.randomUUID();
        jdbc.update("INSERT INTO organizacion (id, nombre, plan, trial_hasta, activo) VALUES (?,?,?,?,true)",
                org, "Clínica Santa Lucía", "CLINICA", Timestamp.from(Instant.now().plus(Duration.ofDays(21))));
        jdbc.update("INSERT INTO configuracion_tenant (organizacion_id, whatsapp_conectado, whatsapp_numero) VALUES (?,true,?)",
                org, "9515550100");

        UUID cons1 = UUID.randomUUID(), cons2 = UUID.randomUUID();
        jdbc.update("INSERT INTO consultorio (id, organizacion_id, nombre, direccion, telefono) VALUES (?,?,?,?,?)",
                cons1, org, "Consultorio 1 · Planta baja", "Av. Independencia 812, Centro, Oaxaca de Juárez", "9515550111");
        jdbc.update("INSERT INTO consultorio (id, organizacion_id, nombre, direccion, telefono) VALUES (?,?,?,?,?)",
                cons2, org, "Consultorio 2 · Primer piso", "Av. Independencia 812, Centro, Oaxaca de Juárez", "9515550112");

        jdbc.update("INSERT INTO usuario (organizacion_id, email, rol, password_hash) VALUES (?,?,?,?)",
                org, ADMIN_EMAIL, "ADMIN", hash);

        UUID[] medicos = new UUID[DOCS.length];
        UUID[] consDe = {cons1, cons2, cons1};
        for (int i = 0; i < DOCS.length; i++) {
            Doc d = DOCS[i];
            medicos[i] = UUID.randomUUID();
            jdbc.update("INSERT INTO medico (id, organizacion_id, consultorio_id, nombre, especialidad, cedula, telefono, tarifa_base) VALUES (?,?,?,?,?,?,?,?)",
                    medicos[i], org, consDe[i], d.nombre(), d.especialidad(), d.cedula(), d.tel(), d.tarifa());
            jdbc.update("INSERT INTO usuario (organizacion_id, medico_id, email, rol, password_hash) VALUES (?,?,?,?,?)",
                    org, medicos[i], d.email(), "MEDICO", hash);
        }

        // Horarios (dia_semana: 0=Dom ... 6=Sab)
        for (int i = 0; i < medicos.length; i++) {
            for (int dia = 1; dia <= 5; dia++) {
                horario(medicos[i], dia, "09:00", "14:00", 30);
                if (i == 0 && (dia == 2 || dia == 4)) horario(medicos[i], dia, "16:00", "19:00", 30);
                if (i == 2 && (dia == 1 || dia == 3)) horario(medicos[i], dia, "16:00", "19:00", 30);
            }
        }
        horario(medicos[1], 6, "09:00", "13:00", 30); // pediatría también sábado

        // Pacientes
        List<UUID> pacientes = new ArrayList<>();
        List<String> telefonos = new ArrayList<>();
        Set<String> usados = new HashSet<>();
        // Paciente fijo con acceso al portal
        UUID pacienteAna = UUID.randomUUID();
        jdbc.update("INSERT INTO paciente (id, organizacion_id, nombre, telefono, fecha_nacimiento, sexo, email, notas) VALUES (?,?,?,?,?,?,?,?)",
                pacienteAna, org, "Ana Sofía Ramírez Mendoza", "9512345678", java.sql.Date.valueOf(LocalDate.of(1994, 3, 18)), "F",
                "ana.ramirez@correo.mx", "Alergia a penicilina.");
        jdbc.update("INSERT INTO usuario (organizacion_id, email, rol, password_hash) VALUES (?,?,?,?)",
                org, "ana.ramirez@correo.mx", "PACIENTE", hash);
        pacientes.add(pacienteAna);
        telefonos.add("9512345678");
        usados.add("Ana Sofía Ramírez Mendoza");

        while (pacientes.size() < 32) {
            boolean mujer = rnd.nextBoolean();
            String nom = pick(mujer ? NOMBRES_F : NOMBRES_M) + " " + pick(APELLIDOS) + " " + pick(APELLIDOS);
            if (!usados.add(nom)) continue;
            UUID id = UUID.randomUUID();
            String tel = "951" + (1000000 + rnd.nextInt(8999999));
            LocalDate nac = LocalDate.now().minusYears(rnd.nextInt(pacientes.size() % 4 == 0 ? 12 : 70) + 1).minusDays(rnd.nextInt(365));
            String email = slug(nom) + "@correo.mx";
            jdbc.update("INSERT INTO paciente (id, organizacion_id, nombre, telefono, fecha_nacimiento, sexo, email) VALUES (?,?,?,?,?,?,?)",
                    id, org, nom, tel, java.sql.Date.valueOf(nac), mujer ? "F" : "M", email);
            pacientes.add(id);
            telefonos.add(tel);
        }

        // Citas: de hace 4 semanas a 2 semanas adelante
        LocalDate hoy = LocalDate.now(CLINICA_TZ);
        LocalDateTime ahoraReloj = LocalDateTime.now(CLINICA_TZ);
        int totalCitas = 0;
        boolean anaHoyProgramada = false;
        for (int off = -28; off <= 14; off++) {
            LocalDate dia = hoy.plusDays(off);
            int dow = dia.getDayOfWeek().getValue() % 7;
            if (dow == 0) continue;
            for (int m = 0; m < medicos.length; m++) {
                if (dow == 6 && m != 1) continue;
                List<LocalTime> slots = slotsDelDia(m, dow);
                double ocupacion = off < 0 ? 0.62 : (off == 0 ? 0.7 : Math.max(0.15, 0.55 - off * 0.03));
                for (LocalTime t : slots) {
                    if (rnd.nextDouble() > ocupacion) continue;
                    int p = 1 + rnd.nextInt(pacientes.size() - 1);
                    // Ana: una cita próxima sin confirmar (para demostrar WhatsApp) y algunas pasadas
                    if (!anaHoyProgramada && m == 0 && off == 1) { p = 0; anaHoyProgramada = true; }
                    else if (m == 0 && off < 0 && rnd.nextDouble() < 0.06) p = 0;
                    LocalDateTime inicio = LocalDateTime.of(dia, t);
                    boolean pasada = inicio.isBefore(ahoraReloj);
                    String estado = estadoPara(pasada, off, p == 0 && off == 1);
                    crearCita(org, pacientes.get(p), medicos[m], consDe[m], m, inicio, estado, pasada);
                    totalCitas++;
                }
            }
        }
        log.info("Seed: Clínica Santa Lucía cargada — {} médicos, {} pacientes, {} citas. Contraseña: {}",
                medicos.length, pacientes.size(), totalCitas, PASSWORD);
    }

    private String estadoPara(boolean pasada, int off, boolean forzarSinConfirmar) {
        if (forzarSinConfirmar) return "SIN_CONFIRMAR";
        double r = rnd.nextDouble();
        if (pasada) {
            if (r < 0.80) return "CONFIRMADA";
            if (r < 0.89) return "CANCELADA";
            return "NO_ASISTIO";
        }
        if (off <= 2) {
            if (r < 0.55) return "CONFIRMADA";
            if (r < 0.92) return "SIN_CONFIRMAR";
            return "CANCELADA";
        }
        if (r < 0.30) return "CONFIRMADA";
        if (r < 0.95) return "SIN_CONFIRMAR";
        return "CANCELADA";
    }

    private void crearCita(UUID org, UUID paciente, UUID medico, UUID consultorio, int m,
                           LocalDateTime inicio, String estado, boolean pasada) {
        Doc d = DOCS[m];
        UUID cita = UUID.randomUUID();
        OffsetDateTime fh = inicio.atOffset(ZoneOffset.UTC);
        String canceladaPor = null;
        Timestamp canceladaEn = null;
        if ("CANCELADA".equals(estado)) {
            canceladaPor = rnd.nextDouble() < 0.7 ? "PACIENTE" : "ADMIN";
            canceladaEn = Timestamp.from(fh.minusHours(6 + rnd.nextInt(40)).toInstant());
        }
        jdbc.update("INSERT INTO cita (id, organizacion_id, paciente_id, medico_id, consultorio_id, fecha_hora, duracion_min, estado, motivo, cancelada_en, cancelada_por, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                cita, org, paciente, medico, consultorio, Timestamp.from(fh.toInstant()), 30, estado, pick(d.motivos()),
                canceladaEn, canceladaPor, Timestamp.from(fh.minusDays(2 + rnd.nextInt(10)).toInstant()));

        // Recordatorios ya enviados para citas próximas o recientes
        if (!"CANCELADA".equals(estado) && fh.isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusDays(3))) {
            notif(org, cita, "RECORDATORIO_48H", "ENVIADO", fh.minusHours(48), null);
            if ("CONFIRMADA".equals(estado)) notif(org, cita, "RECORDATORIO_24H", "ENVIADO", fh.minusHours(24), "1");
        }
        if ("CANCELADA".equals(estado) && "PACIENTE".equals(canceladaPor)) {
            notif(org, cita, "CANCELACION_PACIENTE", "ENVIADO", fh.minusHours(20), "2");
        }

        if (pasada && "CONFIRMADA".equals(estado)) {
            // Pago de la consulta
            double r = rnd.nextDouble();
            String metodo = r < 0.45 ? "EFECTIVO" : (r < 0.75 ? "TARJETA" : "TRANSFERENCIA");
            boolean pagado = rnd.nextDouble() < 0.9;
            jdbc.update("INSERT INTO pago (organizacion_id, cita_id, monto, metodo, concepto, estado, referencia, pagado_en) VALUES (?,?,?,?,?,?,?,?)",
                    org, cita, d.tarifa(), metodo, "Consulta · " + d.especialidad(), pagado ? "PAGADO" : "PENDIENTE",
                    "TRANSFERENCIA".equals(metodo) ? "SPEI-" + (100000 + rnd.nextInt(899999)) : null,
                    pagado ? Timestamp.from(fh.plusMinutes(35).toInstant()) : null);
            // Diagnóstico CIE-10
            String[] dx = d.cie10()[rnd.nextInt(d.cie10().length)];
            jdbc.update("INSERT INTO diagnostico (cita_id, codigo_cie10, descripcion, tipo, created_at) VALUES (?,?,?,?,?)",
                    cita, dx[0], dx[1], "PRINCIPAL", Timestamp.from(fh.plusMinutes(25).toInstant()));
            if (rnd.nextDouble() < 0.25) {
                String[] dx2 = d.cie10()[rnd.nextInt(d.cie10().length)];
                if (!dx2[0].equals(dx[0])) {
                    jdbc.update("INSERT INTO diagnostico (cita_id, codigo_cie10, descripcion, tipo, created_at) VALUES (?,?,?,?,?)",
                            cita, dx2[0], dx2[1], "SECUNDARIO", Timestamp.from(fh.plusMinutes(26).toInstant()));
                }
            }
        }
    }

    private void notif(UUID org, UUID cita, String tipo, String estado, OffsetDateTime enviada, String respuesta) {
        jdbc.update("INSERT INTO notificacion (organizacion_id, cita_id, canal, tipo, estado, enviada_en, respuesta) VALUES (?,?,?,?,?,?,?)",
                org, cita, "WHATSAPP", tipo, estado, Timestamp.from(enviada.toInstant()), respuesta);
    }

    private void horario(UUID medico, int dia, String ini, String fin, int dur) {
        jdbc.update("INSERT INTO horario_medico (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta) VALUES (?,?,?,?,?)",
                medico, (short) dia, java.sql.Time.valueOf(LocalTime.parse(ini)), java.sql.Time.valueOf(LocalTime.parse(fin)), (short) dur);
    }

    private List<LocalTime> slotsDelDia(int m, int dow) {
        List<LocalTime> out = new ArrayList<>();
        LocalTime fin = dow == 6 ? LocalTime.of(13, 0) : LocalTime.of(14, 0);
        for (LocalTime t = LocalTime.of(9, 0); t.isBefore(fin); t = t.plusMinutes(30)) out.add(t);
        boolean tarde = (m == 0 && (dow == 2 || dow == 4)) || (m == 2 && (dow == 1 || dow == 3));
        if (tarde) for (LocalTime t = LocalTime.of(16, 0); t.isBefore(LocalTime.of(19, 0)); t = t.plusMinutes(30)) out.add(t);
        return out;
    }

    private <T> T pick(T[] arr) { return arr[rnd.nextInt(arr.length)]; }

    private static String slug(String s) {
        String n = java.text.Normalizer.normalize(s.toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String[] parts = n.split(" ");
        return parts[0] + "." + parts[parts.length - 2];
    }
}
