package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Response.TicketDTO;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Conversor para manejar la transformación entre entidades Ticket y sus respectivos DTOs.
 */
@Component
public class TicketConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Asigna la fecha y hora actuales formateadas a un ticket.
     *
     * @param ticket Entidad Ticket a la que se le asignará la fecha y hora actuales.
     */
    public void asignarFechaYHoraActual(Ticket ticket) {
        ticket.setFechaCreacion(LocalDate.now().format(DATE_FORMATTER));
        ticket.setHoraCreacion(LocalTime.now().format(TIME_FORMATTER));
    }

    /**
     * Convierte un DTO de ticket a una entidad Ticket.
     *
     * @param ticketDTO DTO TicketDTO a convertir.
     * @return Entidad Ticket con los datos del DTO.
     */
    public Ticket dtoToEntity(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());
        ticket.setUsername(ticketDTO.getUsername());
        ticket.setSolicitudId(ticketDTO.getSolicitudId());
        ticket.setPrioridad(ticketDTO.getPrioridad());
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

    /**
     * Convierte una entidad Ticket a un DTO de ticket.
     *
     * @param ticket Entidad Ticket a convertir.
     * @return DTO TicketDTO con los datos de la entidad.
     */
    public TicketDTO entityToDto(Ticket ticket) {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(ticket.getId());
        ticketDTO.setUsername(ticket.getUsername());
        ticketDTO.setSolicitudId(ticket.getSolicitudId());
        ticketDTO.setPrioridad(ticket.getPrioridad());
        ticketDTO.setEstado(ticket.getEstado());
        ticketDTO.setDescripcionInicial(ticket.getDescripcionInicial());
        ticketDTO.setDescripcionTrabajo(ticket.getDescripcionTrabajo());

        // Formatear fechas y horas para el DTO
        ticketDTO.setFechaCreacion(ticket.getFechaCreacion());
        ticketDTO.setHoraCreacion(ticket.getHoraCreacion());

        return ticketDTO;
    }

}
