package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDetalleResponseDTO {
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
