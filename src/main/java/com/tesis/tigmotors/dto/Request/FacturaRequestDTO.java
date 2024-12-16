package com.tesis.tigmotors.dto.Request;

import lombok.Data;

/**
 * DTO que representa los parámetros de filtrado por fechas.
 */
@Data
public class FacturaRequestDTO {
    private String fechaInicio; // Formato: yyyy/MM/dd
    private String fechaFin;    // Formato: yyyy/MM/dd
}