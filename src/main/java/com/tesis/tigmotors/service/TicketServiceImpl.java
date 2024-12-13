package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.converters.TicketConverter;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Crea un ticket automáticamente asociado a una solicitud.
     *
     * @param ticketDTO Datos del ticket a crear.
     * @param username  Usuario autenticado que genera el ticket.
     * @return TicketDTO con los datos del ticket creado.
     */
    @Override
    public TicketDTO crearTicketAutomatico(TicketDTO ticketDTO, String username) {
        Ticket ticket = ticketConverter.dtoToEntity(ticketDTO);
        ticket.setUsername(username);
        ticketConverter.asignarFechaYHoraActual(ticket);
        Ticket ticketGuardado = ticketRepository.save(ticket);

        return ticketConverter.entityToDto(ticketGuardado);
    }

    /**
     * Lista los tickets por estado.
     *
     * @param estado Estado del ticket.
     * @return Lista de tickets con el estado especificado.
     */
    @Override
    @Transactional
    public List<TicketDTO> listarTicketsPorEstado(TicketEstado estado) {
        logger.info("Iniciando la consulta de tickets con estado '{}'", estado);

        try {
            // Buscar tickets por estado
            List<Ticket> tickets = ticketRepository.findByEstado(estado);

            if (tickets.isEmpty()) {
                logger.warn("No se encontraron tickets con estado '{}'", estado);
                throw new ResourceNotFoundException("No se encontraron tickets con estado '" + estado + "'");
            }

            // Convertir las entidades a DTOs
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            logger.error("Error de negocio: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al listar tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al listar tickets.", e);
        }
    }

    /**
     * Lista todos los tickets sin aplicar ningún filtro.
     *
     * @return una lista de todos los tickets en la base de datos.
     */
    @Override
    @Transactional
    public List<TicketDTO> listarTodosLosTickets() {
        logger.info("Iniciando la consulta de todos los tickets");

        try {
            // Usa findAll para obtener todos los tickets
            List<Ticket> tickets = ticketRepository.findAll();

            if (tickets.isEmpty()) {
                logger.warn("No se encontraron tickets en la base de datos");
                throw new ResourceNotFoundException("No se encontraron tickets en la base de datos");
            }

            // Convertir las entidades a DTOs
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            logger.error("Error de negocio: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al listar todos los tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al listar todos los tickets.", e);
        }
    }


}
