# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Compilar y empaquetar
./mvnw clean package

# Correr la aplicación
./mvnw spring-boot:run

# Correr todos los tests
./mvnw test

# Correr un test específico
./mvnw test -Dtest=NombreDeClaseTest

# Correr migraciones Flyway manualmente
./mvnw flyway:migrate
```

La base de datos debe estar corriendo en `localhost:5432` con la base `MVPClinica` (usuario `postgres`) antes de iniciar la aplicación o ejecutar migraciones.

## Arquitectura

**MedInFlow MVP** es una API REST de gestión de clínicas construida con Spring Boot 4.0.5 y Java 21.

### Stack
- Spring Boot WebMVC + Spring Data JPA (sin Spring Security filter chain — solo BCrypt para hashing de contraseñas)
- PostgreSQL como base de datos; Flyway gestiona el esquema (`ddl-auto=none`)
- Jakarta Validation en los DTOs de request
- Sin Lombok ni MapStruct — getters/setters manuales y mapeo manual en cada Service

### Capas
```
Controller → Service → Repository (JpaRepository) → Entidad JPA
```
- Los **Controllers** reciben y devuelven DTOs (`*Request` / `*Response`); nunca exponen entidades.
- Los **Services** resuelven FKs manualmente (con `orElseThrow(RuntimeException::new)`) y hacen el mapeo `toResponse()` internamente.
- No hay interfaces de Service — cada servicio es una clase `@Service` concreta inyectada por constructor.

### Multi-tenancy
**Todas las entidades** (excepto `Diagnostico`, que hereda el scope a través de `Cita`) contienen `organizacion_id`. Las queries del repositorio siempre filtran por `organizacionId` cuando listan registros.

### Modelo de dominio
```
Organizacion (plan: SOLO | CLINICA | ENTERPRISE)
 ├── Consultorio
 ├── Medico  →  Consultorio
 │    └── HorarioMedico (dia_semana 0-6, duracion_consulta: 20|30|45|60 min)
 ├── Paciente
 ├── Usuario (rol: ADMIN | MEDICO, vinculado opcionalmente a Medico)
 └── Cita  →  Paciente + Medico + Consultorio
      ├── estado: SIN_CONFIRMAR | CONFIRMADA | CANCELADA | REAGENDADA | NO_ASISTIO
      ├── cancelada_por: PACIENTE | ADMIN | MEDICO
      ├── Pago (metodo: EFECTIVO | TRANSFERENCIA | TARJETA; estado: PAGADO | PENDIENTE)
      ├── Diagnostico (tipo: PRINCIPAL | SECUNDARIO; codigo_cie10 opcional)
      ├── Adjunto (subido_por → Usuario)
      └── Notificacion (canal: WHATSAPP; tipo: RECORDATORIO_48H | RECORDATORIO_24H | ADJUNTO)
```

### Migraciones
El esquema completo está en `src/main/resources/db/migration/V1__medinflow_mvp.sql`. Al agregar tablas o columnas se debe crear un nuevo archivo `V2__...sql`, nunca modificar V1.

### Convenciones
- Todos los IDs son `UUID` generados por la BD (`gen_random_uuid()`) y por JPA (`GenerationType.UUID`).
- Los timestamps usan `OffsetDateTime` (mapea a `TIMESTAMP WITH TIME ZONE` en Postgres).
- `createdAt` se inicializa en el constructor Java (`= OffsetDateTime.now()`) y es `updatable = false`.
- Las relaciones `@ManyToOne` usan `FetchType.LAZY`.

## Contexto adicional

@.claude/medinflow-vision-SKILL.md
@.claude/medinflow-springboot-SKILL.md
