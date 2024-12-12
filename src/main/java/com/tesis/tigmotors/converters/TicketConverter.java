package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.SolicitudAdminRequestDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.models.Solicitud;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class TicketConverter {
    // Formateadores para fechas y horas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Método para validar y formatear fecha (String -> LocalDate)
    private LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("La fecha no puede ser nula o vacía.");
        }
        try {
            return LocalDate.parse(fecha, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de fecha debe ser 'yyyy/MM/dd'.");
        }
    }

    // Método para validar y formatear hora (String -> LocalTime)
    private LocalTime parseHora(String hora) {
        if (hora == null || hora.isBlank()) {
            throw new IllegalArgumentException("La hora no puede ser nula o vacía.");
        }
        try {
            return LocalTime.parse(hora, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de hora debe ser 'HH:mm:ss'.");
        }
    }

    // Método para formatear fecha (LocalDate -> String)
    private String formatFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : null;
    }

    // Método para formatear hora (LocalTime -> String)
    private String formatHora(LocalTime hora) {
        return hora != null ? hora.format(TIME_FORMATTER) : null;
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
        // Validar y asignar fechas y horas
        if (ticketDTO.getFechaCreacion() != null) {
            ticket.setFechaCreacion(parseFecha(ticketDTO.getFechaCreacion()));
        }
        if (ticketDTO.getHoraCreacion() != null) {
            ticket.setHoraCreacion(parseHora(ticketDTO.getHoraCreacion()));
        }
        return ticket;
    }

    // Convertir de SolicitudAdminRequestDTO a Entidad
    public Ticket adminRequestToEntity(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());
        ticket.setUsername(ticketDTO.getUsername());
        ticket.setSolicitudId(ticketDTO.getSolicitudId());
        ticket.setEstado(ticketDTO.getEstado());
        ticket.setDescripcionInicial(ticketDTO.getDescripcionInicial());
        ticket.setDescripcionTrabajo(ticketDTO.getDescripcionTrabajo());


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
        // Formatear y asignar fechas y horas
        ticketDTO.setFechaCreacion(formatFecha(ticket.getFechaCreacion()));
        ticketDTO.setHoraCreacion(formatHora(ticket.getHoraCreacion()));

        return ticketDTO;

    }

}

