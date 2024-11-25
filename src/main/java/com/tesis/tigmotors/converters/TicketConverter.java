package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Response.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketConverter {

    // Convertir de DTO a Entidad
    public Ticket dtoToEntity(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());  // Asignar el ID del ticket generado
        ticket.setUsername(ticketDTO.getUsername());
        ticket.setSolicitudId(ticketDTO.getSolicitudId()); // Asignar el ID de la solicitud
        ticket.setDescripcion(ticketDTO.getDescripcion());
        ticket.setEstado(ticketDTO.getEstado());
        ticket.setAprobado(ticketDTO.isAprobado());

        // Asignar las descripciones desde el DTO
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
        ticketDTO.setDescripcion(ticket.getDescripcion());
        ticketDTO.setEstado(ticket.getEstado());
        ticketDTO.setAprobado(ticket.isAprobado());

        // Asignar las descripciones desde la entidad
        ticketDTO.setDescripcionInicial(ticket.getDescripcionInicial());
        ticketDTO.setDescripcionTrabajo(ticket.getDescripcionTrabajo());

        return ticketDTO;
    }

}

