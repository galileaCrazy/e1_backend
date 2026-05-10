# Feedback – Equipo 1: Gestión de Clínicas
**Stack:** Spring Boot · React + Vite · PostgreSQL  
**Fecha de revisión:** 22 de abril de 2025

---

## Resumen general

El equipo entrega uno de los proyectos más sólidos conceptualmente. El backend muestra un dominio claro de los temas vistos en el curso: arquitectura multicapas, separación de responsabilidades, diseño RESTful y autenticación con JWT. El enfoque multi-tenant a través de la entidad `Organizacion` es exactamente el tipo de decisión arquitectónica que se espera demostrar.

---

## Lo que están haciendo bien ✅

### Arquitectura multicapas
La estructura del proyecto refleja de forma clara la arquitectura en 3 capas:

- **Capa de presentación:** `controller/` — recibe las peticiones HTTP, valida la entrada con `@Valid` y delega al servicio. No contiene lógica de negocio.
- **Capa de negocio:** `service/` — contiene toda la lógica de la aplicación: validaciones, orquestación de entidades, reglas del dominio.
- **Capa de datos:** `repository/` + `model/` — acceso a la base de datos a través de JPA. Los repositorios no conocen nada del mundo HTTP.

Cada capa solo habla con la inmediatamente adyacente. Esto es exactamente el patrón MVC/3-capas visto en el Tema 1, bien aplicado.

### Patrón DTO (Data Transfer Object)
Usan tanto `dto/request/` como `dto/response/` para separar el modelo de dominio de lo que se expone/recibe en la API. Esto es correcto por dos razones:

1. El cliente no necesita ver la estructura interna del modelo (por ejemplo, relaciones JPA, campos internos).
2. El modelo de dominio no debe cambiar su forma para acomodarse a lo que el cliente envía.

Tener DTOs de entrada y de salida separados es una señal de madurez en el diseño.

### Diseño RESTful
Los endpoints siguen las convenciones REST correctamente:

- Recursos identificados como sustantivos plurales (`/api/citas`, `/api/pacientes`, `/api/medicos`).
- Métodos HTTP usados semánticamente: `GET` para listar/buscar, `POST` para crear, `PUT` para actualizar, `DELETE` para eliminar.
- Códigos de respuesta apropiados: `201 CREATED` al crear, `204 NO_CONTENT` al eliminar, `404` cuando no existe el recurso.
- Documentación con Swagger/OpenAPI usando `@Tag`, `@Operation` y `@ApiResponse`.

### Autenticación y autorización con JWT
Tienen un flujo completo bien estructurado:

- `JwtUtil` — genera y valida tokens.
- `JwtFilter` — middleware que intercepta cada petición y verifica el token antes de que llegue al controller.
- `@PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")` — control de acceso declarativo a nivel de endpoint.

La separación entre autenticación (¿quién eres?) y autorización (¿qué puedes hacer?) está correctamente implementada.

### Manejo global de excepciones
`GlobalExceptionHandler` + `ResourceNotFoundException` + `ErrorResponse` conforman un patrón de manejo de errores consistente en todo el sistema. Esto evita que los errores internos se propaguen como respuestas 500 genéricas y garantiza que el cliente siempre reciba un mensaje estructurado y un código HTTP apropiado.

### Multi-tenancy
La entidad `Organizacion` con campo `plan` (SOLO / CLINICA / ENTERPRISE) y flag `activo` es una implementación del concepto multi-tenant: múltiples clínicas comparten la misma infraestructura pero sus datos están lógicamente aislados por organización. Que las citas, médicos y pacientes estén siempre asociados a una organización demuestra comprensión del problema de negocio.

---

## Áreas de mejora 🔧

### README vacío
El README solo dice `# e1_backend`. Para la evaluación se espera al menos: descripción del sistema, instrucciones para correrlo localmente (variables de entorno, base de datos, cómo ejecutar) y un diagrama de arquitectura. Es lo primero que cualquier evaluador revisa.

