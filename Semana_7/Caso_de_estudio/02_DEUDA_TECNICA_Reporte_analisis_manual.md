# Informe de Deuda Técnica

**Proyecto:** Backend Salida de Bienes — SENA
**Rama analizada:** `duvan_new_mer`
**Fecha del análisis:** 2026-08-14
**Alcance:** 132 archivos Java, ~5.490 líneas (`src/main`), 1 archivo de test.
**Método:** Análisis estático manual (no se ejecutó SonarQube/CAST; ver sección final para dejarlo integrado).

> Los tiempos siguen el criterio habitual de herramientas tipo SonarQube ("remediation cost"): tiempo estimado de una persona con conocimiento del proyecto para corregir cada hallazgo, no tiempo de descubrimiento.

---

## Resumen ejecutivo

| Categoría | Hallazgos | Deuda estimada |
|---|---|---|
| 🧪 Cobertura de pruebas | 1 | ~72 h |
| 🔒 Seguridad | 3 | ~7 h |
| ⚠️ Manejo de errores | 5 | ~6 h |
| ✅ Validación de entrada | 2 | ~15 h |
| 📋 Logging | 2 | ~9 h |
| 🧹 Code smells / mantenibilidad | 6 | ~16 h |
| ⚙️ Configuración / portabilidad | 1 | ~2 h |
| **Total** | **20** | **≈ 127 h (~16 días-persona de 8 h)** |

**Rating estimado (escala SQALE):** entre **C** y **D** — deuda manejable pero ya notable, concentrada sobre todo en ausencia de pruebas automatizadas (57 % del total).

---

## 1. Cobertura de pruebas — 🔴 Crítico

### 1.1 No existen pruebas unitarias ni de integración reales
- **Evidencia:** único archivo en `src/test`: `BackendApplicationTests.java`, con un solo test vacío (`contextLoads()`). No hay tests de `SolicitudServiceImpl`, `AutorizacionServiceImpl`, `InventarioServiceImpl`, mappers, ni de los 11 controladores. No hay plugin de cobertura (JaCoCo) en `pom.xml`.
- **Por qué importa:** el flujo de negocio principal (5 pasos: creación → firma cuentadante → firma autorizante → salida → entrada) y la lógica de estados (`EstadoEntity` 1-6) no tienen ninguna red de seguridad ante regresiones. Los cambios recientes (`InventarioServiceImpl`, PDF F2, migración `SolicitudBienEntity`) se validaron manualmente.
- **Estimación:**
  - Tests unitarios de los 5 `ServiceImpl` más grandes (`SolicitudServiceImpl` 546 líneas, `InventarioServiceImpl`, `AutorizacionServiceImpl`, `UsuarioServiceImpl`, `PdfServiceImpl`): ~6 h c/u → **30 h**
  - Tests unitarios del resto de servicios (Guardia, Sede, Registro, Login, Contraseña, Email): ~3 h c/u → **18 h**
  - Tests de mappers (7 clases `XxxMapper`): ~1 h c/u → **7 h**
  - Tests de integración del flujo completo de solicitud (creación→cierre) con `@SpringBootTest` + BD de prueba: **12 h**
  - Configurar JaCoCo + umbral mínimo de cobertura en `pom.xml`: **1 h**
  - Tests de seguridad (`JwtUtils`, `JwtAuthFilter`, `SecurityConfig`): **4 h**
  - **Subtotal: ~72 h**

---

## 2. Seguridad — 🔴 Alto

### 2.1 Secreto JWT hardcodeado y compartido entre dev y prod (fallback)
- **Evidencia:**
  ```
  application-dev.properties:25:  jwt.secret=u9qV7kL3mP8rT5wY2xZ6aB0cD4eF8gH1jK5nQ9tX3vC7zA2bE6fI0lM4oR8uS1wY5A9cD3fG7hJ2kN6pQ0tV4xZ8
  application-prod.properties:22: jwt.secret=${JWT_SECRET:u9qV7kL3mP8rT5wY2xZ6aB0cD4eF8gH1jK5nQ9tX3vC7zA2bE6fI0lM4oR8uS1wY5A9cD3fG7hJ2kN6pQ0tV4xZ8}
  ```
  El mismo secreto está commiteado en el repo y además sirve como **valor por defecto en producción** si la variable de entorno `JWT_SECRET` no está seteada — si el despliegue olvida definir la env var, prod firma tokens con un secreto público en el historial de git.
