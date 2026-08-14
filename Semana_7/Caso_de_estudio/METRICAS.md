# METRICAS DE CALIDAD - Backend Salida Bienes

## Objetivo
Este documento resume como aplicar **CMMI, TSP y PSP** en este proyecto de microservicios (Spring Boot + Maven + MySQL), y define un set inicial de **metricas de calidad** para seguimiento semanal.

> Este documento se apoya en los hallazgos ya documentados en [`02_DEUDA_TECNICA_Reporte_analisis_manual.md`](02_DEUDA_TECNICA_Reporte_analisis_manual.md) (informe manual) y [`03_DEUDA_TECNICA_Reporte_analisis_sonar.md`](03_DEUDA_TECNICA_Reporte_analisis_sonar.md) (informe SonarQube), y en la puesta en marcha descrita en [`01_DEUDA_TECNICA_Tutorial.md`](01_DEUDA_TECNICA_Tutorial.md). El `pom.xml` ya incluye `jacoco-maven-plugin` y `sonar-maven-plugin`, así que las métricas de cobertura y deuda técnica descritas aquí son medibles hoy con `mvn clean verify sonar:sonar`.

---

## 1. Marco de referencia: CMMI, TSP y PSP en este proyecto

Los tres modelos son complementarios y operan en distinta escala: **CMMI** mira el proceso de la organización/proyecto, **TSP** mira el proceso del equipo, y **PSP** mira el proceso individual del desarrollador. En un proyecto pequeño como este (equipo reducido, repositorio único `backend_salida_bienes`), los tres se aplican de forma ligera, sin la carga documental de una implementación CMMI formal de nivel 3+.

### 1.1 CMMI (Capability Maturity Model Integration) — nivel de proyecto

CMMI se usa aquí como **checklist de áreas de proceso**, no como certificación. Mapeo de las áreas de proceso relevantes a prácticas ya existentes (o pendientes) en el repo:

| Área de proceso (CMMI-DEV) | Estado en este proyecto | Evidencia / artefacto |
|---|---|---|
| **REQM** (Gestión de requisitos) | Parcial | Flujo de negocio documentado en `CLAUDE.md` (estados 1-6 de `SolicitudEntity`); falta trazabilidad requisito↔commit formal |
| **PP** (Planificación de proyecto) | Ligero | Ramas `main` / `duvan_new_mer`; sin backlog formal fuera del repo |
| **PMC** (Monitoreo y control de proyecto) | Parcial | `git log` como historial de avance; este documento formaliza el seguimiento semanal (sección 4) |
| **MA** (Medición y análisis) | En construcción | Este documento + informes 02/03 son el punto de partida |
| **CM** (Gestión de configuración) | ✅ Cubierto | Git + ramas por feature (`duvan_new_mer`), commits descriptivos |
| **VER** (Verificación) | 🔴 Débil | Cobertura de pruebas 5.2 % (ver sección 2) — mayor brecha del proyecto |
| **PPQA** (Aseguramiento de calidad de proceso y producto) | En construcción | SonarQube + Quality Gate (`01_DEUDA_TECNICA_Tutorial.md`) cubre la parte automatizada |

**Lectura práctica:** el proyecto está en un nivel de madurez informal (comparable a CMMI nivel 1-2): hay disciplina de control de versiones y arquitectura documentada (`CLAUDE.md`), pero falta institucionalizar medición (MA) y verificación (VER). Las secciones 3 y 4 de este documento son el mecanismo para cerrar esa brecha sin necesitar un programa CMMI completo.

### 1.2 TSP (Team Software Process)

TSP formaliza el ciclo de trabajo en equipo alrededor de **lanzamientos (launch)** y **ciclos semanales**. Aplicado a este proyecto:

- **Roles TSP** (adaptados a equipo pequeño — una persona puede cubrir varios roles):
  | Rol TSP | Responsabilidad en este proyecto |
  |---|---|
  | Team Lead | Coordina prioridades de la rama `duvan_new_mer` |
  | Development Manager | Decide qué módulo se trabaja (ej. inventario, autorización, PDF F2) |
  | Quality/Process Manager | Revisa hallazgos de `02_...manual.md` / `03_...sonar.md` y da seguimiento a este documento |
  | Support Manager | Mantiene herramientas (SonarQube local, JaCoCo) funcionando |
- **Ciclo semanal (weekly cycle):** cada semana se registra: horas planeadas vs. reales, tareas completadas, defectos encontrados/corregidos, y se actualiza la tabla de la sección 4.
- **Launch/Relaunch:** al iniciar un módulo nuevo (ej. la migración a `SolicitudBienEntity` o el PDF F2, ya presentes en el historial de commits) se estima esfuerzo y se compara luego contra lo real — esto alimenta la métrica de precisión de estimación (sección 3.2).