### El mapeo de entidad a DTO está dentro del Service
En `CitaService` existe el método privado `toResponse(Cita c)` que convierte la entidad al DTO de respuesta campo por campo. Esto funciona, pero mezcla dos responsabilidades en el mismo lugar: lógica de negocio y transformación de datos.

La solución es mover ese mapeo a una clase separada dentro del mismo módulo. Por ejemplo:

```java
// CitaMapper.java
public class CitaMapper {
    public static CitaResponse toResponse(Cita cita) {
        CitaResponse r = new CitaResponse();
        r.setId(cita.getId());
        r.setFechaHora(cita.getFechaHora());
        // ... resto de campos
        return r;
    }
}
```

Así el `CitaService` queda limpio y solo orquesta la lógica:

```java
public CitaResponse buscarPorId(UUID id) {
    Cita cita = repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cita", id.toString()));
    return CitaMapper.toResponse(cita);  // mapeo delegado
}
```

Este patrón aplica igual para todos los demás servicios del proyecto.

### Falta diagrama de arquitectura
La actividad 10 requiere un diagrama de arquitectura en el README. Puede ser tan simple como un diagrama de capas mostrando cómo fluye una petición desde el cliente hasta la base de datos, pasando por cada capa (Controller → Service → Repository → BD).

### Colección Postman sin revisar
Tienen la carpeta `postman/` en el repo, lo que es buena señal. Asegúrense de que el archivo esté exportado, actualizado con todos los endpoints y con ejemplos de request/response reales para cada operación.

---

## Calificación conceptual

| Criterio | Evaluación |
|---|---|
| Arquitectura multicapas (MVC / 3-capas) | ✅ Excelente |
| Patrón DTO (request y response) | ✅ Bien aplicado |
| Diseño RESTful (verbos, URIs, status codes) | ✅ Excelente |
| Autenticación y autorización JWT | ✅ Implementado correctamente |
| Manejo global de errores | ✅ Consistente |
| Multi-tenancy (decisión arquitectónica) | ✅ Bien pensado |
| Swagger / OpenAPI | ✅ Configurado |
| README y documentación | ⚠️ Falta desarrollar |
| Diagrama de arquitectura | ❌ No encontrado |
| Separación de responsabilidades en el mapeo | ⚠️ Mejorable |

---

## Recomendación final

Tienen una base técnica muy sólida. El esfuerzo principal debe ir ahora a la documentación: README completo, diagrama de arquitectura y colección Postman actualizada. El código habla bien del equipo, pero sin documentación es difícil de evaluar y de defender en la presentación final.

---

## Sugerencias adicionales de buenas prácticas

Estas son mejoras aplicables en el tiempo que queda, que no requieren cambios funcionales:

**1. Usar constantes para los roles en lugar de strings literales**
En lugar de `@PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")` disperso en varios controllers, definir una clase de constantes:
```java
public class Roles {
    public static final String ADMIN   = "ADMIN";
    public static final String MEDICO  = "MEDICO";
    public static final String PACIENTE = "PACIENTE";
}
```
Así, si el nombre de un rol cambia, se actualiza en un solo lugar.

**2. Agregar `@Column(updatable = false)` en el campo `createdAt`**
En el modelo `Organizacion` el campo `createdAt` se inicializa en el constructor pero no tiene la anotación `updatable = false` en todos los modelos. Esto evita que una actualización accidental sobrescriba la fecha de creación.

**3. Usar `@ResponseBody` implícito consistentemente**
Al usar `@RestController` ya está incluido, pero conviene revisar que ningún controller herede de `@Controller` sin el complemento correcto.

**4. Nombrar los paquetes de forma consistente en minúsculas**
El paquete base es `com.clinica.mi_app`. El guion bajo en `mi_app` es inusual en Java; la convención es `com.clinica.miapp` o `com.clinica.app`. No afecta el funcionamiento pero es una práctica estándar del ecosistema Java.

**5. Documentar el archivo `.env` o `application.properties` de ejemplo**
Agregar un archivo `application-example.properties` o comentarios en el `application.properties` existente indicando qué valores configurar. Esto facilita que cualquier persona pueda correr el proyecto sin adivinar las variables necesarias.
