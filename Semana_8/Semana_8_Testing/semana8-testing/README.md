# Semana 8 — Aseguramiento de Calidad con Pruebas
### Curso: Estándares y Métricas de Calidad en el Desarrollo de Software
### UNIMINUTO — Especialización en Desarrollo de Software

---

## Contexto del proyecto

Este proyecto implementa un servicio CRUD de gestión de usuarios para ilustrar
los **tres niveles de la pirámide de pruebas**. La aplicación es intencionalmente
simple para que el foco esté en las pruebas, no en la lógica de negocio.

```
src/
├── main/java/com/uniminuto/usuarios/
│   ├── model/          → Usuario.java (entidad + reglas de modelo)
│   ├── exception/      → UsuarioNoEncontradoException.java
│   ├── repository/     → UsuarioRepositorio.java (interfaz)
│   │                   → UsuarioRepositorioH2.java (implementación JDBC+H2)
│   └── service/        → UsuarioServicio.java (lógica de negocio)
│
└── test/
    ├── java/com/uniminuto/usuarios/
    │   ├── unit/           → UsuarioServicioTest.java      (JUnit 5 + Mockito)
    │   ├── integration/    → UsuarioIntegracionTest.java   (JUnit 5 + H2)
    │   └── acceptance/
    │       ├── AceptacionSuite.java                        (runner Cucumber)
    │       └── steps/GestionUsuariosSteps.java             (step definitions)
    └── resources/
        └── features/
            └── gestion_usuarios.feature                   (escenarios BDD)
```

---

## Cómo ejecutar las pruebas

### Todos los tipos a la vez
```bash
mvn test
```

### Solo pruebas unitarias
```bash
mvn test -Dtest=UsuarioServicioTest
```

### Solo pruebas de integración
```bash
mvn test -Dtest=UsuarioIntegracionTest
```

### Solo pruebas de aceptación (Cucumber)
```bash
mvn test -Dtest=AceptacionSuite
```

### Reporte HTML de Cucumber (después de ejecutar)
```
target/cucumber-report.html
```

---

## La pirámide de pruebas — por qué importa el orden

```
         ╱▔▔▔▔▔▔▔▔▔▔▔▔▔╲
        ╱  Aceptación    ╲    ← Pocas (5-10)
       ╱   (BDD/Cucumber)  ╲    Lentas, validan criterios de usuario
      ╱▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔╲
     ╱    Integración        ╲  ← Algunas (10-30)
    ╱   (JUnit + H2)          ╲   Velocidad media, detectan errores de interfaz
   ╱▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔╲
  ╱        Unitarias            ╲ ← Muchas (100+)
 ╱  (JUnit + Mockito)            ╲  Rápidas, aíslan defectos de lógica
╱▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔╲
```

**La pirámide no es solo una metáfora visual.**
La proporción refleja un equilibrio de costo/beneficio:
cuanto más alto en la pirámide, más costoso mantener, más lento ejecutar,
y más difícil diagnosticar dónde falló. Las unitarias son la base
porque son baratas de escribir y ejecutar, y precisan el defecto exacto.

---

## Tabla comparativa: los tres tipos de prueba

| Dimensión | Unitarias | Integración | Aceptación |
|---|---|---|---|
| **¿Qué verifica?** | Lógica de negocio aislada | Interacción entre componentes | Criterios de valor para el usuario |
| **Autores típicos** | Desarrollador | Desarrollador / QA | QA + Product Owner |
| **Lenguaje** | Java (código) | Java (código) | Gherkin (lenguaje natural) |
| **Herramientas** | JUnit 5 + Mockito | JUnit 5 + H2 | Cucumber + JUnit 5 |
| **Dependencias externas** | Ninguna (todo es mock) | BD en memoria (H2) | BD en memoria (H2) |
| **Velocidad** | < 50 ms por prueba | 100–500 ms por prueba | 500 ms–2 s por escenario |
| **Granularidad** | Un método / una regla | Un flujo / una operación | Un criterio de aceptación de historia |
| **¿Usa mocks?** | Sí, obligatoriamente | No (componentes reales) | No (sistema completo) |
| **¿Qué detectan mejor?** | Bugs en lógica pura | Errores de SQL, mapeo, constraints | Comportamiento incorrecto desde perspectiva del usuario |
| **¿Qué NO detectan?** | Errores de integración | Bugs de lógica de negocio aislada | Dónde exactamente está el defecto |
| **Proporción recomendada** | 70% del total | 20% del total | 10% del total |
| **Dónde viven en el repo** | `src/test/.../unit/` | `src/test/.../integration/` | `src/test/resources/features` |
| **Relación con ISO 25010** | Funcionalidad, mantenibilidad | Fiabilidad, compatibilidad | Usabilidad, adecuación funcional |

---

## Preguntas para reflexión en clase

1. ¿Por qué las pruebas de aceptación usan componentes reales y las unitarias usan mocks?
   ¿No sería más "seguro" que todas usaran componentes reales?

2. Si el test unitario `registrar_conEmailDuplicado_debeLanzarExcepcion` pasa
   pero el de integración `registrar_emailDuplicado_debeLanzarExcepcionEnCualquierCapa` falla,
   ¿qué nos dice eso sobre la ubicación del defecto?

3. Los escenarios `.feature` son ejecutables. ¿Cómo cambia esto la conversación
   entre desarrolladores y clientes comparado con tener casos de uso en Word?

4. En ISO/IEC 25010, ¿qué subcaracterística de **fiabilidad** están cubriendo
   principalmente las pruebas de integración de este proyecto?

---

## Bibliografía relacionada

- Beck, K. (2003). *Test-Driven Development: By Example*. Addison-Wesley.
- Meszaros, G. (2007). *xUnit Test Patterns*. Addison-Wesley.
- Smart, J. F. (2014). *BDD in Action*. Manning Publications.
- ISO/IEC 25010:2023 — *Systems and software Quality Requirements and Evaluation (SQuaRE)*.
