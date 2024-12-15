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
    private String facturaId;          // Identificador único de la factura
    private String ticketId;           // ID del ticket asociado
    private String solicitudId;        // ID de la solicitud vinculada
    private String username;           // Usuario que generó la solicitud
    private String estadoTicket;       // Estado del ticket
    private String prioridad;          // Prioridad del ticket
    private String descripcionInicial; // Descripción inicial del problema
    private String descripcionTrabajo; // Trabajo realizado
    private String pago;               // Estado del pago
    private double cotizacion;         // Monto de la cotización
    private String fechaCreacion;      // Fecha de creación (yyyy/MM/dd)
    private String horaCreacion;       // Hora de creación (HH:mm:ss)
}
