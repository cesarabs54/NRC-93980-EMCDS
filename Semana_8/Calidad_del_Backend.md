# Calidad del Backend — `salidabienes-backend`

**Proyecto:** `backend` (Backend Salida de Bienes — SENA)
**Herramienta:** SonarQube Community Build v26.8.0.126808 (local), modo MQR
**Fecha del análisis:** 2026-08-21
**Fuente:** `http://localhost:9000/component_measures?id=salidabienes-backend`

---

## Resumen

| | |
|---|---|
| **Quality Gate** | ❌ **ERROR** |
| **Confiabilidad** | **A** — 0 bugs |
| **Seguridad** | **D** — 1 vulnerabilidad |
| **Mantenibilidad** | **A** — 114 code smells |
| **Revisión de seguridad** | **A** — 0 hotspots |
| **Cobertura de pruebas** | **4.6%** (1 test) |
| **Duplicación** | **0.0%** |
| **Deuda técnica** | **13h 29min** (ratio 0.6%) |

**Tamaño:** 4 830 líneas de código · 140 archivos · 142 clases · 391 funciones · complejidad ciclomática 418 · complejidad cognitiva 146 · densidad de comentarios 5.9%.

**115 incidencias abiertas** (114 code smells + 1 vulnerabilidad + 0 bugs), ~13h 34min de esfuerzo estimado de corrección.

---

## Por qué falla el Quality Gate

Condiciones evaluadas sobre **código nuevo desde la versión anterior** (14 ago 2026):

| Condición | Umbral | Valor actual | Resultado |
|---|---|---|---|
| Cobertura en código nuevo | ≥ 80% | 0.0% | ❌ ERROR |
| Duplicación en código nuevo | ≤ 3% | 0.0% | ✅ OK |
| Incidencias en código nuevo | = 0 | 24 | ❌ ERROR |

El proyecto no está agregando tests para el código que se va escribiendo, y el código nuevo introduce incidencias que el gate por defecto no tolera.

---

## Incidencias por severidad

| Severidad | Cantidad |
|---|---|
| Blocker | 1 |
| Critical | 31 |
| Major | 42 |
| Minor | 27 |
| Info | 14 |
| **Total** | **115** |

---

## La única vulnerabilidad (motivo del rating D)

**`SecurityConfig.java:64`** — regla `java:S4502`, severidad **CRITICAL**

> "Make sure disabling Spring Security's CSRF protection is safe here."

La protección CSRF de Spring Security está deshabilitada. No es necesariamente un error — en una API stateless con JWT suele ser correcto — pero Sonar exige confirmarlo explícitamente (comentario justificando la decisión o revisión marcada).

---

## Reglas con más incidencias

De 29 reglas distintas activas, estas 10 concentran la mayoría:

| Regla | Descripción | Incidencias |
|---|---|---|
| `S106` | No usar salidas estándar (`System.out`/`err`) para registrar información; usar un logger. | 16 |
| `S1192` | Los literales de texto repetidos deben extraerse a una constante. | 14 |
| `S8688` | Los métodos `.now()` basados en tiempo deben indicar `ZoneId` o `Clock`. | 11 |
| `S1128` | Eliminar los imports que no se usan. | 11 |
| `S1118` | Las clases utilitarias no deben tener constructor público. | 10 |
| `S125` | Eliminar bloques de código comentado. | 7 |
| `S115` | Los nombres de constantes deben cumplir la convención de nombres. | 6 |
| `S6809` | No invocar vía `this` métodos que dependen del proxy de Spring (rompe AOP: `@Transactional`, seguridad, etc.). | 4 |
| `S1452` | No usar wildcards genéricos (`<?>`) en tipos de retorno. | 4 |
| `S120` | Los nombres de paquete deben cumplir la convención de nombres. | 4 |

---

## Archivos con más incidencias

Dónde concentrar el trabajo de limpieza primero:

| Archivo | Incidencias |
|---|---|
| `services/impl/SolicitudServiceImpl.java` | 22 |
| `services/impl/AutorizacionServiceImpl.java` | 11 |
| `services/impl/DashboardServiceImpl.java` | 10 |
| `exceptions/GlobalExceptionHandler.java` | 6 |
| `dtos/requests/AutorizacionRequestDTO.java` | 3 |
| `entities/AutorizacionEntity.java` | 3 |
| `services/impl/InventarioServiceImpl.java` | 3 |

---

## Lectura rápida

- **Lo más urgente:** el único **Blocker** y la vulnerabilidad **CSRF** en `SecurityConfig.java` — bajo esfuerzo, alta severidad.
- **Lo más impactante para destrabar el Quality Gate:** subir la cobertura de pruebas (hoy 4.6%, solo 1 test). Es la condición que más lejos está del umbral (0% vs 80% requerido) y bloquea cualquier código nuevo.
- **Lo más repetitivo:** `System.out`/`err` en vez de logger (16) y literales duplicados (14) — fáciles de resolver en lote.
- **Dónde concentrar refactor:** `SolicitudServiceImpl.java` encabeza tanto incidencias (22) como probablemente responsabilidad excesiva en el archivo.

---

## Comparación con el reporte anterior (`03_DEUDA_TECNICA_Reporte_analisis_sonar.md`, 14 ago 2026)

| Métrica | 14 ago 2026 | 21 ago 2026 |
|---|---|---|
| Quality Gate | ✅ Passed | ❌ ERROR |
| Confiabilidad | C (4 issues) | **A (0 bugs)** |
| Seguridad | D (1 issue) | D (1 issue, igual) |
| Mantenibilidad | A (94 issues) | A (114 issues) |
| Coverage | 5.2% | 4.6% |
| Deuda técnica | 1d 3h | 13h 29min |
| Total incidencias | 95 | 115 |

La confiabilidad pasó de C a A (0 bugs abiertos hoy), pero el Quality Gate ahora falla porque hay código nuevo desde la versión anterior sin tests ni limpieza — confirma que el hueco de cobertura sigue siendo el problema estructural del proyecto.
