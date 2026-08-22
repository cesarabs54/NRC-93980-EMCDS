# Tutorial: medir la deuda técnica con SonarQube

Guía paso a paso para analizar **backend_salida_bienes** (Java 21 / Spring Boot 3.4.1 / Maven) con SonarQube y obtener una medición automática de deuda técnica (rating SQALE), bugs, vulnerabilidades, code smells, duplicación y cobertura.

> Ya existe un análisis manual en [`02_DEUDA_TECNICA_Reporte_analisis_manual.md`](02_DEUDA_TECNICA_Reporte_analisis_manual.md) (20 hallazgos, ~127 h estimadas). Este tutorial sirve para contrastar ese informe con una herramienta automatizada y dejarlo repetible en cada cambio.

---

## 0. Qué vas a obtener al final

- Un SonarQube corriendo en `http://localhost:9000`.
- El proyecto `backend` analizado con métricas de:
  - **Reliability** (bugs), **Security** (vulnerabilidades + security hotspots), **Maintainability** (code smells).
  - **Technical Debt** en horas/minutos (misma filosofía SQALE que se usó a mano en `02_DEUDA_TECNICA_Reporte_analisis_manual.md`).
  - **Coverage** (requiere añadir JaCoCo, ver paso 4.1) y **Duplications**.
- Un **Quality Gate** (pasa/no pasa) que puedes usar como criterio de PR.

---

## 1. Prerrequisitos

| Herramienta | Ya lo tienes en el proyecto | Notas |
|---|---|---|
| Java 21 | ✅ (`pom.xml`) | Necesario para compilar y para el propio scanner |
| Maven | ✅ (`pom.xml`) | Usa el `mvnw`/`mvnw.cmd` si existe, o Maven instalado |
| Docker Desktop | Ya usan Docker (hay `Dockerfile` en la raíz) | Es la forma más simple de levantar SonarQube localmente |
| MySQL local (`f02`) | ✅ | No hace falta para el análisis estático; sólo si luego quieres correr tests de integración |

Verifica versiones:

```bash
java -version
mvn -version
docker --version
```

Si no usas Docker, la alternativa sin instalar nada es **SonarCloud** (paso 7).

---

## 2. Levantar SonarQube (Community Edition) con Docker

```bash
docker run -d --name sonarqube ^
  -p 9000:9000 ^
  -v sonarqube_data:/opt/sonarqube/data ^
  -v sonarqube_extensions:/opt/sonarqube/extensions ^
  -v sonarqube_logs:/opt/sonarqube/logs ^
  sonarqube:community

docker run -d --name sonarqube -p 9000:9000 -v sonarqube_data:/opt/sonarqube/data -v sonarqube_extensions:/opt/sonarqube/extensions -v sonarqube_logs:/opt/sonarqube/logs sonarqube:community
```

(En PowerShell puedes escribir el comando en una sola línea si el `^` te da problemas.)

Espera ~1 minuto y verifica que está sano:

```bash
docker logs -f sonarqube
```

Busca la línea `SonarQube is operational`. Luego abre en el navegador:

```
http://localhost:9000
```

**Credenciales por defecto:** usuario `admin`, contraseña `admin` (te pedirá cambiarla en el primer login).

> Si el contenedor no arranca por `vm.max_map_count`, en Docker Desktop con WSL2 ejecuta una vez desde una terminal WSL:
> ```bash
> wsl -d docker-desktop
> sysctl -w vm.max_map_count=262144
> ```

---

## 3. Crear el proyecto y generar el token

1. En la UI de SonarQube: **Create Project → Local Project**.
2. **Project display name:** `backend-salida-bienes` (o el que prefieras). **Project key:** por ejemplo `salidabienes-backend`.
3. Elige **"Use the global setting"** o `main` como rama principal (coincide con la rama `main` del repo).
4. En "Provide a token", genera un token nuevo (ej. `backend-salida-bienes-token`) y **cópialo** — solo se muestra una vez.
5. Selecciona **Maven** como método de análisis: SonarQube te mostrará el comando exacto (lo replicamos en el paso 5).

Si prefieres generarlo aparte: **My Account → Security → Generate Tokens**.

---

## 4. Preparar el proyecto Maven

El `pom.xml` actual no tiene ni JaCoCo ni el plugin de Sonar. Añádelos así (no rompe nada existente):

### 4.1 JaCoCo (para que Sonar reporte cobertura)

Dentro de `<properties>`:

```xml
<sonar.coverage.jacoco.xmlReportPaths>
    ${project.basedir}/target/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>
```

Dentro de `<plugins>` (mismo bloque `<build>` donde están `spring-boot-maven-plugin`, `maven-compiler-plugin`, etc.):

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

> Como se documenta en `02_DEUDA_TECNICA_Reporte_analisis_manual.md` (sección 1.1), hoy solo existe `BackendApplicationTests.contextLoads()`. Sin más tests, JaCoCo reportará cobertura ≈ 0 %, lo cual es correcto y es justamente la métrica que queremos que Sonar confirme automáticamente.

### 4.2 Plugin de Sonar para Maven

También en `<plugins>`:

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>4.0.0.4121</version>
</plugin>
```

No necesitas más configuración en el XML: el `project key`, la URL y el token se pasan por línea de comandos (paso 5), así el `pom.xml` no queda atado a credenciales.

---

## 5. Ejecutar el análisis

Desde la raíz del proyecto (`backend_salida_bienes`):

```bash
mvn clean verify sonar:sonar ^
  -Dsonar.projectKey=salidabienes-backend ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.token=TU_TOKEN_AQUI
