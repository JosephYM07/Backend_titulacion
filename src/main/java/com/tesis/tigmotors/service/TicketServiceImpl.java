package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.Exceptions.TicketNotFoundException;
import com.tesis.tigmotors.converters.FacturaConverter;
import com.tesis.tigmotors.converters.TicketConverter;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Solicitud;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.FacturaRepository;
import com.tesis.tigmotors.repository.SolicitudRepository;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.FacturaService;
import com.tesis.tigmotors.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tesis.tigmotors.enums.TicketEstado;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final SolicitudRepository solicitudRepository;

    private final FacturaService facturaService;
    private final TicketConverter ticketConverter;
    private final FacturaConverter facturaConverter;


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

    /**
     * Obtiene el historial de tickets asociados a un usuario específico.
     *
     * @param username Nombre del usuario cuyos tickets se desean obtener.
     * @return Una lista de tickets en formato DTO asociados al usuario.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> obtenerHistorialTicketsPorUsuario(String username) {
        logger.info("Iniciando la consulta del historial de tickets para el usuario '{}'", username);

        try {
            // Buscar tickets asociados al usuario
            List<Ticket> tickets = ticketRepository.findByUsername(username);
            if (tickets.isEmpty()) {
                logger.warn("No se encontraron tickets para el usuario '{}'", username);
            } else {
                logger.info("Se encontraron {} tickets para el usuario '{}'", tickets.size(), username);
            }
            // Convertir los tickets a DTO y retornar
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo el historial de tickets para el usuario '{}': {}", username, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo el historial de tickets del usuario", e);
        }
    }

    /**
     * Obtiene una lista de tickets asociados a un usuario específico y filtrados por estado.
     *
     * @param username El nombre del usuario al que están asociados los tickets.
     * @param estado El estado del ticket (TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, VALOR_PAGADO, etc.).
     * @return Una lista de TicketDTO que representan los tickets del usuario con el estado especificado.
     * @throws IllegalArgumentException Si el estado proporcionado no es válido.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> obtenerTicketsPorUsuarioYEstado(String username, String estado) {
        logger.info("Iniciando la consulta de tickets para el usuario '{}' con estado '{}'", username, estado);

        // Validar si el estado pertenece a trabajos, pagos o prioridades
        if (!TicketEstado.isTrabajoEstado(estado) && !TicketEstado.isPagoEstado(estado) && !TicketEstado.isPrioridadEstado(estado)) {
            if (estado.startsWith("TRABAJO_")) {
                logger.warn("Estado inválido ingresado: '{}'. Estados válidos de trabajo: {}", estado, TicketEstado.getEstadosValidosTrabajo());
                throw new IllegalArgumentException("Estado inválido. Estados válidos de trabajo: " + TicketEstado.getEstadosValidosTrabajo());
            } else if (TicketEstado.isPagoEstado(estado)) {
                logger.warn("Estado inválido ingresado: '{}'. Estados válidos de pago: {}", estado, TicketEstado.getEstadosValidosPago());
                throw new IllegalArgumentException("Estado inválido. Estados válidos de pago: " + TicketEstado.getEstadosValidosPago());
            } else {
                logger.warn("Estado inválido ingresado: '{}'. Estados válidos de prioridad: {}", estado, TicketEstado.getEstadosValidosPrioridad());
                throw new IllegalArgumentException("Estado inválido. Estados válidos de prioridad: " + TicketEstado.getEstadosValidosPrioridad());
            }
        }

        // Buscar tickets por usuario y estado
        List<Ticket> tickets = ticketRepository.findByUsernameAndEstado(username, estado);

        if (tickets.isEmpty()) {
            logger.warn("No se encontraron tickets para el usuario '{}' con estado '{}'", username, estado);
        } else {
            logger.info("Se encontraron {} tickets para el usuario '{}' con estado '{}'", tickets.size(), username, estado);
        }

        // Convertir los tickets a DTO y retornar
        return tickets.stream()
                .map(ticketConverter::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una lista de tickets asociados a un usuario específico y filtrados por prioridad.
     *
     * @param prioridad La prioridad del ticket (ALTA, MEDIA, BAJA).
     * @param username El nombre del usuario al que están asociados los tickets.
     * @return Una lista de TicketDTO que representan los tickets del usuario con la prioridad especificada.
     * @throws IllegalArgumentException Si la prioridad proporcionada no es válida.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> obtenerTicketsPorPrioridadYUsuario(String prioridad, String username) {
        logger.info("Iniciando la consulta de tickets para el usuario '{}' con prioridad '{}'", username, prioridad);

        // Validar si la prioridad ingresada es válida
        if (!TicketEstado.isPrioridadEstado(prioridad)) {
            logger.warn("Prioridad inválida ingresada: '{}'. Estados válidos de prioridad: {}", prioridad, TicketEstado.getEstadosValidosPrioridad());
            throw new IllegalArgumentException("Prioridad inválida. Estados válidos de prioridad: " + TicketEstado.getEstadosValidosPrioridad());
        }

        // Buscar tickets por usuario y prioridad
        List<Ticket> tickets = ticketRepository.findByUsernameAndPrioridad(username, prioridad.toUpperCase());

        if (tickets.isEmpty()) {
            logger.warn("No se encontraron tickets para el usuario '{}' con prioridad '{}'", username, prioridad);
        } else {
            logger.info("Se encontraron {} tickets para el usuario '{}' con prioridad '{}'", tickets.size(), username, prioridad);
        }

        // Convertir los tickets a DTO y retornar
        return tickets.stream()
                .map(ticketConverter::entityToDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public TicketDTO actualizarEstadoTicket(String ticketId, TicketEstado nuevoEstado) {
        try {
            // Buscar el ticket por ID
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con ID: " + ticketId));

            // Validar que el nuevo estado sea válido
            if (nuevoEstado == null ||
                    (!nuevoEstado.equals(TicketEstado.TRABAJO_EN_PROGRESO) &&
                            !nuevoEstado.equals(TicketEstado.TRABAJO_TERMINADO))) {
                throw new IllegalStateException("El nuevo estado no es válido. Debe ser TRABAJO_EN_PROGRESO o TRABAJO_TERMINADO.");
            }

            // Validaciones de transición de estados
            if (nuevoEstado.equals(TicketEstado.TRABAJO_EN_PROGRESO) &&
                    !ticket.getEstado().equals(TicketEstado.TRABAJO_PENDIENTE.name())) {
                throw new IllegalStateException("El ticket debe estar en estado TRABAJO_PENDIENTE para avanzar a TRABAJO_EN_PROGRESO.");
            }

            if (nuevoEstado.equals(TicketEstado.TRABAJO_TERMINADO) &&
                    !ticket.getEstado().equals(TicketEstado.TRABAJO_EN_PROGRESO.name())) {
                throw new IllegalStateException("El ticket debe estar en estado TRABAJO_EN_PROGRESO para avanzar a TRABAJO_TERMINADO.");
            }

            // Actualizar el estado del ticket
            ticket.setEstado(nuevoEstado.name());
            Ticket ticketActualizado = ticketRepository.save(ticket);

            // Si el estado es TRABAJO_TERMINADO, generar y guardar factura usando el servicio
            if (nuevoEstado.equals(TicketEstado.TRABAJO_TERMINADO)) {
                double cotizacion = obtenerCotizacion(ticket.getSolicitudId());
                Factura factura = facturaService.generarFacturaDesdeTicket(ticketId, cotizacion);
                logger.info("Factura generada con ID {} para el ticket {}", factura.getFacturaId(), ticketId);
            }

            // Retornar DTO del ticket actualizado
            return ticketConverter.entityToDto(ticketActualizado);
        } catch (TicketNotFoundException e) {
            logger.error("Error: Ticket no encontrado. ID: {}", ticketId, e);
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Error: Transición de estado inválida. ID: {}", ticketId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el estado del ticket con ID {}: ", ticketId, e);
            throw new RuntimeException("Error interno al actualizar el estado del ticket");
        }
    }

    /**
     * Obtiene la cotización de una solicitud por su ID.
     * @param solicitudId ID de la solicitud asociada al ticket.
     * @return Monto de la cotización.
     * @throws ResourceNotFoundException Si no se encuentra la solicitud o no tiene cotización.
     */
    private double obtenerCotizacion(String solicitudId) {
        // Lógica para buscar la cotización real en la base de datos (simulada)
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        // Validar que la cotización exista
        if (solicitud.getCotizacion() == null || solicitud.getCotizacion() <= 0) {
            throw new IllegalStateException("La solicitud con ID " + solicitudId + " no tiene una cotización válida.");
        }

        return solicitud.getCotizacion();
    }


}
