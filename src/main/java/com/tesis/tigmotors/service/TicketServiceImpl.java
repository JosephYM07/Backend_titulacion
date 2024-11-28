package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.TicketConverter;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;

    private final TicketConverter ticketConverter;

    private final SequenceGeneratorService sequenceGeneratorService;

    public TicketDTO crearTicketAutomatico(TicketDTO ticketDTO, String username) {
        try {
            var ticket = ticketConverter.dtoToEntity(ticketDTO);

            // Validar estado inicial
            if (!TicketEstado.isValidEstado(ticketDTO.getEstado())) {
                throw new IllegalArgumentException("Estado del ticket no válido: " + ticketDTO.getEstado());
            }

            ticketDTO.setId("TICKET-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.TICKET_SEQUENCE));
            ticket.setSolicitudId(ticketDTO.getSolicitudId());
            ticket.setUsername(username);
            ticket.setEstado(ticketDTO.getEstado().toUpperCase());
            ticket.setDescripcionInicial(ticketDTO.getDescripcionInicial());
            ticket.setDescripcionTrabajo(ticketDTO.getDescripcionTrabajo());
            ticket.setAprobado(ticketDTO.isAprobado());

            var ticketGuardado = ticketRepository.save(ticket);
            return ticketConverter.entityToDto(ticketGuardado);
        } catch (Exception e) {
            logger.error("Error creando el ticket: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando el ticket", e);
        }
    }

    public List<TicketDTO> obtenerHistorialTickets(String username) {
        try {
            var tickets = ticketRepository.findByUsername(username);
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo el historial de tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Error obteniendo el historial de tickets", e);
        }
    }

    public TicketDTO modificarTicket(String ticketId, TicketDTO ticketDTO, String username) {
        try {
            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado con ID: " + ticketId));

            if (ticket.isAprobado()) {
                throw new RuntimeException("El ticket ya está aprobado y no puede ser modificado");
            }

            if (!ticket.getUsername().equals(username)) {
                throw new SecurityException("No tienes permiso para modificar este ticket");
            }

            if (!TicketEstado.isValidEstado(ticketDTO.getEstado())) {
                throw new IllegalArgumentException("Estado del ticket no válido: " + ticketDTO.getEstado());
            }

            ticket.setDescripcion(ticketDTO.getDescripcion());
            ticket.setEstado(ticketDTO.getEstado().toUpperCase());

            var ticketModificado = ticketRepository.save(ticket);
            return ticketConverter.entityToDto(ticketModificado);

        } catch (SecurityException e) {
            logger.error("Error de seguridad al modificar el ticket con ID {}: {}", ticketId, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error modificando el ticket con ID {}: {}", ticketId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado modificando el ticket con ID {}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al modificar el ticket", e);
        }
    }

    public void eliminarTicket(String ticketId, String username) {
        try {
            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado con ID: " + ticketId));

            if (ticket.isAprobado() || !ticket.getEstado().equals(TicketEstado.PENDIENTE.name()) || !ticket.getUsername().equals(username)) {
                throw new RuntimeException("El ticket no puede ser eliminado porque ya está aprobado o no pertenece al usuario");
            }

            ticketRepository.delete(ticket);
        } catch (RuntimeException e) {
            logger.error("Error eliminando el ticket con ID {}: {}", ticketId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado eliminando el ticket con ID {}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al eliminar el ticket", e);
        }
    }

    public TicketDTO aprobarTicket(String ticketId) {
        try {
            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado con ID: " + ticketId));

            if (ticket.isAprobado()) {
                throw new RuntimeException("El ticket ya está aprobado");
            }

            if (!ticket.getEstado().equals(TicketEstado.PENDIENTE.name())) {
                throw new RuntimeException("El ticket no está en estado 'Pendiente'");
            }

            ticket.setAprobado(true);
            ticket.setEstado(TicketEstado.APROBADO.name());

            var ticketAprobado = ticketRepository.save(ticket);
            return ticketConverter.entityToDto(ticketAprobado);
        } catch (RuntimeException e) {
            logger.error("Error aprobando el ticket con ID {}: {}", ticketId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado aprobando el ticket con ID {}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al aprobar el ticket", e);
        }
    }

    public TicketDTO rechazarTicket(String ticketId) {
        try {
            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado con ID: " + ticketId));

            if (ticket.isAprobado()) {
                throw new RuntimeException("El ticket ya está aprobado");
            }

            if (!ticket.getEstado().equals(TicketEstado.PENDIENTE.name())) {
                throw new RuntimeException("El ticket no está en estado 'Pendiente'");
            }

            ticket.setEstado(TicketEstado.RECHAZADO.name());

            var ticketRechazado = ticketRepository.save(ticket);
            return ticketConverter.entityToDto(ticketRechazado);
        } catch (RuntimeException e) {
            logger.error("Error rechazando el ticket con ID {}: {}", ticketId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado rechazando el ticket con ID {}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al rechazar el ticket", e);
        }
    }
}
