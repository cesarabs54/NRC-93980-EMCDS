package com.uniminuto.usuarios.acceptance;

import org.junit.platform.suite.api.*;

/**
 * PUNTO DE ENTRADA para las pruebas de aceptación con Cucumber.
 *
 * Esta clase NO contiene lógica de prueba: solo configura cómo Cucumber
 * descubre y ejecuta los escenarios .feature.
 *
 * Opciones de configuración:
 *  · features    → dónde buscar los archivos .feature
 *  · glue        → dónde están los Step Definitions
 *  · plugin      → formatos de reporte (pretty, html, json)
 *
 * Ejecución:
 *  · mvn test -Dtest=AceptacionSuite
 *  · O clic derecho en IntelliJ IDEA → Run 'AceptacionSuite'
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = "cucumber.glue",
    value = "com.uniminuto.usuarios.acceptance.steps"
)
@ConfigurationParameter(
    key = "cucumber.plugin",
    value = "pretty, html:target/cucumber-report.html"
)
@ConfigurationParameter(
    key = "cucumber.publish.quiet",
    value = "true"
)
public class AceptacionSuite {
    // Clase vacía — Cucumber usa la configuración de las anotaciones
}