### 1.3 PSP (Personal Software Process)

PSP aplica a nivel individual (cada desarrollador que toca el backend). Los niveles PSP0 → PSP2 introducen progresivamente:

| Nivel | Qué añade | Aplicación aquí |
|---|---|---|
| **PSP0** | Registro de tiempo (Time Log) y de defectos (Defect Log) | Plantilla en sección 3.5 |
| **PSP0.1** | Estándar de conteo de tamaño (LOC) y estándar de codificación | Ya existe convención PascalCase para controllers/services (ver `02_...manual.md §6.1`) |
| **PSP1** | Estimación de tamaño y esfuerzo antes de codificar | Comparar estimado vs. real por tarea/rama |
| **PSP2** | Revisión de diseño y de código personal antes de commit/PR, checklist de defectos | Usar los hallazgos recurrentes de `02_...manual.md` como checklist de auto-revisión (ej. ¿usé `@Valid`? ¿usé logger en vez de `System.out`?) |

#### Plantilla — Time Log Register (por desarrollador/semana)

| Fecha | Módulo/Tarea | Fase (diseño/código/revisión/prueba) | Tiempo planeado | Tiempo real | Interrupciones |
|---|---|---|---|---|---|
| | | | | | |

#### Plantilla — Defect Recording Log

| # | Fecha | Módulo | Fase donde se inyectó | Fase donde se detectó | Tipo de defecto | Tiempo de corrección |
|---|---|---|---|---|---|---|
| | | | | | | |

> Tipos de defecto sugeridos (alineados con las categorías de `02_...manual.md`): Seguridad, Manejo de errores, Validación de entrada, Logging, Code smell, Configuración.

---

## 2. Línea base de métricas (análisis del 2026-08-14)

Estos son los valores de referencia contra los que se compara el progreso semanal (sección 4). Provienen de los informes ya generados para este mismo commit/rama.

| Métrica | Valor base | Fuente |
|---|---|---|
| Líneas de código (`src/main`) | ~5.490 (132 archivos Java) / 4.3k según Sonar | `02_...manual.md`, `03_...sonar.md` |
| Cobertura de pruebas | **5.2 %** | SonarQube (`03_...sonar.md`) |
| Duplicación de código | 0.0 % | SonarQube |
| Quality Gate | ✅ Passed ("Sonar way", umbral laxo) | SonarQube |
| Rating Reliability | C (4 issues) | SonarQube |
| Rating Security | D (1 issue) | SonarQube |
| Rating Maintainability | A (94 issues, ratio bajo) | SonarQube |
| Deuda técnica (SQALE, solo code smells) | 1d 3h (≈11 h) | SonarQube |
| Deuda técnica total estimada (manual, incluye tests faltantes) | ≈127 h (~16 días-persona) | `02_...manual.md` |
| Rating SQALE estimado (manual) | C–D | `02_...manual.md` |
| Issues por severidad (Sonar) | 1 Blocker · 27 High · 38 Medium · 24 Low · 5 Info | `03_...sonar.md` |
| Controladores con `@Valid` | 4 de 11 | `02_...manual.md §4.1` |
| DTOs de entrada con validación | 4 de 12 | `02_...manual.md §4.2` |
| Clases con logging SLF4J | 4 de 132 | `02_...manual.md §5.1` |

---

## 3. Set de métricas de seguimiento semanal

Categorías de métricas a registrar cada semana, con fuente y meta progresiva (no se exige el óptimo desde la semana 1, sino mejora sostenida sobre la línea base).

### 3.1 Métricas de tamaño y avance
| Métrica | Fórmula / definición | Herramienta |
|---|---|---|
| LOC añadidas/modificadas por semana | `git diff --stat` entre inicio y fin de semana | Git |
| Archivos Java nuevos vs. modificados | `git log --stat` | Git |
| Commits por semana | Conteo directo | Git |

### 3.2 Métricas de proceso (TSP/PSP)
| Métrica | Fórmula / definición | Meta |
|---|---|---|
| Precisión de estimación de esfuerzo | `tiempo real / tiempo estimado` | Rango 0.9–1.1 (±10 %) |
| % tiempo en cada fase | horas por fase / horas totales de la semana | Tendencia: más tiempo en diseño/revisión, menos en corrección |
| Interrupciones por sesión | conteo del Time Log | Minimizar |

### 3.3 Métricas de defectos
| Métrica | Fórmula / definición | Meta |
|---|---|---|
| Densidad de defectos | defectos encontrados / KLOC | Descendente semana a semana |
| Yield de revisión personal | defectos hallados antes del commit / defectos totales del módulo | Creciente (idealmente >70 % antes de PR) |
| Defectos por fase de inyección vs. detección | tabla del Defect Log | Reducir brecha (defectos detectados tarde = más costosos) |

