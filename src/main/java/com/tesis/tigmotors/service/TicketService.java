package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.TicketConverter;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Response.Ticket;
import com.tesis.tigmotors.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketConverter ticketConverter;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    public TicketDTO crearTicket(TicketDTO ticketDTO, String username) {
        try {
            Ticket ticket = ticketConverter.dtoToEntity(ticketDTO);
            ticket.setId("TICKET-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.TICKET_SEQUENCE));
            ticket.setUsername(username);
            Ticket ticketGuardado = ticketRepository.save(ticket);
            return ticketConverter.entityToDto(ticketGuardado);
        } catch (Exception e) {
            logger.error("Error creando el ticket: ", e);
            throw new RuntimeException("Error creando el ticket");
        }
    }

    public List<TicketDTO> obtenerHistorialTickets(String username) {
        try {
            List<Ticket> tickets = ticketRepository.findByUsername(username);
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo el historial de tickets: ", e);
            throw new RuntimeException("Error obteniendo el historial de tickets");
        }
    }

    public TicketDTO modificarTicket(String ticketId, TicketDTO ticketDTO, String username) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            if (!ticket.isAprobado() && ticket.getUsername().equals(username)) {
                ticket.setDescripcion(ticketDTO.getDescripcion());
                ticket.setEstado(ticketDTO.getEstado());
                Ticket ticketModificado = ticketRepository.save(ticket);
                return ticketConverter.entityToDto(ticketModificado);
            } else {
                throw new RuntimeException("El ticket ya ha sido aprobado y no puede ser modificado o no pertenece al usuario");
            }
        } catch (Exception e) {
            logger.error("Error modificando el ticket: ", e);
            throw new RuntimeException("Error modificando el ticket");
        }
    }

    public void eliminarTicket(String ticketId, String username) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            if (!ticket.isAprobado() && ticket.getUsername().equals(username) && ticket.getEstado().equals("Pendiente")) {
                ticketRepository.delete(ticket);
            } else {
                throw new RuntimeException("El ticket ya ha sido aprobado y no puede ser eliminado o no pertenece al usuario");
            }
        } catch (Exception e) {
            logger.error("Error eliminando el ticket: ", e);
            throw new RuntimeException("Error eliminando el ticket");
        }
    }

    //Metodo para solo administradores
    public TicketDTO aprobarTicket(String ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            if (!ticket.isAprobado() && ticket.getEstado().equals("Pendiente")) {
                ticket.setAprobado(true);
                ticket.setEstado("Aprobado");
                Ticket ticketAprobado = ticketRepository.save(ticket);
                return ticketConverter.entityToDto(ticketAprobado);
            } else {
                throw new RuntimeException("El ticket ya está aprobado o no está en estado 'Pendiente'");
            }
        } catch (Exception e) {
            logger.error("Error aprobando el ticket: ", e);
            throw new RuntimeException("Error aprobando el ticket");
        }
    }

    // Rechazar ticket (administrador)
    public TicketDTO rechazarTicket(String ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            if (!ticket.isAprobado() && ticket.getEstado().equals("Pendiente")) {
                ticket.setEstado("Rechazado");
                Ticket ticketRechazado = ticketRepository.save(ticket);
                return ticketConverter.entityToDto(ticketRechazado);
            } else {
                throw new RuntimeException("El ticket ya está aprobado o no está en estado 'Pendiente'");
            }
        } catch (Exception e) {
            logger.error("Error rechazando el ticket: ", e);
            throw new RuntimeException("Error rechazando el ticket");
        }
    }
}