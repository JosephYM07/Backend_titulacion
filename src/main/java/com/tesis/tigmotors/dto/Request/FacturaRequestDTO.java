package com.tesis.tigmotors.dto.Request;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

/**
 * DTO que representa los parámetros de filtrado por fechas.
 */
@Data
public class FacturaRequestDTO {
    private String fechaInicio;
    private String fechaFin;
    private String username;
    private String estadoPago;
    private Boolean generarPdf; // Bandera para decidir si se genera PDF
    private transient HttpServletResponse response;
}