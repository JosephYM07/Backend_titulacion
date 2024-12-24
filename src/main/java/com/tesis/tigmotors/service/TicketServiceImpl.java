package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.Exceptions.TicketNotFoundException;
import com.tesis.tigmotors.converters.FacturaConverter;
import com.tesis.tigmotors.converters.TicketConverter;
import com.tesis.tigmotors.dto.Request.TicketRequestDTO;
import com.tesis.tigmotors.dto.Response.TicketDTO;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Solicitud;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.FacturaRepository;
import com.tesis.tigmotors.repository.SolicitudRepository;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.EmailService;
import com.tesis.tigmotors.service.interfaces.FacturaService;
import com.tesis.tigmotors.service.interfaces.TicketService;
import com.tesis.tigmotors.utils.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final SolicitudRepository solicitudRepository;
    private final UserRepository userRepository;

    private final RoleValidator roleValidator;
    private final Map<TicketEstado, Set<TicketEstado>> transicionesValidas = inicializarTransicionesValidas();


    private final EmailService emailService;

    private final FacturaService facturaService;
    private final TicketConverter ticketConverter;
    private final FacturaConverter facturaConverter;


    /**
     * Obtiene las estadísticas de tickets por estado.
     *
     * @return ResponseEntity con el conteo de tickets por estado.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> getTicketsStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // Validar si el usuario tiene el rol adecuado
            if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
                logger.error("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
                throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
            }
            // Contar los tickets por estado
            long trabajoPendienteCount = ticketRepository.countByEstado("TRABAJO_PENDIENTE");
            long trabajoEnProgresoCount = ticketRepository.countByEstado("TRABAJO_EN_PROGRESO");
            long trabajoTerminadoCount = ticketRepository.countByEstado("TRABAJO_TERMINADO");

            // Crear la respuesta
            Map<String, Long> response = Map.of(
                    "TRABAJO_PENDIENTE", trabajoPendienteCount,
                    "TRABAJO_EN_PROGRESO", trabajoEnProgresoCount,
                    "TRABAJO_TERMINADO", trabajoTerminadoCount
            );

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error inesperado al obtener el estado de los tickets: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al obtener el estado de los tickets.", ex); // Manejado globalmente
        }
    }

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
    @Transactional(readOnly = true)
    public List<TicketDTO> listarTicketsPorEstado(String estado) {
        logger.info("Iniciando la consulta de tickets con estado '{}'", estado);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Validar si el usuario tiene el rol adecuado
        if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
            logger.error("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
            throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
        }

        try {
            // Validar y convertir el estado usando TicketEstado
            TicketEstado estadoEnum;

            if (estado.startsWith("TRABAJO_")) {
                estadoEnum = TicketEstado.fromTrabajoString(estado);
            } else {
                estadoEnum = TicketEstado.fromPrioridadString(estado);
            }

            // Buscar tickets por estado
            List<Ticket> tickets = ticketRepository.findByEstado(estadoEnum);

            if (tickets.isEmpty()) {
                logger.warn("No se encontraron tickets con estado '{}'", estadoEnum);
                throw new ResourceNotFoundException("No se encontraron tickets con estado '" + estadoEnum + "'");
            }

            // Convertir las entidades a DTOs
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            logger.error("El estado proporcionado '{}' no es válido: {}", estado, e.getMessage());
            throw new IllegalArgumentException("El estado '" + estado + "' no es válido. " + e.getMessage());
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
            logger.error("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
            throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
        }

        try {
            // Usa findAll para obtener todos los tickets
            List<Ticket> tickets = ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));

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
            List<Ticket> tickets = ticketRepository.findByUsername(username, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
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

        try {
            TicketEstado estadoEnum;

            // Validar y convertir el estado usando los métodos de TicketEstado
            if (estado.startsWith("TRABAJO_")) {
                estadoEnum = TicketEstado.fromTrabajoString(estado);
            } else {
                estadoEnum = TicketEstado.fromPrioridadString(estado);
            }

            // Buscar tickets por usuario y estado
            List<Ticket> tickets = ticketRepository.findByUsernameAndEstado(username, estadoEnum.name(), Sort.by(Sort.Direction.DESC, "fechaCreacion"));

            if (tickets.isEmpty()) {
                logger.warn("No se encontraron tickets para el usuario '{}' con estado '{}'", username, estadoEnum);
            } else {
                logger.info("Se encontraron {} tickets para el usuario '{}' con estado '{}'", tickets.size(), username, estadoEnum);
            }

            // Convertir los tickets a DTO y retornar
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException ex) {
            logger.error("Error al validar el estado '{}': {}", estado, ex.getMessage());
            throw ex; // El GlobalExceptionHandler maneja la excepción y devuelve una respuesta HTTP 400
        } catch (Exception ex) {
            logger.error("Error inesperado al consultar tickets para el usuario '{}'", username, ex);
            throw new RuntimeException("Error inesperado al procesar la solicitud.");
        }
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

        try {
            // Validar y convertir la prioridad usando el enum
            TicketEstado prioridadEnum = TicketEstado.fromPrioridadString(prioridad);

            // Buscar tickets por usuario y prioridad
            List<Ticket> tickets = ticketRepository.findByUsernameAndPrioridad(username, prioridadEnum.name(), Sort.by(Sort.Direction.DESC, "fechaCreacion"));

            if (tickets.isEmpty()) {
                logger.warn("No se encontraron tickets para el usuario '{}' con prioridad '{}'", username, prioridadEnum);
            } else {
                logger.info("Se encontraron {} tickets para el usuario '{}' con prioridad '{}'", tickets.size(), username, prioridadEnum);
            }

            // Convertir los tickets a DTO y retornar
            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException ex) {
            logger.error("Error al obtener tickets: {}", ex.getMessage());
            throw ex; // Relanzamos la excepción para que sea manejada por el GlobalExceptionHandler
        } catch (Exception ex) {
            logger.error("Error inesperado al obtener tickets", ex);
            throw new RuntimeException("Error inesperado al procesar la solicitud.");
        }
    }


    @Override
    @Transactional
    public TicketDTO actualizarEstadoTicket(String ticketId, String nuevoEstadoStr) {
        try {
            // Buscar el ticket por ID
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con ID: " + ticketId));

            // Obtener el estado actual del ticket
            TicketEstado estadoActual = TicketEstado.valueOf(ticket.getEstado());

            // Convertir el nuevo estado utilizando TicketEstado
            TicketEstado nuevoEstado;
            if (nuevoEstadoStr.startsWith("TRABAJO_")) {
                nuevoEstado = TicketEstado.fromTrabajoString(nuevoEstadoStr);
            } else {
                nuevoEstado = TicketEstado.fromPrioridadString(nuevoEstadoStr);
            }

            // Validar si la transición de estado es válida
            if (!esTransicionValida(estadoActual, nuevoEstado)) {
                throw new IllegalStateException(String.format(
                        "Transición no válida: No se puede pasar de %s a %s.",
                        estadoActual, nuevoEstado));
            }

            // Actualizar el estado del ticket
            ticket.setEstado(nuevoEstado.name());
            Ticket ticketActualizado = ticketRepository.save(ticket);

            // Si el estado es TRABAJO_TERMINADO, generar factura y enviar correo
            if (nuevoEstado.equals(TicketEstado.TRABAJO_TERMINADO)) {
                double cotizacion = obtenerCotizacion(ticket.getSolicitudId());
                Factura factura = facturaService.generarFacturaDesdeTicket(ticketId, cotizacion);
                logger.info("Factura generada con ID {} para el ticket {}", factura.getFacturaId(), ticketId);

                String username = ticket.getUsername();
                String email = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username))
                        .getEmail();

                String asunto = "Tu trabajo ha sido completado";
                String contenido = construirCorreoTrabajoFinalizado(username, ticket, cotizacion);
                emailService.sendEmail(email, asunto, contenido);
            }

            // Retornar DTO del ticket actualizado
            return ticketConverter.entityToDto(ticketActualizado);

        } catch (TicketNotFoundException e) {
            logger.error("Error: Ticket no encontrado. ID: {}", ticketId, e);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Error: El estado '{}' proporcionado no es válido.", nuevoEstadoStr);
            throw new IllegalArgumentException("El estado '" + nuevoEstadoStr + "' no es válido.");
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
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        // Validar que la cotización exista
        if (solicitud.getCotizacion() == null || solicitud.getCotizacion() <= 0) {
            throw new IllegalStateException("La solicitud con ID " + solicitudId + " no tiene una cotización válida.");
        }

        return solicitud.getCotizacion();
    }

    @Override
    public List<TicketDTO> listarTicketsConFiltros(TicketRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Validar si el usuario tiene el rol adecuado
        if (!roleValidator.tieneAlgunRol(authentication, Role.ADMIN, Role.PERSONAL_CENTRO_DE_SERVICIOS)) {
            logger.error("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
            throw new SecurityException("Acceso denegado. Se requiere el rol ADMIN o PERSONAL_CENTRO_DE_SERVICIOS.");
        }
        logger.info("Iniciando proceso para listar tickets con filtros: {}", requestDTO);

        try {
            validarFechas(requestDTO);

            String prioridad = (requestDTO.getPrioridad() != null) ? validarPrioridad(requestDTO.getPrioridad()) : null;
            String estado = (requestDTO.getEstado() != null) ? validarEstado(requestDTO.getEstado()) : null;

            List<Ticket> tickets = obtenerTicketsPorFiltros(requestDTO, estado, prioridad);

            return tickets.stream()
                    .map(ticketConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.warn("Error en validaciones: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al listar tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al listar tickets.");
        }
    }

// Métodos auxiliares

    private void validarFechas(TicketRequestDTO requestDTO) {
        String fechaRegex = "\\d{4}/\\d{2}/\\d{2}";

        // Validar formato de fechaInicio
        if (requestDTO.getFechaInicio() == null || !requestDTO.getFechaInicio().matches(fechaRegex)) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria y debe estar en el formato 'yyyy/MM/dd'.");
        }

        // Validar formato de fechaFin
        if (requestDTO.getFechaFin() == null || !requestDTO.getFechaFin().matches(fechaRegex)) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria y debe estar en el formato 'yyyy/MM/dd'.");
        }
        // Validar que fechaInicio no sea posterior a fechaFin
        if (requestDTO.getFechaInicio().compareTo(requestDTO.getFechaFin()) > 0) {
            log.warn("La fecha de inicio no puede ser posterior a la fecha de fin.");
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    private String validarPrioridad(String prioridad) {
        return TicketEstado.fromPrioridadString(prioridad).name();
    }

    private String validarEstado(String estado) {
        return TicketEstado.fromTrabajoString(estado).name();
    }

    private List<Ticket> obtenerTicketsPorFiltros(TicketRequestDTO requestDTO, String estado, String prioridad) {
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaCreacion");
        if (requestDTO.getUsername() != null && estado != null && prioridad != null) {
            return ticketRepository.findByFechaCreacionAndUsernameAndEstadoAndPrioridad(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getUsername(), estado, prioridad, sort);
        } else if (requestDTO.getUsername() != null && estado != null) {
            return ticketRepository.findByFechaCreacionAndUsernameAndEstado(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getUsername(), estado, sort);
        } else if (estado != null && prioridad != null) {
            return ticketRepository.findByFechaCreacionAndEstadoAndPrioridad(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), estado, prioridad, sort);
        } else if (requestDTO.getUsername() != null) {
            return ticketRepository.findByFechaCreacionAndUsername(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getUsername(), sort);
        } else if (estado != null) {
            return ticketRepository.findByFechaCreacionAndEstado(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), estado, sort);
        } else if (prioridad != null) {
            return ticketRepository.findByFechaCreacionAndPrioridad(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), prioridad, sort);
        } else {
            return ticketRepository.findByFechaCreacionBetween(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), sort);
        }
    }


    private String construirCorreoTrabajoFinalizado(String username, Ticket ticket, double cotizacion) {
        return "<html>" +
                "<meta charset='UTF-8'>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<h2 style='color: #333;'>¡Tu trabajo ha sido completado!</h2>" +
                "<p>Hola " + username + ",</p>" +
                "<p>Nos complace informarte que el trabajo asociado a tu solicitud <strong>" + ticket.getSolicitudId() + "</strong> ha sido finalizado.</p>" +
                "<p>El costo total de la reparación es: <strong>$" + String.format("%.2f", cotizacion) + "</strong>.</p>" +
                "<p>Por favor, acércate a nuestro taller para completar el proceso de pago.</p>" +
                "<p>Si tienes alguna pregunta, no dudes en contactarnos.</p>" +
                "<br>" +
                "<p>Gracias por confiar en TigMotors.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Inicializa las transiciones válidas entre estados de tickets.
     *
     * @return Mapa con las transiciones válidas.
     */
    private Map<TicketEstado, Set<TicketEstado>> inicializarTransicionesValidas() {
        Map<TicketEstado, Set<TicketEstado>> transiciones = new EnumMap<>(TicketEstado.class);
        transiciones.put(TicketEstado.TRABAJO_PENDIENTE, EnumSet.of(TicketEstado.TRABAJO_EN_PROGRESO));
        transiciones.put(TicketEstado.TRABAJO_EN_PROGRESO, EnumSet.of(TicketEstado.TRABAJO_TERMINADO));
        transiciones.put(TicketEstado.TRABAJO_TERMINADO, EnumSet.noneOf(TicketEstado.class));
        return transiciones;
    }

    /**
     * Valida si una transición entre dos estados es válida.
     *
     * @param estadoActual El estado actual del ticket.
     * @param nuevoEstado  El estado al que se quiere transicionar.
     * @return true si la transición es válida, false en caso contrario.
     */
    private boolean esTransicionValida(TicketEstado estadoActual, TicketEstado nuevoEstado) {
        return transicionesValidas.getOrDefault(estadoActual, EnumSet.noneOf(TicketEstado.class)).contains(nuevoEstado);
    }

}

