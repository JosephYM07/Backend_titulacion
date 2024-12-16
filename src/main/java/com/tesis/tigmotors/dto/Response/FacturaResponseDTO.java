package com.tesis.tigmotors.dto.Response;

import lombok.Data;

@Data
public class FacturaResponseDTO {
    private String facturaId;
    private String ticketId;
    private String solicitudId;
    private String username;
    private String estadoTicket;
    private String prioridad;
    private String descripcionInicial;
    private String descripcionTrabajo;
    private double cotizacion;
    private String fechaCreacion;
    private String horaCreacion;
    private String estadoPago;
}
