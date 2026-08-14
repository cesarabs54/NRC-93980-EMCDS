# Medición de la Deuda Técnica con SonarQube Cloud: del concepto al número en tiempo

**Curso:** Estándares y Métricas de Calidad en el Desarrollo de Software — Especialización en Desarrollo de Software
**Semana 7 — Resultado de aprendizaje:** Establecer las herramientas de calidad de software.

> *Nota de origen:* este documento complementa el marco conceptual de deuda técnica (Cunningham, 1992; cuadrantes de Kruchten y de Fowler) con el mecanismo concreto que usa **SonarQube Cloud** para traducir ese concepto en una cifra operable: tiempo estimado de remediación. Las capturas de referencia usadas para ilustrar los rangos de esta sección corresponden a un mismo proyecto en dos estados distintos (código nuevo con deuda baja vs. código global con deuda acumulada mayor).

---

## 1. De la analogía financiera al número: la metodología SQALE

SonarQube no "opina" sobre qué tan sucio está el código; aplica un método de cálculo estandarizado llamado **SQALE** (*Software Quality Assessment based on Lifecycle Expectations*). SQALE traduce cada regla de calidad incumplida (cada *code smell*, vulnerabilidad o *bug* potencial) en una **cantidad de tiempo de desarrollo** necesaria para corregirla, previamente calibrada por regla.

Esto cierra el vínculo con la analogía financiera del artículo de referencia: si el **capital** de la deuda técnica es "el esfuerzo que el equipo emplearía en eliminarla", SQALE es, literalmente, la calculadora de ese capital, expresado en minutos, horas o días-persona.

Cada regla del motor de análisis de SonarQube (por ejemplo, "método con complejidad ciclomática > 15" o "variable no utilizada") tiene asociado un **costo de remediación fijo o proporcional**, definido por Sonar según la dificultad típica de corregir ese problema. La suma de esos costos, para todos los *code smells* detectados en un componente, es lo que SonarQube reporta como **Debt**.

---

## 2. Las cuatro métricas clave y cómo se calculan

### 2.1 Code Smells

Es el conteo de issues de tipo *maintainability* detectados: patrones que no rompen el sistema hoy, pero que aumentan el costo de cambiarlo mañana (hardcodeos, funciones demasiado largas, duplicación, complejidad excesiva, ausencia de manejo de errores, etc. — los mismos ejemplos típicos que menciona el artículo conceptual). Es la métrica de **cantidad**; `Debt` es la métrica de **costo**.

### 2.2 Debt (Technical Debt / Remediation Cost)

$$
\text{Debt} = \sum_{i=1}^{n} \text{costo de remediación}_i
$$

donde la suma recorre todos los *code smells* abiertos del componente. Es la métrica central: **el tiempo estimado que un desarrollador necesitaría para eliminar toda la deuda técnica detectada**, expresado directamente en unidades de tiempo (`min`, `h`, `d`).

SonarQube reporta este valor en **dos alcances simultáneos**, que conviene no confundir:

| Alcance | Qué mide | Cuándo usarlo |
|---|---|---|
| **New Code** | Deuda introducida solo en el código nuevo/modificado desde el *baseline* (ej. desde la última versión, o los últimos N días) | Gobierno del *Quality Gate* en cada Pull Request: evita que se siga acumulando deuda |
| **Overall Code** | Deuda acumulada en la totalidad de la base de código, histórica incluida | Diagnóstico del estado general del proyecto y planificación de *paydown* (pago de deuda) a mediano plazo |

Esta distinción es la aplicación práctica del principio "deuda real vs. deuda potencial" visto en el marco conceptual: la deuda de **New Code** es la que el equipo está generando *ahora* (más fácil de evitar/controlar), mientras que la de **Overall Code** es la deuda ya heredada, potencial o real según si esas zonas del código siguen evolucionando.

### 2.3 Debt Ratio (SQALE Rating input)

$$
\text{Debt Ratio} = \frac{\text{Costo de remediación (Debt)}}{\text{Costo de desarrollo estimado}} \times 100
$$

El **costo de desarrollo estimado** se calcula como:

$$
\text{Costo de desarrollo} = \text{Líneas de código} \times \text{costo por línea}
$$

SonarQube usa por defecto un valor de referencia de **0.5 días-persona (≈ 30 min) por línea de código** como costo de haber construido esa línea desde cero. El Debt Ratio responde entonces a la pregunta: *"¿qué porcentaje del costo total de haber escrito este componente desde cero representa arreglar su deuda técnica?"*

Es una métrica **normalizada por tamaño**: permite comparar la salud relativa de un módulo pequeño con deuda de 2 horas contra un módulo grande con deuda de 2 días, algo que la métrica `Debt` en bruto no permite.

### 2.4 Rating (SQALE Rating, A–E)

El Debt Ratio se traduce a una calificación de letra mediante umbrales fijos:

| Rating | Debt Ratio | Interpretación |
|---|---|---|
| **A** | ≤ 5 % | Deuda técnica bajo control |
| **B** | 5 % – 10 % | Deuda moderada, vigilar tendencia |
| **C** | 10 % – 20 % | Deuda relevante, priorizar en backlog |
| **D** | 20 % – 50 % | Deuda alta, riesgo de fricción creciente |
| **E** | > 50 % | Deuda crítica, el "punto de inflexión" (T3) del artículo conceptual ya fue superado: el pasivo supera el activo |