- **Estimación:** rotar el secreto, quitar el fallback de `application-prod.properties` (dejar la app sin arrancar si falta `JWT_SECRET`), purgar el valor del historial si se considera necesario: **2 h**

### 2.2 Posible path traversal en `FileStorageService.getPdfPath`
- **Evidencia** (`FileStorageService.java:61-79`):
  ```java
  public Resource getPdfPath(String filename) {
      String relative = filename;
      if (relative.startsWith("/uploads/")) relative = relative.substring("/uploads/".length());
      Path filePath = this.uploadDir.resolve(relative).normalize();
      ...
  }
  ```
  `filename` no se valida contra secuencias `..`; `resolve().normalize()` no impide escapar de `uploadDir` si el valor de entrada las contiene. Hay que confirmar si `filename` llega siempre generado internamente (código de solicitud) o si en algún punto viene de un parámetro de request expuesto al cliente.
- **Estimación:** añadir verificación `filePath.startsWith(uploadDir)` tras `normalize()` y lanzar excepción si no: **1 h**

### 2.3 Sin manejador genérico para errores no controlados (fuga de información)
- Ver punto 3.1 — se cuenta el tiempo allí, se referencia aquí por ser también un riesgo de seguridad (stack traces de Spring por defecto en 500).
- **Estimación:** incluida en 3.1 (**~2 h** de las 6 h totales de esa sección corresponden a este punto).

**Subtotal sección 2 (sin solapar con 3.1): ~7 h**

---

## 3. Manejo de errores — 🟠 Medio-alto

### 3.1 `GlobalExceptionHandler` no cubre todas las excepciones de dominio ni el caso genérico
- **Evidencia:** existen `NullException` y `TokenRefreshException` en `exceptions/Exception/`, pero `GlobalExceptionHandler` solo maneja `MethodArgumentNotValidException`, `NotFoundException` y `DuplicateException`. No hay `@ExceptionHandler(Exception.class)` de respaldo → cualquier excepción no prevista devuelve el error por defecto de Spring (potencialmente con stack trace).
- **Estimación:** **3 h**

### 3.2 `System.out.println` de depuración en código de producción
- **Evidencia:** `SolicitudServiceImpl.java:80-81`
  ```java
  System.out.println("fechaFirma = " + fechaFirma);
  System.out.println("fechaFirmaAutorizante = " + campoFirmado);
  ```
  Solo 4 de 132 clases usan `SLF4J` (`LoggerFactory.getLogger`); el resto no registra nada o usa `System.out`/`printStackTrace` (5 archivos: `SolicitudServiceImpl`, `AutorizacionServiceImpl`, `SolicitudMapper`, `GuardiaServiceImpl`, `BackendApplication`).
- **Estimación:** eliminar prints y reemplazar por logger donde aporte valor: **1 h** (ver también sección 5, que cubre la introducción sistemática de logging).

### 3.3 Try/catch que no aporta valor (re-lanza lo mismo que captura)
- **Evidencia:** `SolicitudServiceImpl.java:46-53`
  ```java
  public boolean existeSolicitud(String codigoFormato) {
      try {
          return solicitudRepository.existsByCodigoFormato(codigoFormato);
      } catch (RuntimeException e) {
          throw new RuntimeException(e);
      }
  }
  ```
  Captura `RuntimeException` solo para volver a envolverla en otra `RuntimeException` — no añade contexto ni cambia el comportamiento; puede eliminarse.
- **Estimación:** **0.5 h**

### 3.4 Comentarios de depuración/legado poco profesionales en código productivo
- **Evidencia:** `JwtUtils.java:36` (`// MÉTODO NUEVO Y ESTÁNDAR...`), `JwtUtils.java:64` (`// MÉTODOS QUE TE FALTABA - ¡AHORA SÍ EXISTE!`). No son bugs, pero son ruido que dificulta lectura y sugieren código pegado sin revisar.
- **Estimación:** **0.5 h**

