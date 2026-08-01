# Semana 7 — Técnicas de gestión y medición en la calidad de software

**Curso:** Estándares y Métricas de Calidad en el Desarrollo de Software
**Resultados de aprendizaje de la semana:**
1. Establecer las herramientas de calidad de software.
2. Clasificar las técnicas de gestión de la calidad del software.

---

## 1. Técnica, herramienta y estándar: una distinción necesaria

Antes de clasificar nada conviene fijar tres conceptos que suelen usarse como sinónimos sin serlo.

- **Técnica:** procedimiento sistemático y repetible orientado a lograr un objetivo de calidad. Ejemplos: revisión por pares, TDD, pruebas de aceptación.
- **Herramienta:** software o instrumento que automatiza o soporta la ejecución de una técnica. Ejemplos: SonarQube, Karate Framework, JIRA + Xray.
- **Estándar / modelo:** marco de referencia que define qué se debe lograr o cómo organizar el proceso, sin prescribir el procedimiento operativo. Ejemplos: ISO/IEC 25010, CMMI.

El estándar define el marco, la técnica define el método y la herramienta automatiza su ejecución. Ninguno de los tres sustituye a los otros dos.

---

## 2. Eje 1 — ISO/IEC 25010: características del producto

Modelo de calidad del **producto**: responde a la pregunta *¿qué atributo del software mejora esta técnica?*

| Característica | Característica |
|---|---|
| Adecuación funcional | Eficiencia de desempeño |
| Compatibilidad | Usabilidad |
| **Fiabilidad** | **Seguridad** |
| **Mantenibilidad** | Portabilidad |

Las características en negrita son las que las técnicas tratadas en este módulo impactan de forma directa.

---

## 3. Eje 2 — CMMI Nivel 3: áreas de proceso (nivel Definido)

Modelo de madurez de **procesos**: responde a la pregunta *¿en qué área de proceso se institucionaliza esta técnica?*

| Área | Nombre completo | Descripción |
|---|---|---|
| REQM | Requirements Management | Gestión de requisitos: control de cambios y trazabilidad frente al alcance acordado. |
| VER | Verification | El producto se construyó conforme a lo especificado (revisiones, pruebas unitarias). |
| VAL | Validation | El producto satisface su uso previsto (pruebas de aceptación, demos al usuario). |
| PPQA | Process and Product Quality Assurance | Auditorías objetivas de cumplimiento de procesos y estándares definidos. |
| RSKM | Risk Management | Identificación temprana y mitigación de riesgos técnicos del proyecto. |

---

## 4. Cómo se cruzan los dos ejes de clasificación

ISO/IEC 25010 y CMMI nivel 3 no son taxonomías paralelas: una misma técnica se ubica en un único eje de proceso (CMMI) mientras impacta una o varias características de producto (25010) al mismo tiempo.

| Técnica | Característica(s) ISO/IEC 25010 | Área de proceso CMMI N3 |
|---|---|---|
| Revisión de código por pares | Mantenibilidad (análisis, modularidad) | Verification (VER) |
| Pruebas unitarias automatizadas (TDD) | Fiabilidad, adecuación funcional | Verification (VER) |
| Pruebas de aceptación (Karate / BDD) | Adecuación funcional, compatibilidad | Validation (VAL) |
| Quality gate de análisis estático en CI | Mantenibilidad, seguridad | Process and Product QA (PPQA) |
| Matriz de trazabilidad requisito-prueba | Adecuación funcional (completitud) | Requirements Mgmt (REQM) |
| Auditorías periódicas de QA | Transversal (todas las características) | Process and Product QA (PPQA) |
| Gestión de riesgos técnicos por sprint | Fiabilidad (tolerancia a fallos) | Risk Management (RSKM) |

---

## 5. Herramientas que ponen estas técnicas en práctica

- **SonarQube** — análisis estático continuo en el pipeline de CI: detecta code smells, vulnerabilidades y deuda técnica antes del merge.
- **Diagrama de Ishikawa** — análisis de causa raíz en retrospectivas; clasifica causas en personas, proceso, tecnología y datos.
- **Diagrama de Pareto** — prioriza el grupo de categorías de defectos que concentra la mayor parte de los problemas reportados.
- **Karate Framework** — pruebas de aceptación de APIs en estilo BDD sobre Java/Gradle; retroalimentación rápida en arquitecturas de microservicios.
- **JIRA + Xray** — gestión de casos de prueba y matriz de trazabilidad entre requisitos y pruebas.

La herramienta se elige *después* del diagnóstico: el instrumento se selecciona según la causa raíz identificada con Pareto o Ishikawa, no según la tendencia tecnológica del momento.

---

## 6. Métricas que hacen tangible la calidad

Valores ilustrativos, construidos con fines didácticos sobre un horizonte de tres releases.

| Métrica | Antes | Después | Variación |
|---|---|---|---|
| Densidad de defectos (defectos/KLOC) | 4.8 | 1.6 | -67% |
| Cobertura de pruebas automatizadas | 22% | 81% | +268% |
| Eficiencia de detección de defectos (DDE) | 54% | 89% | +35 pts |
| Defectos escapados a producción / release | 38 | 7 | -82% |
| Tiempo medio de resolución (MTTR) | 6.4 días | 1.8 días | -72% |
| Complejidad ciclomática promedio | 18 | 9 | -50% |
| Net Promoter Score (NPS) | 21 | 47 | +124% |

---

## 7. Caso aplicado: DataFlow Solutions — InventoryPro v3

**Contexto.** DataFlow Solutions S.A.S., empresa de desarrollo de software de Bucaramanga (45 empleados), desarrolla InventoryPro v3, la migración de un monolito de gestión de inventario y pedidos hacia una arquitectura de microservicios. El equipo está compuesto por 14 personas en un squad Scrum (3 backend, 3 frontend, 2 QA, 1 DevOps, PO, SM, UX y soporte). La meta de negocio es escalar de 230 a 600 clientes PYME en 18 meses.

**Problema identificado.** Antes de aplicar las técnicas anteriores, la empresa reportaba 38 defectos críticos en producción durante las primeras dos semanas de cada release, con un tiempo medio de resolución de 6.4 días. El NPS cayó de 42 a 21 en dos releases consecutivos. Las causas raíz: ausencia de análisis estático, cobertura de pruebas de apenas 22% y requisitos sin trazabilidad formal.

## 8. Resultados del caso

- **+35 puntos porcentuales** en detección temprana de defectos (de 54% a 89%).
- **-58%** en costo de retrabajo (cost of poor quality).
- **-33%** en duración del ciclo de release (de 6 a 4 semanas).
- **-61%** en tickets de soporte por errores de software.

---

## 9. Conclusiones

1. La calidad se gestiona en dos planos complementarios: el producto (ISO/IEC 25010) y el proceso (CMMI); confundirlos lleva a decisiones equivocadas en el equipo.
2. Herramienta, técnica y estándar no son sinónimos: el estándar define el marco, la técnica define el método y la herramienta automatiza su ejecución.
3. La detección temprana es la métrica de mayor impacto económico: el costo de corregir un defecto crece según la etapa en que se detecta.
4. Institucionalizar procesos definidos (CMMI Nivel 3) sostiene la calidad pese a la rotación del equipo, y es cada vez más un requisito comercial, no solo técnico.

*Nota metodológica: las cifras del caso aplicado son hipotéticas y construidas con fines didácticos; no aíslan el efecto individual de cada técnica ni controlan por otras variables. Vale la pena discutir en clase qué evidencia adicional se necesitaría para afirmar causalidad y no solo correlación.*
