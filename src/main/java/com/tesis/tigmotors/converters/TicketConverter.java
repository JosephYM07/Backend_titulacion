package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketConverter {

    // Convertir de DTO a Entidad
    public Ticket dtoToEntity(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setId(ticketDTO.getId());
        ticket.setUsername(ticketDTO.getUsername());
        ticket.setSolicitudId(ticketDTO.getSolicitudId());
        ticket.setEstado(ticketDTO.getEstado());
        ticket.setAprobado(ticketDTO.isAprobado());
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
        ticketDTO.setAprobado(ticket.isAprobado());
        ticketDTO.setDescripcionInicial(ticket.getDescripcionInicial());
        ticketDTO.setDescripcionTrabajo(ticket.getDescripcionTrabajo());
        return ticketDTO;
    }

}