### 3.5 Excepciones genéricas (`RuntimeException`) en `FileStorageService` en vez de excepciones de dominio propias
- **Evidencia:** `FileStorageService.java:42,57,76` — usa `RuntimeException` en lugar de una excepción específica (p.ej. `FileStorageException`) que el `GlobalExceptionHandler` pueda mapear a un código HTTP adecuado (hoy cae también en el 500 genérico sin manejar, ver 3.1).
- **Estimación:** **2 h**

**Subtotal sección 3: ~6 h** (0.5+0.5+2 h propios, netos de solapamiento con 3.1 ya contado)

---

## 4. Validación de entrada — 🟠 Medio

### 4.1 Solo 4 de 11 controladores usan `@Valid`
- **Evidencia:** `@Valid` aparece únicamente en `SolicitudController`, `GuardiaController`, `pdfController`, `AuthController`. Los otros 7 (`UsuarioController`, `InventarioController`, `SedeController`, `AutorizacionController`, `ContrasenaController`, `RolController`, `TipoDocumentoController`) reciben el `@RequestBody` sin activar las validaciones Bean Validation aunque los DTOs las declaren.
- **Estimación:** **7 h** (revisar cada endpoint, añadir `@Valid` + probar)

### 4.2 Solo 4 de 12 DTOs de entrada tienen anotaciones de validación
- **Evidencia:** solo `BienRequestDto`, `DetallesSolicitudRequestDTO`, `InventarioRequestDTO` y `SolicitudRequestDTO` usan `@NotNull/@NotBlank/@Size/@Email`. Los 8 restantes (usuarios, roles, sedes, autorización, login, etc.) no validan nulos/formatos antes de llegar a la capa de persistencia, dependiendo de que la BD rechace por constraint (`NOT NULL`) con un error 500 poco informativo.
- **Estimación:** **8 h**

**Subtotal sección 4: ~15 h**

---

## 5. Logging — 🟡 Medio

### 5.1 Ausencia sistemática de logging estructurado
- **Evidencia:** solo `LoginServiceImpl`, `CustomUserDetail`, `JwtUtils`, `JwtAuthFilter` usan SLF4J. Ninguno de los `ServiceImpl` de negocio (`SolicitudServiceImpl`, `AutorizacionServiceImpl`, `InventarioServiceImpl`, etc.) registra eventos de negocio (creación de solicitud, cambios de estado, rechazos) ni errores capturados — dificulta la trazabilidad en producción/soporte.
- **Estimación:** introducir `Logger` en los 7 `ServiceImpl` de negocio principales con logs de entrada/salida/errores en operaciones críticas: **8 h**

### 5.2 `catch (Exception e)` que solo hace `printStackTrace()` sin logging estructurado
- **Evidencia:** patrón repetido en `AutorizacionServiceImpl`, `GuardiaServiceImpl`, `SolicitudMapper` — se imprime a stdout en vez de usar el logger, perdiendo nivel de severidad, timestamps y posibilidad de centralizar logs (ELK, CloudWatch, etc.).
- **Estimación:** **1 h**

**Subtotal sección 5: ~9 h**

---

## 6. Code smells / mantenibilidad — 🟡 Medio-bajo

### 6.1 Clase con convención de nombres incorrecta
- **Evidencia:** `controllers/pdfController.java` — nombre de clase pública en minúscula inicial (`pdfController`), viola convención Java (`PdfController`) y `CLAUDE.md` que documenta el resto de controladores en PascalCase.
- **Estimación:** **0.5 h** (rename + actualizar referencias/imports)

### 6.2 `SolicitudServiceImpl` concentra demasiada responsabilidad (546 líneas)
- **Evidencia:** una sola clase orquesta creación de solicitud, firmas de cuentadante/autorizante, envío de emails, generación de PDF y helpers auxiliares (`// METODOS AUXILIARES` en línea 331). Es el archivo más grande del proyecto por casi el doble que el segundo (`InventarioServiceImpl`, 255 líneas).
- **Estimación:** extraer al menos el subflujo de firmas/notificaciones a un colaborador dedicado (p. ej. `SolicitudFirmaService`): **8 h**

### 6.3 Imports con wildcard (`import x.*;`) en 28 archivos (36 ocurrencias)
- **Evidencia:** incluyendo `SolicitudServiceImpl` (5), `AutorizacionServiceImpl` (3), `BusquedaService` (2). Dificulta ver de un vistazo qué símbolos usa cada clase y puede esconder colisiones de nombres.
- **Estimación:** **2 h** (automatizable con el "optimize imports" del IDE + revisión)