Este rating es, en la práctica, el indicador que un *Quality Gate* usa para bloquear o aprobar un despliegue (ej. "el Rating de mantenibilidad del New Code debe ser A").

### 2.5 Effort to Reach A (Effort to Reach Maintainability Rating A)

Cuando el Rating actual no es A, esta métrica cuantifica —de nuevo, en tiempo— **cuánto esfuerzo adicional** haría falta remediar para que el Debt Ratio baje por debajo del umbral del 5 % y el componente alcance Rating A. Si el proyecto ya está en A (como en ambos ejemplos de referencia), este valor es `0`: no hace falta esfuerzo adicional para sostener la calificación máxima.

Esta métrica es la más directamente accionable para *sprint planning*: convierte la pregunta abstracta "¿deberíamos pagar deuda este sprint?" en una cifra de horas-ingeniero concreta para estimar en el backlog.

---

## 3. Lectura comparada de un mismo proyecto en dos estados

La siguiente tabla resume dos evaluaciones reales del mismo proyecto (rama `main` vs. rama `develop` en distinto momento), tal como se ven en el panel *Measures* de SonarQube Cloud:

| Métrica | Estado con deuda alta | Estado con deuda baja |
|---|---:|---:|
| Code Smells (New Code) | 111 | 0 |
| **Debt (New Code)** | **1 d 7 h** | **0** |
| Code Smells (Overall Code) | 258 | 14 |
| **Debt (Overall Code)** | **4 d 1 h** | **37 min** |
| Debt Ratio (Overall Code) | 0.3 % | 0.1 % |
| Rating (Overall Code) | A | A |
| Effort to Reach A | 0 | 0 |

**Lectura de la tabla:**

- El salto de **258 → 14** *code smells* y de **4 d 1 h → 37 min** de `Debt` muestra visualmente el efecto de pagar deuda técnica de forma sostenida: no solo bajan los issues, baja proporcionalmente el tiempo estimado de remediación.
- Es notable que en **ambos** casos el `Rating` se mantenga en A: con un Debt Ratio de 0.3 % y de 0.1 %, ambos están muy por debajo del umbral del 5 %. Esto ilustra un matiz importante — **un Rating A no significa cero deuda**, significa que la deuda es proporcionalmente pequeña frente al tamaño del código (recordar que Debt Ratio normaliza por líneas de código). El caso de "deuda alta" del ejemplo (`4 d 1 h`) es alto en términos absolutos, pero sigue siendo bajo en términos relativos para el tamaño del proyecto (500–577 archivos).
- El gráfico de burbujas ("Technical Debt" / eje de riesgo) que acompaña ambas capturas ubica cada archivo según su `Debt` en el eje horizontal y su cobertura de pruebas en el eje vertical, con el tamaño de la burbuja proporcional a líneas de código. Es la representación visual de dónde concentrar el esfuerzo de *paydown*: archivos grandes (burbuja grande), con poca cobertura (arriba) y mucho tiempo de deuda (a la derecha) son los candidatos prioritarios.

---

## 4. Por qué expresar la deuda en tiempo (y no solo en cantidad de issues) importa para la gestión

Conectando con el marco conceptual previo:

1. **Permite comparar capital vs. interés en las mismas unidades.** Si el equipo sabe que pagar la deuda actual toma `4 d 1 h`, y estima que una nueva funcionalidad que se apoya en ese código sucio tomará un 20 % más de tiempo por el "interés" de la deuda, ambas cifras son comparables directamente en el sprint planning.
2. **Hace operable la priorización del cuadrante de Fowler.** Un ítem de deuda "imprudente" con `Debt` alto y Rating D/E es candidato inmediato al backlog; un ítem "prudente" con `Debt` bajo puede posponerse sin culpa.
3. **El *Quality Gate* convierte la teoría en control automático de proceso.** Fijar la condición *"Debt Ratio del New Code ≤ 5 %"* en el Quality Gate es la forma concreta de evitar que un equipo acumule deuda técnica inadvertida sprint tras sprint, sin depender del criterio cualitativo de un líder técnico.
4. **Separar New Code de Overall Code operacionaliza "deuda real vs. potencial".** El New Code Rating gobierna que no se genere deuda nueva (deuda real inmediata); el Overall Code Rating es el indicador de salud a largo plazo del pasivo heredado (deuda potencial que se vuelve real si esa zona del código se toca de nuevo).

---

## 5. Referencias

- Cunningham, W. (1992). *The WyCash Portfolio Management System* — origen del concepto de deuda técnica.
- SonarSource. *SQALE Method — Metric Definitions.* Documentación oficial de SonarQube/SonarQube Cloud sobre `Debt`, `Debt Ratio` y `Maintainability Rating`.
- Ruiz, M. (2022). *Deuda Técnica: lo que necesitás saber para poder gestionarla.* Redbee, Medium.
- Fowler, M. *TechnicalDebtQuadrant.* martinfowler.com.
- Capturas de referencia: panel *Measures* de SonarQube Cloud, secciones "New Code" y "Overall Code" (proyecto propio, anonimizado).
