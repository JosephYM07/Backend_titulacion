package com.tesis.tigmotors.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "factura_pagos")
public class Factura {

    @Id
    private String comprobanteId;
    private String ticketId;           // ID del ticket asociado
    private String solicitudId;
    private String username;
    private String estadoTicket;
    private String prioridad;
    private String descripcionInicial;
    private String descripcionTrabajo;
    private String pago;
    private double cotizacion;
    private String fechaCreacion;
    private String horaCreacion;
}
