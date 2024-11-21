package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Response.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketConverter {

    public Ticket dtoToEntity(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setUsername(ticketDTO.getUsername());
        ticket.setDescripcion(ticketDTO.getDescripcion());
        ticket.setEstado("Pendiente");
        ticket.setAprobado(ticketDTO.isAprobado());
        return ticket;
    }

    public TicketDTO entityToDto(Ticket ticket) {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(ticket.getId());
        ticketDTO.setUsername(ticket.getUsername());
        ticketDTO.setDescripcion(ticket.getDescripcion());
        ticketDTO.setEstado(ticket.getEstado());
        ticketDTO.setAprobado(ticket.isAprobado());
        return ticketDTO;
    }
}

