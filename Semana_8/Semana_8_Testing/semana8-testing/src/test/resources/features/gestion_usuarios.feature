# language: es

# ╔══════════════════════════════════════════════════════════════════╗
# ║              PRUEBAS DE ACEPTACIÓN — FORMATO BDD                ║
# ╠══════════════════════════════════════════════════════════════════╣
# ║  Este archivo .feature es el CONTRATO entre el equipo de        ║
# ║  desarrollo y el cliente/usuario.                               ║
# ║                                                                  ║
# ║  Ventaja clave: es legible por personas NO técnicas.            ║
# ║  Un Product Owner puede revisar y validar estos escenarios.     ║
# ║                                                                  ║
# ║  Herramienta: Cucumber (BDD)                                    ║
# ║   · "dado" (Given)  → estado inicial del sistema               ║
# ║   · "cuando" (When) → acción del usuario                       ║
# ║   · "entonces" (Then) → resultado esperado / criterio de éxito  ║
# ╚══════════════════════════════════════════════════════════════════╝

Característica: Gestión de usuarios del sistema
  Como administrador del sistema
  Quiero poder registrar y gestionar usuarios
  Para mantener control de acceso y datos actualizados

  # ──────────────────────────────────────────────────────────────────
  # ESCENARIO 1: Criterio de aceptación del cliente
  # Historia: "Como usuario nuevo, quiero registrarme con mis datos
  #            para poder acceder al sistema"
  # ──────────────────────────────────────────────────────────────────
  Escenario: Registro exitoso de un usuario nuevo
    Dado que el sistema no tiene ningún usuario registrado con email "nuevo@sistema.com"
    Cuando registro un usuario con nombre "Valentina Cruz", email "nuevo@sistema.com" y edad 27
    Entonces el sistema confirma el registro con un identificador único
    Y el usuario aparece como activo en el sistema

  # ──────────────────────────────────────────────────────────────────
  # ESCENARIO 2: Protección de datos / regla de unicidad
  # Historia: "Como sistema, debo evitar registros duplicados
  #            para mantener la integridad de los datos"
  # ──────────────────────────────────────────────────────────────────
  Escenario: Rechazo de registro con email ya existente
    Dado que ya existe un usuario registrado con email "existente@sistema.com"
    Cuando intento registrar otro usuario con el mismo email "existente@sistema.com"
    Entonces el sistema rechaza el registro
    Y el sistema informa que el email ya está en uso

  # ──────────────────────────────────────────────────────────────────
  # ESCENARIO 3: Desactivación de cuenta
  # Historia: "Como administrador, quiero desactivar usuarios
  #            sin perder su historial en el sistema"
  # ──────────────────────────────────────────────────────────────────
  Escenario: Desactivación de un usuario existente
    Dado que existe un usuario activo con email "temporal@sistema.com"
    Cuando desactivo al usuario con email "temporal@sistema.com"
    Entonces el usuario con email "temporal@sistema.com" aparece como inactivo
    Y el usuario sigue existiendo en el sistema (no fue eliminado)

  # ──────────────────────────────────────────────────────────────────
  # ESCENARIO 4: Elegibilidad para servicios premium
  # Historia: "Como usuario, quiero saber si puedo acceder a premium"
  # ──────────────────────────────────────────────────────────────────
  Escenario: Usuario adulto activo es elegible para premium
    Dado que tengo un usuario activo con 18 años de edad
    Cuando consulto si el usuario es elegible para servicios premium
    Entonces el sistema confirma que el usuario es elegible para premium

  Escenario: Usuario menor de edad no es elegible para premium
    Dado que tengo un usuario activo con 17 años de edad
    Cuando consulto si el usuario es elegible para servicios premium
    Entonces el sistema confirma que el usuario NO es elegible para premium
