# Reporte SonarQube — Deuda Técnica

**Proyecto analizado:** `backend` (Backend Salida de Bienes — SENA)
**Herramienta:** SonarQube Community Build v26.8.0 (local, Docker)
**Fecha del análisis:** 2026-08-14
**Tamaño del proyecto:** 4.3k líneas de código (Java, XML)

> Este reporte complementa el análisis manual de [`02_DEUDA_TECNICA_Reporte_analisis_manual.md`](02_DEUDA_TECNICA_Reporte_analisis_manual.md), esta vez con una herramienta automatizada. Sirve como material de clase para explicar cómo se lee un dashboard de SonarQube.

---

## Resumen general

| Métrica | Resultado | Lectura |
|---|---|---|
| **Quality Gate** | ✅ Passed | El proyecto pasa el umbral por defecto de Sonar ("Sonar way") — no te dejes engañar por esto, es un umbral laxo si no hay código nuevo con el que comparar |
| **Security** | **D** (1 issue) | Preocupante — D es la segunda peor nota posible en la escala A-E |
| **Reliability** | **C** (4 issues) | Aceptable, pero mejorable |
| **Maintainability** | **A** (94 issues) | Ojo: "A" con 94 code smells suena contradictorio, pero el rating se calcula por **ratio** (esfuerzo de arreglo ÷ tamaño del código), no por cantidad de issues. 94 issues pequeños en 4.3k líneas siguen dando A |
| **Coverage** | **5.2%** | Bajísimo — este es el problema real del proyecto |
| **Duplications** | **0.0%** | Excelente, sin bloques de código duplicado |
| **Esfuerzo total (code smells)** | **1d 3h** | Es solo la deuda de *mantenibilidad* (code smells). No incluye el hueco de cobertura de tests |

**Desglose de los 95 issues por severidad:** 1 Blocker · 27 High · 38 Medium · 24 Low · 5 Info.

---

## Cómo explicar cada métrica a los estudiantes

- **Quality Gate:** un "semáforo" pasa/no pasa definido por umbrales configurables (ej. cobertura mínima, % de duplicación máximo, cero issues Blocker en código nuevo). Que pase no significa que el proyecto esté bien; significa que cumple el umbral que se le configuró.
- **Rating A-E:** escala SQALE. Se usa en tres dimensiones independientes — *Reliability* (bugs), *Security* (vulnerabilidades) y *Maintainability* (code smells) — cada una con su propio criterio de cálculo.
- **Reliability / Security rating:** se basa en la **severidad del peor issue abierto**, no en la cantidad. Un solo issue *High* ya baja el rating a D, aunque haya cero issues más.
- **Maintainability rating (Debt Ratio):** `tiempo estimado para arreglar todos los code smells ÷ tiempo estimado de haber escrito el proyecto desde cero`. A ≤5%, B ≤10%, C ≤20%, D ≤50%, E >50%.
- **Coverage:** % de líneas/ramas ejercitadas por los tests automáticos. No mide si los tests son buenos, solo si el código se ejecutó durante algún test.
- **Duplications:** % de líneas detectadas como bloques copy-paste (bloques de ~10 líneas o más, repetidos).

---

## Priorización (de más a menos urgente)

### 1. El único Blocker — arréglalo primero, sin excusa
`JwtAuthFilter.java:23` → *"logger" is the name of a field in "GenericFilterBean"* — se está sombreando un campo heredado de Spring Security. 5 min de esfuerzo, pero es la severidad más alta posible.

### 2. El único issue de Security (motivo del rating D)
`SecurityConfig.java:64` → *"Make sure disabling Spring Security's CSRF protection is safe here"* (High). No es necesariamente un error — en una API stateless con JWT, deshabilitar CSRF suele ser correcto — pero Sonar exige confirmarlo explícitamente (comentario justificando la decisión, o marcarlo como hotspot revisado).

### 3. Los 4 issues de Reliability (motivo del rating C)
Mayormente del tipo *"Explicitly specify the time zone by passing a ZoneId to .now()"* — bajo esfuerzo (5 min c/u), pero son bugs reales de reproducibilidad y zona horaria.

