package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Response.FacturaDetalleResponseDTO;
import com.tesis.tigmotors.enums.EstadoPago;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Conversor para manejar la conversión de datos entre las entidades Factura y DTOs relacionados.
 */
@Component
public class FacturaConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Genera una entidad Factura a partir de un Ticket y un monto de cotización.
     *
     * @param ticket Objeto Ticket del cual se generan los datos de la factura.
     * @param cotizacion Monto de la cotización asociado a la factura.
     * @return Objeto Factura construido con los datos del Ticket y la cotización.
     */
    public Factura generarFacturaDesdeTicket(Ticket ticket, double cotizacion) {
        return Factura.builder()
                .comprobanteId(null)
                .ticketId(ticket.getId())
                .solicitudId(ticket.getSolicitudId())
                .username(ticket.getUsername())
                .estadoTicket(ticket.getEstado())
                .prioridad(ticket.getPrioridad())
                .descripcionInicial(ticket.getDescripcionInicial())
                .descripcionTrabajo(ticket.getDescripcionTrabajo())
                .pago(EstadoPago.PENDIENTE_PAGO.name())
                .cotizacion(cotizacion)
                .fechaCreacion(LocalDate.now().format(DATE_FORMATTER))
                .horaCreacion(LocalTime.now().format(TIME_FORMATTER))
                .build();
    }

    /**
     * Convierte una entidad Factura en un DTO de respuesta detallada.
     *
     * @param factura Entidad Factura a convertir.
     * @return DTO FacturaDetalleResponseDTO con los datos de la factura.
     */
    public FacturaDetalleResponseDTO entityToDto(Factura factura) {
        FacturaDetalleResponseDTO responseDTO = new FacturaDetalleResponseDTO();
        responseDTO.setComprobanteId(factura.getComprobanteId());
        responseDTO.setTicketId(factura.getTicketId());
        responseDTO.setSolicitudId(factura.getSolicitudId());
        responseDTO.setUsername(factura.getUsername());
        responseDTO.setEstadoTicket(factura.getEstadoTicket());
        responseDTO.setPrioridad(factura.getPrioridad());
        responseDTO.setDescripcionInicial(factura.getDescripcionInicial());
        responseDTO.setDescripcionTrabajo(factura.getDescripcionTrabajo());
        responseDTO.setCotizacion(factura.getCotizacion());
        responseDTO.setFechaCreacion(factura.getFechaCreacion());
        responseDTO.setHoraCreacion(factura.getHoraCreacion());
        responseDTO.setEstadoPago(factura.getPago()); // Estado del pago
        return responseDTO;
    }
}