### 3.4 Métricas de calidad automatizada (SonarQube/JaCoCo)
| Métrica | Fórmula / definición | Meta progresiva |
|---|---|---|
| Cobertura de pruebas (%) | JaCoCo vía Sonar | Base 5.2 % → meta trimestral ≥40 % en servicios core (`SolicitudServiceImpl`, `AutorizacionServiceImpl`, `InventarioServiceImpl`) |
| Deuda técnica (SQALE, `sqale_index`) | Sonar → Measures → Maintainability | Mantener o reducir respecto a 1d 3h; no debe crecer con código nuevo |
| Rating Security / Reliability | Sonar (A-E) | Subir a B en ambos tras cerrar quick wins (§ Priorización de `02_...manual.md`) |
| Issues Blocker/High abiertos | Sonar → Issues, filtrado por severidad | 0 Blocker permanente; High en descenso |
| % código nuevo que pasa Quality Gate | Sonar (comparación "New Code") | 100 % desde que se configure el gate estricto (ver `01_DEUDA_TECNICA_Tutorial.md §6.1`) |

### 3.5 Métricas de proceso PSP individuales
Registradas directamente en las plantillas de la sección 1.3 (Time Log y Defect Log), agregadas semanalmente en la tabla de la sección 4.

---

## 4. Plantilla de seguimiento semanal

Copiar esta tabla al inicio de cada semana y completar al cierre (viernes o último día laboral de la semana):

| Semana (fecha inicio) | LOC añadidas | Commits | Cobertura (%) | Deuda SQALE | Defectos encontrados | Defectos corregidos | Precisión estimación | Rating Security/Reliability/Maintainability |
|---|---|---|---|---|---|---|---|---|
| 2026-08-14 (línea base) | — | — | 5.2 | 1d 3h | 20 (informe manual) | 0 | — | D / C / A |
| | | | | | | | | |

> Actualizar corriendo `mvn clean verify sonar:sonar -Dsonar.projectKey=salidabienes-backend -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN` (ver `01_DEUDA_TECNICA_Tutorial.md §5`) y leyendo el dashboard antes de llenar la fila de la semana.

---

## 5. Cómo recolectar cada métrica (referencia rápida)

| Métrica | Comando / ruta |
|---|---|
| LOC y commits de la semana | `git log --since="<fecha>" --stat` |
| Cobertura, deuda, ratings, issues | `mvn clean verify sonar:sonar ...` (§1 tutorial) + dashboard `http://localhost:9000/dashboard?id=salidabienes-backend` |
| Deuda técnica desglosada por archivo | `http://localhost:9000/component_measures?id=salidabienes-backend&metric=sqale_index&view=list` |
| Time Log / Defect Log personal | Plantillas de la sección 1.3 (hoja de cálculo o texto plano por desarrollador) |

---

## 6. Umbrales de calidad propios (Quality Gate progresivo)

En vez de exigir desde ya el 80 % de cobertura del gate "Sonar way" por defecto (poco realista con la línea base de 5.2 %), se propone un umbral progresivo, ajustable trimestralmente:

| Trimestre | Cobertura mínima (código nuevo) | Deuda SQALE máxima | Issues Blocker permitidos | Rating Security/Reliability mínimo |
|---|---|---|---|---|
| Actual (base) | Informativo, sin bloqueo | — | 0 | D permitido, plan de cierre en curso |
| Siguiente | ≥40 % en código nuevo | No crecer respecto a 1d 3h | 0 | C |
| Posterior | ≥60 % en código nuevo | Reducir 20 % respecto a la línea base | 0 | B |

Esto se configura en SonarQube como Quality Gate propio (`Quality Gates → Create`, ver `01_DEUDA_TECNICA_Tutorial.md §6.1`) en lugar de usar "Sonar way" tal cual.

---

## Documentos relacionados

- [`01_DEUDA_TECNICA_Tutorial.md`](01_DEUDA_TECNICA_Tutorial.md) — cómo levantar SonarQube y ejecutar el análisis.
- [`02_DEUDA_TECNICA_Reporte_analisis_manual.md`](02_DEUDA_TECNICA_Reporte_analisis_manual.md) — informe manual de deuda técnica (20 hallazgos, ~127 h).
- [`03_DEUDA_TECNICA_Reporte_analisis_sonar.md`](03_DEUDA_TECNICA_Reporte_analisis_sonar.md) — informe SonarQube (línea base de las métricas automatizadas de este documento).
