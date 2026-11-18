package com.clinica.mi_app.seed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

/**
 * El seed genera notificaciones con instantes REALES: sin fechas futuras, sin duplicados, recordatorios
 * exactamente 48/24 h antes de la cita, creada_en = enviada_en y solo unas pocas respuestas sin leer.
 */
class SeedDataLoaderTest {

    private static final ZoneId CLINICA = ZoneId.of("America/Mexico_City");
    /** Martes 17/11/2026 10:00 en la clínica (16:00Z). */
    private static final Instant AHORA = Instant.parse("2026-11-17T16:00:00Z");

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final Map<UUID, Instant> citaReal = new HashMap<>();
    private final List<Object[]> notifs = new ArrayList<>();

    @BeforeEach
    void generar() {
        new SeedDataLoader(jdbc, null, Clock.fixed(AHORA, ZoneOffset.UTC)).generar();
        for (Invocation inv : mockingDetails(jdbc).getInvocations()) {
            Object[] a = inv.getArguments();
            if (!(a.length > 0 && a[0] instanceof String sql)) continue;
            if (sql.startsWith("INSERT INTO cita ")) {
                // fecha_hora = hora de reloj de la clínica en UTC → instante real en America/Mexico_City
                LocalDateTime reloj = LocalDateTime.ofInstant(((Timestamp) a[6]).toInstant(), ZoneOffset.UTC);
                citaReal.put((UUID) a[1], reloj.atZone(CLINICA).toInstant());
            } else if (sql.startsWith("INSERT INTO notificacion ")) {
                notifs.add(a);
            }
        }
        assertFalse(citaReal.isEmpty());
        assertFalse(notifs.isEmpty());
    }

    // args: [sql, org, cita, canal, tipo, estado, enviada_en, creada_en, leida, respuesta]
    private static UUID cita(Object[] n) { return (UUID) n[2]; }
    private static String tipo(Object[] n) { return (String) n[4]; }
    private static Instant enviada(Object[] n) { return ((Timestamp) n[6]).toInstant(); }
    private static Instant creada(Object[] n) { return ((Timestamp) n[7]).toInstant(); }
    private static boolean leida(Object[] n) { return (Boolean) n[8]; }

    @Test
    void ningunaNotificacionEnElFuturo() {
        notifs.forEach(n -> assertFalse(enviada(n).isAfter(AHORA), () -> tipo(n) + " con fecha futura: " + enviada(n)));
    }

    @Test
    void sinDuplicadosPorCitaYTipo() {
        Set<String> claves = new HashSet<>();
        notifs.forEach(n -> assertTrue(claves.add(cita(n) + "|" + tipo(n)), "duplicado " + tipo(n)));
    }

    @Test
    void recordatorios_exactamente48y24HorasAntesDelMomentoRealDeLaCita() {
        long revisados = 0;
        for (Object[] n : notifs) {
            int horas = switch (tipo(n)) {
                case "RECORDATORIO_48H" -> 48;
                case "RECORDATORIO_24H" -> 24;
                default -> 0;
            };
            if (horas == 0) continue;
            assertEquals(citaReal.get(cita(n)).minus(Duration.ofHours(horas)), enviada(n));
            revisados++;
        }
        assertTrue(revisados > 0, "el seed debe incluir recordatorios");
    }

    @Test
    void ejemploDelBug_cita1030_recordatorioALas1030HoraDeLaClinica_no0430() {
        // Para cualquier cita, la hora local (clínica) del recordatorio coincide con la hora de la cita
        notifs.stream().filter(n -> tipo(n).startsWith("RECORDATORIO_")).forEach(n -> assertEquals(
                LocalDateTime.ofInstant(citaReal.get(cita(n)), CLINICA).toLocalTime(),
                LocalDateTime.ofInstant(enviada(n), CLINICA).toLocalTime()));
    }

    @Test
    void creadaEnIgualAlEnvio() {
        notifs.forEach(n -> assertEquals(enviada(n), creada(n)));
    }

    @Test
    void soloPocasRespuestasRecientesSinLeer_yLoViejoLeido() {
        List<Object[]> sinLeer = notifs.stream().filter(n -> !leida(n)).toList();
        assertFalse(sinLeer.isEmpty(), "debe haber algunas respuestas sin leer para la campana");
        assertTrue(sinLeer.size() <= SeedDataLoader.RESPUESTAS_SIN_LEER);
        sinLeer.forEach(n -> {
            assertTrue(tipo(n).endsWith("_PACIENTE"), "solo respuestas de pacientes quedan sin leer");
            assertTrue(enviada(n).isAfter(AHORA.minus(Duration.ofDays(2))));
        });
        notifs.stream().filter(n -> enviada(n).isBefore(AHORA.minus(Duration.ofDays(2))))
                .forEach(n -> assertTrue(leida(n), "más de 2 días → leída"));
    }

    @Test
    void notificacionesDeCita_reglas() {
        UUID org = UUID.randomUUID(), cita = UUID.randomUUID();
        Instant manana = AHORA.plus(Duration.ofHours(24 + 6));   // la de 24 h aún no toca
        var proxima = SeedDataLoader.notificacionesDeCita(org, cita, manana, "SIN_CONFIRMAR", null, null, AHORA, Duration.ofMinutes(30));
        assertEquals(List.of("RECORDATORIO_48H"), proxima.stream().map(SeedDataLoader.NotifSeed::tipo).toList());

        Instant enDiezDias = AHORA.plus(Duration.ofDays(10));
        assertTrue(SeedDataLoader.notificacionesDeCita(org, cita, enDiezDias, "SIN_CONFIRMAR", null, null, AHORA,
                Duration.ofMinutes(30)).isEmpty(), "sin recordatorios futuros");

        Instant hoyTarde = AHORA.plus(Duration.ofHours(5));
        var confirmada = SeedDataLoader.notificacionesDeCita(org, cita, hoyTarde, "CONFIRMADA", null, null, AHORA, Duration.ofMinutes(30));
        assertEquals(List.of("RECORDATORIO_48H", "RECORDATORIO_24H", "CONFIRMACION_PACIENTE"),
                confirmada.stream().map(SeedDataLoader.NotifSeed::tipo).toList());

        Instant canceladaEn = hoyTarde.minus(Duration.ofHours(30));  // entre el de 48 h y el de 24 h
        var cancelada = SeedDataLoader.notificacionesDeCita(org, cita, hoyTarde, "CANCELADA", "PACIENTE", canceladaEn, AHORA, Duration.ZERO);
        assertEquals(List.of("RECORDATORIO_48H", "CANCELACION_PACIENTE"),
                cancelada.stream().map(SeedDataLoader.NotifSeed::tipo).toList(), "no se recuerda una cita ya cancelada");
        assertEquals(canceladaEn, cancelada.get(1).enviada());
    }
}