### 6.4 Falta de `Javadoc`/contrato en excepciones de dominio no utilizadas
- **Evidencia:** `NullException` y `TokenRefreshException` existen pero no están referenciadas desde ningún `catch`/`throw` visible fuera de su propia definición ni gestionadas por `GlobalExceptionHandler` (ver 3.1) — código muerto o incompleto, no está claro cuál era la intención.
- **Estimación:** **2 h** (decidir si se usan o se eliminan, y documentar)

### 6.5 Comentarios "parche" que delatan iteración sin limpieza posterior
- Repetido de 3.4, aplica también a `SolicitudServiceImpl.java:331` (`// METODOS AUXILIARES` como único criterio de organización en vez de separar en clases/paquetes).
- **Estimación:** **1 h**

### 6.6 Ausencia de convención `@Transactional(readOnly = true)` consistente en consultas
- **Evidencia:** de 27 usos de `@Transactional` en 5 clases, no todos los métodos de solo lectura declaran `readOnly = true` (sí lo hace `getAllSolicitudes`, pero no de forma sistemática en el resto de consultas de `InventarioServiceImpl`/`AutorizacionServiceImpl`), perdiendo la optimización de Hibernate para transacciones de solo lectura.
- **Estimación:** **2.5 h** (auditar y anotar)

**Subtotal sección 6: ~16 h**

---

## 7. Configuración / portabilidad — 🟢 Bajo

### 7.1 Ruta de subida hardcodeada específica de Windows en `dev`
- **Evidencia:** `application-dev.properties:31` → `app.upload.dir=C:/uploads`. Correcto que exista un perfil `dev`, pero acopla el entorno de desarrollo a Windows y no es documentado como prerequisito en el README/CLAUDE.md (si un desarrollador usa Linux/Mac, falla silenciosamente al no poder crear el directorio con esa sintaxis).
- **Estimación:** documentar como variable de entorno override (`APP_UPLOAD_DIR`) en vez de valor fijo: **2 h**

---

## No se detectó (puntos positivos a mantener)

- ✅ CORS bien configurado: orígenes explícitos, sin `*` combinado con `allowCredentials(true)` (`CorsConfig.java`).
- ✅ Contraseñas con BCrypt strength 12.
- ✅ Sin bloques `catch` vacíos (`catch (Exception e) {}`) en el código analizado.
- ✅ Separación clara Entity/DTO/Mapper y capa Service/Impl consistente con lo documentado en `CLAUDE.md`.
- ✅ `@Transactional` sí se usa donde hay escritura multi-tabla (aunque falta afinar `readOnly`, ver 6.6).

---

## Priorización recomendada

| Prioridad | Acción | Impacto | Esfuerzo |
|---|---|---|---|
| 1 | Quitar fallback del secreto JWT en prod (2.1) | Alto (seguridad) | 2 h |
| 2 | Añadir handler genérico + excepciones sin usar (3.1, 6.4) | Alto (seguridad + estabilidad) | 5 h |
| 3 | Validar `path traversal` en `getPdfPath` (2.2) | Alto (seguridad) | 1 h |
| 4 | `@Valid` en los 7 controladores restantes (4.1) | Medio-alto | 7 h |
| 5 | Tests unitarios del flujo de solicitud + servicios core (1.1) | Alto (calidad a largo plazo) | 72 h |
| 6 | Resto (logging, wildcard imports, refactor `SolicitudServiceImpl`, etc.) | Medio-bajo | ~40 h |

**Quick wins (< 1 día, alto impacto):** puntos 1 a 4 de la tabla — **≈15 h** cubren el riesgo de seguridad y estabilidad más importante antes de invertir en cobertura de pruebas.

---

## Nota metodológica

Este informe es un análisis estático manual, equivalente en criterio (no en exhaustividad automatizada) a lo que reportaría una herramienta como **SonarQube/SonarCloud** (métrica *Technical Debt* en minutos/horas vía SQALE) o **CodeScene**. Para tener esta medición de forma continua y en cada PR, se recomienda integrar `sonar-maven-plugin` + JaCoCo al pipeline — quedó fuera del alcance de esta tarea porque se pidió únicamente el análisis, no la puesta en marcha de la herramienta.