### 4. El hueco que Sonar no traduce a horas: Coverage 5.2%
Confirma la sección 1.1 de `02_DEUDA_TECNICA_Reporte_analisis_manual.md` (72 h estimadas en tests faltantes). El "1d 3h" de esfuerzo que muestra Sonar **solo cubre code smells**, no la ausencia de tests — por eso ese número se ve engañosamente bajo comparado con las ~127 h del informe manual.

### 5. El resto (94 code smells, mayoría 1-20 min c/u)
Patrones repetidos, fáciles de resolver en lote:
- Constructores privados faltantes en los 7 `XxxMapper` (clases de solo métodos estáticos) — 5 min c/u.
- Imports sin usar y wildcards (`import x.*`) — coincide con la sección 6.3 del informe manual.
- `System.out` en vez de logger (`BackendApplication.java`, un mapper) — coincide con la sección 3.2.
- Bloques de código comentado sin limpiar — coincide con la sección 6.5.
- Convenciones de nombres (constantes, clases como `pdfController` que debería ser `PdfController`) — coincide con la sección 6.1.

---

## Dónde ver la deuda técnica *de Sonar* (y por qué no son las 127 h)

**Ruta en la UI:** menú lateral del proyecto → **Measures → Maintainability**, o directo:
```
http://localhost:9000/component_measures?id=salidabienes-backend&metric=sqale_index&view=list
```

Ahí aparece el metric **"Technical Debt" (`sqale_index`) = 1d 3h** (≈11 h, usando el estándar de 8h/día de Sonar), desglosado por archivo. Los que más aportan:

| Archivo | Deuda |
|---|---|
| `SolicitudServiceImpl.java` | 3h 27min |
| `GlobalExceptionHandler.java` | 1h 15min |
| `InventarioServiceImpl.java` | 46min |
| `AutorizacionServiceImpl.java` | 39min |
| `FileStorageService.java` | 30min |
| `AuthController.java` | 26min |
| `JwtUtils.java` | 20min |
| `GuardiaServiceImpl.java` | 18min |
| `DetallesSolicitudEntity.java` | 16min |
| `SolicitudMapper.java` | 15min |
| ...resto (30 archivos más, 1-11 min c/u) | hasta completar 1d 3h |

Dato para clase: `SolicitudServiceImpl.java` encabeza la lista — coincide exactamente con la sección 6.2 de `02_DEUDA_TECNICA_Reporte_analisis_manual.md` ("concentra demasiada responsabilidad, 546 líneas").

**Por qué esta cifra (1d 3h) no coincide con las 127 h del informe manual:**

El `sqale_index` **solo suma el esfuerzo de los *code smells* (Maintainability)**. No incluye:
- El esfuerzo de **escribir los tests faltantes** (las 72 h más grandes del informe manual, sección 1.1) — Sonar solo muestra el síntoma (**Coverage 5.2%**), pero no traduce "falta de tests" en horas de deuda.
- El esfuerzo de remediar los issues de **Security** o **Reliability** — cada uno tiene su propio "remediation effort" (5, 10, 20 min por issue, visible en la pestaña Issues), pero SonarQube **no los suma** dentro del `sqale_index` que ves en Maintainability.

En resumen: **127 h (manual) ⊃ 1d 3h (Sonar, solo code smells) + Coverage 5.2% (síntoma, no horas) + esfuerzo de Security/Reliability issue por issue (no sumado por Sonar)**. Ninguna herramienta automatizada mide "toda" la deuda técnica de una sola vez — cada una tiene su propio alcance, y hay que cruzar fuentes para tener el panorama completo.

---

## Comparación con el análisis manual (`02_DEUDA_TECNICA_Reporte_analisis_manual.md`)

Ambos análisis apuntan en la misma dirección: **la cobertura de pruebas es el hueco más grande** del proyecto, y varios code smells de mantenibilidad ya se habían detectado a mano. La diferencia es que SonarQube da un Blocker y un Security issue concretos, con archivo y línea exactos, que el análisis manual no había puntualizado así — y lo hace en segundos, de forma repetible en cada commit.

| | Análisis manual | SonarQube |
|---|---|---|
| Método | Lectura humana del código | Reglas estáticas automatizadas |
| Mayor hallazgo | Falta de tests (~72 h) | Coverage 5.2% |
| Detecta intención de negocio | Sí (ej. reglas del dominio SENA) | No |
| Repetible en cada PR | No, manual | Sí, con `mvn sonar:sonar` o CI |
| Puntualiza línea exacta | A veces | Siempre |
