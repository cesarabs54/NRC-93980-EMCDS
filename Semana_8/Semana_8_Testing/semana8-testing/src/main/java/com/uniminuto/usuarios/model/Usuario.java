package com.uniminuto.usuarios.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MODELO DE DOMINIO: Usuario
 *
 * Representa la entidad central del sistema. Mantener el modelo
 * separado del repositorio y el servicio es el principio de
 * Separación de Responsabilidades (SoC), que también facilita
 * las pruebas: cada capa se puede probar de forma independiente.
 *
 * Reglas de negocio implícitas en este modelo:
 *  - El email es único e inmutable una vez registrado
 *  - Los nombres no pueden estar vacíos
 *  - La edad mínima para registrarse es 18 años
 */
@Data           // Lombok: genera getters, setters, equals, hashCode, toString
@Builder        // Lombok: patrón Builder → Usuario.builder().nombre("Ana").build()
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    private int edad;
    private boolean activo;

    /**
     * Regla de negocio: un usuario es elegible para servicios premium
     * si es mayor de edad y tiene la cuenta activa.
     *
     * NOTA DIDÁCTICA: Colocar lógica aquí (modelo rico) vs. en el servicio
     * es una decisión de diseño. La ventaja: esta regla es fácilmente
     * testeable sin necesitar ninguna dependencia externa.
     */
    public boolean esElegibleParaPremium() {
        return activo && edad >= 18;
    }
}