```

```bash
mvn clean verify sonar:sonar -Dsonar.projectKey=salidabienes-backend -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN_AQUI
```

Qué hace cada fase:

1. `clean verify` → compila, corre los tests existentes (`BackendApplicationTests`) y genera el reporte JaCoCo (`target/site/jacoco/jacoco.xml`).
2. `sonar:sonar` → sube el análisis (código + cobertura) al SonarQube local.

Al terminar verás en consola:

```
INFO: ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=salidabienes-backend
```

Abre ese enlace.

---

## 6. Leer el dashboard

En `http://localhost:9000/dashboard?id=salidabienes-backend` vas a ver:

| Métrica | Qué significa aquí | Dónde mirar primero en este proyecto |
|---|---|---|
| **Reliability (Bugs)** | Errores probables de runtime | Revisa los `try/catch` que re-lanzan `RuntimeException` sin envolver (ver `02_DEUDA_TECNICA_Reporte_analisis_manual.md` 3.3) |
| **Security (Vulnerabilities + Hotspots)** | Riesgos de seguridad | Debería marcar algo relacionado con `FileStorageService.getPdfPath` (path traversal, sección 2.2) y el secreto JWT hardcodeado (2.1) como *Security Hotspot* |
| **Maintainability (Code Smells) → Technical Debt** | Minutos/horas estimadas para "limpiar" el código (rating A–E, SQALE) | Este es el número comparable con las ~127 h del informe manual |
| **Coverage** | % de líneas/branches cubiertas por tests | Debería salir muy bajo/0 %, confirmando la sección 1.1 |
| **Duplications** | % de código duplicado | Útil para detectar mappers o controllers copy-pasteados |

Haz clic en **"Code"** (panel izquierdo) para navegar archivo por archivo y ver los *issues* marcados en línea, con severidad (`Blocker`, `Critical`, `Major`, `Minor`, `Info`) y el tiempo de remediación estimado por issue — es el mismo criterio ("remediation cost") que se usó a mano en el informe existente.

### 6.1 Quality Gate

Por defecto usa **"Sonar way"**: falla si hay nuevo código con cobertura <80 %, duplicación >3 % o issues de severidad alta sin resolver. Puedes:
- Dejarlo así y usarlo como gate de PR más adelante.
- O crear uno propio en **Quality Gates → Create** ajustado a la realidad actual del proyecto (por ejemplo, no exigir 80 % de cobertura de entrada, sino un umbral progresivo).

---

## 7. Alternativa sin instalar nada: SonarCloud

Si no quieres correr Docker localmente:

1. Entra a [sonarcloud.io](https://sonarcloud.io) y conecta tu cuenta de GitHub.
2. **+ → Analyze new project** y selecciona el repositorio (necesita estar en GitHub/GitLab/Bitbucket; si el remoto aún es solo local, primero haz push).
3. Sigue el asistente "With GitHub Actions" o "Locally" — para análisis local te da un comando casi idéntico al del paso 5, cambiando `sonar.host.url` por `https://sonarcloud.io` y usando `sonar.organization`.
4. El resto de la lectura del dashboard (paso 6) es idéntica.

Ventaja: no consume recursos locales y se integra fácil con GitHub Actions para analizar cada PR automáticamente.

---

## 8. (Opcional) Automatizar el análisis en cada cambio

Con SonarCloud + GitHub Actions, un workflow mínimo (`.github/workflows/sonar.yml`):

```yaml
name: SonarCloud
on:
  push:
    branches: [main, duvan_new_mer]
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonar:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: mvn clean verify sonar:sonar -Dsonar.organization=TU_ORG -Dsonar.projectKey=TU_KEY
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

Esto deja la medición de deuda técnica corriendo en cada PR en vez de ser un análisis puntual — es justo lo que `02_DEUDA_TECNICA_Reporte_analisis_manual.md` deja pendiente en su "Nota metodológica" final.

---

## 9. Comparar con el informe manual existente

Después del primer análisis, contrasta:

- **Deuda total en horas** que reporta Sonar (pestaña *Measures → Maintainability → Technical Debt*) vs. las **~127 h** del informe manual.
- **Rating SQALE** (A–E) que da Sonar vs. el rango **C–D** estimado a mano.
- Revisa si Sonar detecta issues que el informe manual no cubrió (p. ej. complejidad ciclomática en `SolicitudServiceImpl`, que ya se señala como "concentra demasiada responsabilidad" en la sección 6.2).

No van a coincidir exactamente (Sonar no sabe que falta cobertura de negocio específica del dominio SENA, y el informe manual no mide complejidad ciclomática número por número), pero deberían apuntar en la misma dirección: cobertura de pruebas como el hueco más grande, seguido de logging/manejo de errores y code smells de mantenibilidad.

---

## Troubleshooting rápido

| Problema | Causa típica | Solución |
|---|---|---|
| `docker run` falla / contenedor se reinicia solo | `vm.max_map_count` bajo | Ver nota al final del paso 2 |
| `sonar:sonar` falla con 401 | Token inválido o expirado | Regenera el token (paso 3) |
| Coverage aparece en 0 % siempre | Falta ejecutar `test`/`verify` antes de `sonar:sonar`, o falta el plugin JaCoCo | Confirma que corriste `mvn clean verify sonar:sonar` completo (no solo `sonar:sonar`) |
| El análisis tarda mucho | Primera vez indexando 132 archivos Java | Es normal; las siguientes corridas son incrementales |
