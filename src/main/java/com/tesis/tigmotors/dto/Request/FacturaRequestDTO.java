package com.tesis.tigmotors.dto.Request;

import lombok.Data;

/**
 * DTO que representa los parámetros de filtrado por fechas.
 */
@Data
public class FacturaRequestDTO {
    private String fechaInicio; // Formato: yyyy/MM/dd
    private String fechaFin;
    private String usuario;     // Username del usuario
    private String estadoPago;// Formato: yyyy/MM/dd
}