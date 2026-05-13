# MediFlow — Sistema de Gestión de Clínicas (Multi-tenant)

**Equipo 1**

### Integrantes:
* **Alonso Cruz Yahir Jibsam**
* **Martinez Lopez Fatima**
* **Santiago Jimenez Galilea**

**Stack:** Spring Boot, FastAPI (IA), React + Vite, PostgreSQL.

---
## Resumen del Proyecto
MediFlow es una solución integral diseñada para gestionar múltiples clínicas de forma independiente dentro de una misma plataforma. El sistema permite la gestión de citas médicas, procesamiento de pagos, diagnósticos especializados y cuenta con un microservicio dedicado de Inteligencia Artificial para asistencia en el dominio médico.

---

## Arquitectura y Diseño
El sistema sigue una arquitectura de **Microservicios** para separar la lógica de negocio central del procesamiento de IA.

- **Backend (Core):** Desarrollado en Java 17 con Spring Boot, utilizando una estructura de capas: `Controller` → `Service` → `Repository` → `Model`.
- **IA Module (medinflow-ia):** Microservicio independiente desarrollado en FastAPI que consume modelos de lenguaje (Groq) para diagnósticos.
- **Frontend:** SPA moderna construida con React y Vite.
- **Persistencia:** PostgreSQL con control de versiones de esquema mediante **Flyway**.

### Diagrama de Arquitectura (Conceptual)


---

## Patrones de Diseño Implementados
Para cumplir con los estándares de la rúbrica, se identifican y justifican los siguientes patrones:

1. **Data Transfer Object (DTO):** Se utilizan clases `Request` y `Response` para desacoplar las entidades de la base de datos de la capa de presentación, mejorando la seguridad y el rendimiento.
2. **Repository Pattern:** Implementado mediante Spring Data JPA para abstraer la lógica de persistencia y permitir un acceso a datos consistente.
3. **Service Layer:** Centraliza la lógica de negocio en una capa intermedia, facilitando el mantenimiento y la escalabilidad del sistema.
4. **Global Exception Handler:** Implementado mediante `@ControllerAdvice` para capturar errores de forma centralizada y retornar respuestas HTTP estandarizadas.

---

## Configuración e Instalación

### Variables de Entorno (.env)
Es necesario configurar las siguientes variables en el archivo `application.properties` o mediante variables de entorno:

**Spring Boot:**
- `SPRING_DATASOURCE_URL`: URL de conexión a PostgreSQL.
- `JWT_SECRET`: Clave secreta para la generación de tokens.
- `IA_SERVICE_URL`: Endpoint del servicio FastAPI.

**FastAPI (IA):**
- `GROQ_API_KEY`: API Key para el motor de IA.
- `APP_PORT`: Puerto de ejecución (default: 8000).

