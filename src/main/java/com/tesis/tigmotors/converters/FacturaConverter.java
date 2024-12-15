package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class FacturaConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Factura generarFacturaDesdeTicket(Ticket ticket, double cotizacion) {
        return Factura.builder()
                .facturaId(null)
                .ticketId(ticket.getId())
                .solicitudId(ticket.getSolicitudId())
                .username(ticket.getUsername())
                .estadoTicket(ticket.getEstado())
                .prioridad(ticket.getPrioridad())
                .descripcionInicial(ticket.getDescripcionInicial())
                .descripcionTrabajo(ticket.getDescripcionTrabajo())
                .pago(ticket.getPago())
                .cotizacion(cotizacion)
                .fechaCreacion(LocalDate.now().format(DATE_FORMATTER))
                .horaCreacion(LocalTime.now().format(TIME_FORMATTER))
                .build();
    }
}