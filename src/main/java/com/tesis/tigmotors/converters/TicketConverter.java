package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class TicketConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Asignar automáticamente fecha y hora formateadas al ticket
    public void asignarFechaYHoraActual(Ticket ticket) {
        ticket.setFechaCreacion(LocalDate.now().format(DATE_FORMATTER));
        ticket.setHoraCreacion(LocalTime.now().format(TIME_FORMATTER));
    }

    // Convertir de DTO a Entidad
    public Ticket dtoToEntity(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());
        ticket.setUsername(ticketDTO.getUsername());
        ticket.setSolicitudId(ticketDTO.getSolicitudId());
        ticket.setEstado(ticketDTO.getEstado());
        ticket.setDescripcionInicial(ticketDTO.getDescripcionInicial());
        ticket.setDescripcionTrabajo(ticketDTO.getDescripcionTrabajo());

        // Validar y asignar fechas y horas si están presentes
        if (ticketDTO.getFechaCreacion() != null) {
            ticket.setFechaCreacion(ticketDTO.getFechaCreacion());
        }
        if (ticketDTO.getHoraCreacion() != null) {
            ticket.setHoraCreacion(ticketDTO.getHoraCreacion());
        }
        return ticket;
    }

    // Convertir de Entidad a DTO
    public TicketDTO entityToDto(Ticket ticket) {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(ticket.getId());
        ticketDTO.setUsername(ticket.getUsername());
        ticketDTO.setSolicitudId(ticket.getSolicitudId());
        ticketDTO.setEstado(ticket.getEstado());
        ticketDTO.setDescripcionInicial(ticket.getDescripcionInicial());
        ticketDTO.setDescripcionTrabajo(ticket.getDescripcionTrabajo());

        // Formatear fechas y horas para el DTO
        ticketDTO.setFechaCreacion(ticket.getFechaCreacion());
        ticketDTO.setHoraCreacion(ticket.getHoraCreacion());

        return ticketDTO;
    }
}
