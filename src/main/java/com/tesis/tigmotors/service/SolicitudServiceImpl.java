package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.InvalidSolicitudStateException;
import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.Exceptions.SolicitudNotFoundException;
import com.tesis.tigmotors.converters.SolicitudConverter;
import com.tesis.tigmotors.dto.Request.SolicitudAdminRequestDTO;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Response.TicketDTO;
import com.tesis.tigmotors.dto.Response.EliminarSolicitudResponse;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.models.Solicitud;
import com.tesis.tigmotors.enums.SolicitudEstado;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate; // Para realizar consultas dinámicas
import org.springframework.data.mongodb.core.query.Criteria; // Para construir los filtros
import org.springframework.data.mongodb.core.query.Query; // Para representar la consulta
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.tesis.tigmotors.repository.SolicitudRepository;
import com.tesis.tigmotors.repository.UserRepository;
import com.tesis.tigmotors.service.interfaces.SolicitudService;
import com.tesis.tigmotors.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudServiceImpl.class);

    private final SolicitudRepository solicitudRepository;
    private final SolicitudConverter solicitudConverter;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final TicketService ticketService;
    private final UserRepository userRepository;

    /**
     * Obtiene las estadísticas de solicitudes por estado.
     *
     * @return ResponseEntity con el conteo de solicitudes por estado.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> getSolicitudesStatus() {
        try {
            // Contar las solicitudes por estado
            long pendientesCount = solicitudRepository.countByEstado("PENDIENTE");
            long aceptadasCount = solicitudRepository.countByEstado("ACEPTADO");

            // Crear la respuesta
            Map<String, Long> response = Map.of(
                    "PENDIENTE", pendientesCount,
                    "ACEPTADO", aceptadasCount
            );

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error inesperado al obtener el estado de las solicitudes: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error inesperado al obtener el estado de las solicitudes.", ex); // Manejado globalmente
        }
    }

    /**
     * Crea una nueva solicitud en el sistema desde la cuenta usuario.
     * @param solicitudDTO Datos de la solicitud a crear, proporcionados por el usuario.
     * @param username Nombre del usuario que está creando la solicitud.
     * @return Un DTO de respuesta con la información de la solicitud creada.
     */
    @Override
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudDTO solicitudDTO, String username) {
        try {
            // Convertir el DTO a entidad
            Solicitud solicitud = solicitudConverter.dtoToEntity(solicitudDTO);

            // Validar y normalizar la prioridad reutilizando el método
            String normalizedPrioridad = normalizarYValidarPrioridad(solicitud.getPrioridad());
            solicitud.setPrioridad(normalizedPrioridad);

            // Generar ID único para la solicitud
            solicitud.setIdSolicitud("SOLICITUD-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.SOLICITUD_SEQUENCE));

            // Asignar datos adicionales al modelo
            solicitud.setUsername(username);
            solicitud.setEstado(SolicitudEstado.PENDIENTE.name());

            // Asignar fecha y hora actuales utilizando el convertidor
            solicitudConverter.asignarFechaYHoraActual(solicitud);

            // Guardar la solicitud en la base de datos
            Solicitud solicitudGuardada = solicitudRepository.save(solicitud);

            // Convertir la entidad guardada a DTO de respuesta
            return solicitudConverter.entityToResponseDto(solicitudGuardada);

        } catch (IllegalArgumentException e) {
            // Lanzar excepciones de validación para que sean manejadas globalmente
            logger.warn("Error de validación al crear la solicitud: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Manejo de errores inesperados
            logger.error("Error creando la solicitud: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al crear la solicitud.", e);
        }
    }


    @Override
    @Transactional
    public SolicitudResponseDTO registrarSolicitudPorAdmin(SolicitudAdminRequestDTO solicitudAdminRequestDTO) {
        try {
            // Validar y normalizar el username
            if (solicitudAdminRequestDTO.getUsername() == null || solicitudAdminRequestDTO.getUsername().trim().isEmpty()) {
                logger.error("El username proporcionado está vacío.");
                throw new IllegalArgumentException("El username proporcionado no puede estar vacío.");
            }
            String normalizedUsername = solicitudAdminRequestDTO.getUsername().trim();

            // Validar si el usuario especificado existe
            User user = userRepository.findByUsername(normalizedUsername)
                    .orElseThrow(() -> {
                        logger.error("Usuario no encontrado: '{}'", normalizedUsername);
                        return new IllegalArgumentException("Usuario no encontrado: El usuario especificado no existe.");
                    });

            logger.info("Usuario encontrado: '{}'", user.getUsername());

            // Convertir el DTO a entidad
            Solicitud solicitud = solicitudConverter.adminRequestToEntity(solicitudAdminRequestDTO);

            // Normalizar y validar la prioridad
            logger.info("Normalizando y validando la prioridad: '{}'", solicitud.getPrioridad());
            String normalizedPrioridad = normalizarYValidarPrioridad(solicitud.getPrioridad());
            solicitud.setPrioridad(normalizedPrioridad);
            logger.info("Prioridad validada: '{}'", normalizedPrioridad);

            // Generar ID único para la solicitud
            solicitud.setIdSolicitud("SOLICITUD-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.SOLICITUD_SEQUENCE));
            solicitud.setUsername(user.getUsername());
            solicitud.setEstado(SolicitudEstado.ACEPTADO.name());
            solicitud.setCotizacionAceptada(SolicitudEstado.COTIZACION_ACEPTADA.name());
            // Asignar fecha y hora actuales utilizando el convertidor
            solicitudConverter.asignarFechaYHoraActual(solicitud);

            // Guardar la solicitud en la base de datos
            Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
            logger.info("Solicitud creada exitosamente con ID '{}'", solicitudGuardada.getIdSolicitud());

            //Crear un ticket asociado a la solicitud
            generarYCrearTicket(solicitudGuardada, user);

            // Convertir la solicitud guardada a DTO de respuesta
            return solicitudConverter.entityToResponseDto(solicitudGuardada);

        } catch (IllegalArgumentException e) {
            // Lanzar excepciones de validación para que sean manejadas globalmente
            logger.warn("Error de validación al registrar la solicitud: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Manejo de errores inesperados
            logger.error("Error registrando la solicitud: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al registrar la solicitud.", e);
        }
    }


    /**
     * Acepta una solicitud en el sistema cambiando su estado a 'ACEPTADO' desde el perfil Administrador.
     * @param solicitudId ID de la solicitud a aceptar.
     * @return Un DTO de respuesta con la información de la solicitud aceptada.
     */
    @Override
    @Transactional
    public SolicitudResponseDTO aceptarSolicitud(String solicitudId) {
        logger.info("Iniciando el proceso para aceptar la solicitud con ID '{}'", solicitudId);

        // Validar el formato del ID
        if (!solicitudId.toUpperCase().startsWith("SOLICITUD-")) {
            logger.error("El ID ingresado '{}' no tiene el formato correcto. Debe comenzar con 'SOLICITUD-'.", solicitudId);
            throw new IllegalArgumentException("El ID proporcionado no es válido. Debe comenzar con 'SOLICITUD-' seguido de un número.");
        }
        // Convertir el ID a mayúsculas
        String normalizedSolicitudId = solicitudId.toUpperCase();
        try {
            // Buscar la solicitud con el ID normalizado
            Solicitud solicitud = solicitudRepository.findById(normalizedSolicitudId)
                    .orElseThrow(() -> {
                        logger.error("Solicitud no encontrada con ID '{}'", solicitudId);
                        return new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
                    });

            // Validaciones
            if (solicitud.getEstado().equals(SolicitudEstado.ACEPTADO.name())) {
                logger.warn("Intento de aceptar una solicitud ya aceptada. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud ya está en estado 'Aceptado'");
            }

            if (!solicitud.getEstado().equals(SolicitudEstado.PENDIENTE.name())) {
                logger.warn("Intento de aceptar una solicitud que no está en estado PENDIENTE. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'Pendiente'");
            }

            // Cambiar el estado a ACEPTADO
            solicitud.setEstado(SolicitudEstado.ACEPTADO.name());
            Solicitud solicitudAceptada = solicitudRepository.save(solicitud);

            logger.info("Solicitud con ID '{}' aceptada exitosamente.", solicitudId);

            // Convertir la entidad a DTO
            return solicitudConverter.entityToResponseDto(solicitudAceptada);

        } catch (SolicitudNotFoundException | IllegalStateException e) {
            logger.error("Error controlado: {}", e.getMessage());
            throw e; // Se maneja en el GlobalExceptionHandler
        } catch (Exception e) {
            logger.error("Error inesperado al aceptar la solicitud con ID '{}': {}", solicitudId, e.getMessage(), e);
            throw new RuntimeException("Error interno al aceptar la solicitud", e);
        }
    }


    /**
     * Añade una cotización y descripción del trabajo a una solicitud desde el perfil Administrador.
     * @param solicitudId ID de la solicitud a la que se añadirá la cotización.
     * @param requestBody Mapa de datos con los campos 'cotizacion' y 'descripcionTrabajo'.
     * @param username Nombre del usuario que realiza la operación.
     * @return Un DTO con la información actualizada de la solicitud.
     */
    @Override
    @Transactional
    public SolicitudDTO anadirCotizacion(String solicitudId, Map<String, Object> requestBody, String username) {
        // Validar el formato del ID
        if (!solicitudId.toUpperCase().startsWith("SOLICITUD-")) {
            logger.error("El ID ingresado '{}' no tiene el formato correcto. Debe comenzar con 'SOLICITUD-'.", solicitudId);
            throw new IllegalArgumentException("El ID proporcionado no es válido. Debe comenzar con 'SOLICITUD-' seguido de un número.");
        }
        // Convertir el ID a mayúsculas
        String normalizedSolicitudId = solicitudId.toUpperCase();
        try {
            logger.info("Usuario {} inició el proceso para añadir cotización. ID Solicitud: {}", username, solicitudId);
            // Validar y extraer parámetros del requestBody
            Double cotizacion = Optional.ofNullable(requestBody.get("cotizacion"))
                    .map(Object::toString)
                    .map(Double::valueOf)
                    .filter(valor -> valor > 0)
                    .orElseThrow(() -> new IllegalArgumentException("El valor de 'cotizacion' debe ser un número positivo, mayor que cero y no puede estar en blanco el campo"));
            String descripcionTrabajo = Optional.ofNullable(requestBody.get("descripcionTrabajo"))
                    .map(Object::toString)
                    .filter(descripcion -> !descripcion.isBlank())
                    .orElseThrow(() -> new IllegalArgumentException("La 'descripcionTrabajo' es obligatoria."));
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(normalizedSolicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
            // Validar estado y cotización existente
            if (!solicitud.getEstado().equals(SolicitudEstado.ACEPTADO.name())) {
                logger.error("La solicitud no está en estado 'Aceptado'. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'Aceptado'");
            }
            if (solicitud.getCotizacion() != null) {
                logger.error("La solicitud ya tiene una cotización asignada. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud ya tiene una cotización asignada");
            }
            // Actualizar los datos
            solicitud.setCotizacion(cotizacion);
            solicitud.setDescripcionTrabajo(descripcionTrabajo);
            // Guardar la solicitud actualizada
            Solicitud solicitudConCotizacion = solicitudRepository.save(solicitud);
            logger.info("Usuario {} añadió cotización correctamente a la solicitud con ID: {}", username, solicitudId);
            // Convertir a DTO y retornar
            return solicitudConverter.entityToDto(solicitudConCotizacion);
        } catch (Exception e) {
            logger.error("Error inesperado al añadir cotización. ID: {}, Usuario: {}", solicitudId, username, e);
            throw e;
        }
    }

    /**
     * Acepta la cotización de una solicitud y genera automáticamente un ticket desde el perfil Usuario.
     * @param solicitudId ID de la solicitud cuya cotización se acepta.
     * @param username Nombre del usuario que realiza la operación.
     * @return Un DTO del ticket generado automáticamente.
     */
    @Override
    @Transactional
    public SolicitudResponseDTO aceptarCotizacionGenerarTicket(String solicitudId, String username) {
        logger.info("Usuario '{}' intentando aceptar cotización para solicitud ID '{}'", username, solicitudId);
        // Validar el formato del ID
        if (!solicitudId.toUpperCase().startsWith("SOLICITUD-")) {
            logger.error("El ID ingresado '{}' no tiene el formato correcto. Debe comenzar con 'SOLICITUD-'.", solicitudId);
            throw new IllegalArgumentException("El ID proporcionado no es válido. Debe comenzar con 'SOLICITUD-' seguido de un número.");
        }
        // Convertir el ID a mayúsculas
        String normalizedSolicitudId = solicitudId.toUpperCase();
        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(normalizedSolicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

            // Validar que el usuario tiene permisos para aceptar la cotización
            if (!solicitud.getUsername().equals(username)) {
                logger.error("Acceso denegado: usuario '{}' no coincide con el propietario de la solicitud '{}'", username, solicitud.getUsername());
                throw new AccessDeniedException("No tiene permisos para aceptar esta cotización.");
            }

            // Verificar que la cotización no haya sido rechazada previamente
            if (SolicitudEstado.RECHAZO_COTIZACION_USUARIO.name().equals(solicitud.getCotizacionAceptada())) {
                logger.error("Intento de aceptar una cotización ya rechazada. Solicitud ID '{}'", solicitudId);
                throw new InvalidSolicitudStateException("No es posible aceptar una cotización que ya ha sido rechazada.");
            }

            // Verificar que la cotización no haya sido ya aceptada
            if (SolicitudEstado.COTIZACION_ACEPTADA.name().equals(solicitud.getCotizacionAceptada())) {
                logger.error("Intento de aceptar una cotización ya aceptada. Solicitud ID '{}'", solicitudId);
                throw new InvalidSolicitudStateException("La cotización ya ha sido aceptada.");
            }

            // Verificar que el estado de la solicitud sea 'ACEPTADO'
            if (!SolicitudEstado.ACEPTADO.name().equals(solicitud.getEstado())) {
                logger.error("Estado inválido: la solicitud con ID '{}' no está en estado 'ACEPTADO'", solicitudId);
                throw new InvalidSolicitudStateException("La solicitud debe estar en estado 'ACEPTADO' para aceptar la cotización.");
            }
            // Actualizar el estado de la cotización
            solicitud.setCotizacionAceptada(SolicitudEstado.COTIZACION_ACEPTADA.name());
            solicitudRepository.save(solicitud);

            // Crear el ticket asociado reutilizando el método
            User user = new User();
            user.setUsername(username);
            generarYCrearTicket(solicitud, user);

            // Retornar la solicitud actualizada como DTO de respuesta
            return solicitudConverter.entityToResponseDto(solicitud);

        } catch (SolicitudNotFoundException | AccessDeniedException | InvalidSolicitudStateException e) {
            logger.error("Error procesando la solicitud ID '{}': {}", solicitudId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado aceptando cotización y generando ticket para solicitud ID '{}', usuario '{}': {}", solicitudId, username, e.getMessage(), e);
            throw new RuntimeException("Error aceptando la cotización y generando el ticket", e);
        }
    }


    /**
     * Permite que un usuario rechace la cotización de una solicitud.
     * @param solicitudId ID de la solicitud cuya cotización se va a rechazar.
     * @param username Nombre del usuario que realiza la operación.
     * @return Un DTO con la información actualizada de la solicitud.
     * @throws SolicitudNotFoundException Si la solicitud no se encuentra.
     * @throws AccessDeniedException Si el usuario no tiene permisos para rechazar la cotización.
     * @throws IllegalStateException Si la solicitud no está en estado 'ACEPTADO'.
     * @throws RuntimeException Para cualquier error inesperado durante el proceso.
     */
    @Override
    @Transactional
    public SolicitudDTO rechazarCotizacion(String solicitudId, String username) {
        logger.info("Usuario '{}' intentando rechazar la cotización para la solicitud ID '{}'", username, solicitudId);
        // Validar el formato del ID
        if (!solicitudId.toUpperCase().startsWith("SOLICITUD-")) {
            logger.error("El ID ingresado '{}' no tiene el formato correcto. Debe comenzar con 'SOLICITUD-'.", solicitudId);
            throw new IllegalArgumentException("El ID proporcionado no es válido. Debe comenzar con 'SOLICITUD-' seguido de un número.");
        }
        // Convertir el ID a mayúsculas
        String normalizedSolicitudId = solicitudId.toUpperCase();
        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(normalizedSolicitudId)
                    .orElseThrow(() -> {
                        logger.error("Solicitud no encontrada con ID '{}'", solicitudId);
                        return new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
                    });
            // Verificar si la cotización ya fue aceptada
            if (SolicitudEstado.COTIZACION_ACEPTADA.name().equals(solicitud.getCotizacionAceptada())) {
                logger.warn("Intento de rechazar una cotización ya aceptada. ID Solicitud: '{}'", solicitudId);
                throw new IllegalStateException("No es posible rechazar una cotización que ya ha sido aceptada.");
            }
            // Validar permisos del usuario
            if (!solicitud.getUsername().equals(username)) {
                logger.warn("Acceso denegado: el usuario '{}' no es el propietario de la solicitud ID '{}'", username, solicitudId);
                throw new AccessDeniedException("No tiene permisos para rechazar esta cotización.");
            }
            // Verificar si la cotización ya fue rechazada
            if (SolicitudEstado.RECHAZO_COTIZACION_USUARIO.name().equals(solicitud.getCotizacionAceptada())) {
                logger.warn("Intento de rechazar una cotización ya rechazada. ID Solicitud: '{}'", solicitudId);
                throw new IllegalStateException("La cotización ya ha sido rechazada anteriormente.");
            }
            // Validar estado de la solicitud
            if (!solicitud.getEstado().equals(SolicitudEstado.ACEPTADO.name())) {
                logger.warn("Estado inválido: la solicitud con ID '{}' no está en estado 'ACEPTADO'", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'ACEPTADO'");
            }
            // Actualizar el estado de la cotización y de la solicitud
            solicitud.setCotizacionAceptada(SolicitudEstado.RECHAZO_COTIZACION_USUARIO.name());
            Solicitud solicitudRechazada = solicitudRepository.save(solicitud);
            logger.info("Cotización rechazada exitosamente por el usuario '{}' para la solicitud ID '{}'", username, solicitudId);
            // Convertir a DTO y retornar
            return solicitudConverter.entityToDto(solicitudRechazada);
        } catch (SolicitudNotFoundException | AccessDeniedException | IllegalStateException e) {
            // Manejo específico de excepciones conocidas
            logger.error("Error procesando el rechazo de cotización. ID Solicitud: '{}', Usuario: '{}'. Detalle: {}", solicitudId, username, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // Manejo general de errores inesperados
            logger.error("Error inesperado al rechazar la cotización para la solicitud ID '{}', Usuario: '{}'. Detalle: {}", solicitudId, username, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al rechazar la cotización", e);
        }
    }

    /**
     * Obtiene el historial de solicitudes asociadas a un usuario específico perfil USER.
     * @param username Nombre del usuario cuyas solicitudes se desean obtener.
     * @return Una lista de solicitudes en formato DTO asociadas al usuario.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerHistorialSolicitudesPorUsuario(String username) {
        logger.info("Iniciando la consulta del historial de solicitudes para el usuario '{}'", username);

        try {
            // Buscar solicitudes asociadas al usuario
            List<Solicitud> solicitudes = solicitudRepository.findByUsername(username, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (solicitudes.isEmpty()) {
                logger.warn("No se encontraron solicitudes para el usuario '{}'", username);
            } else {
                logger.info("Se encontraron {} solicitudes para el usuario '{}'", solicitudes.size(), username);
            }
            // Convertir las solicitudes a DTO y retornar
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo el historial de solicitudes para el usuario '{}': {}", username, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo el historial de solicitudes del usuario", e);
        }
    }

    /**
     * Obtiene todas las solicitudes que coinciden con un estado específico.
     * @param estado Estado de las solicitudes que se desean obtener.
     * @return Una lista de solicitudes en formato DTO con el estado especificado.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerSolicitudesPorEstado(String estado) {
        logger.info("Iniciando la consulta de solicitudes con estado '{}'", estado);
        // Validar el formato del ID
        if (!estado.toUpperCase().startsWith("PENDIENTE") && !estado.toUpperCase().startsWith("ACEPTADO") && !estado.toUpperCase().startsWith("SOLICITUD_RECHAZADA")) {
            logger.error("El ID ingresado '{}' no tiene el formato correcto. Debe comenzar con 'SOLICITUD-'.", estado);
            throw new IllegalArgumentException("El ID proporcionado no es válido. Debe comenzar con 'Pendiente, ACEPTADO o SOLICITUD_RECHAZADA' seguido de un número.");
        }
        // Convertir el ID a mayúsculas
        String normalizedEstadoSolicitud = estado.toUpperCase();
        try {
            // Buscar solicitudes por estado
            List<Solicitud> solicitudes = solicitudRepository.findByEstado(normalizedEstadoSolicitud, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (solicitudes.isEmpty()) {
                logger.warn("No se encontraron solicitudes con el estado '{}'", estado);
            } else {
                logger.info("Se encontraron {} solicitudes con el estado '{}'", solicitudes.size(), estado);
            }
            // Convertir las solicitudes a DTO y retornar
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Manejo de errores inesperados
            logger.error("Error obteniendo las solicitudes con estado '{}': {}", estado, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo las solicitudes por estado", e);
        }
    }

    /**
     * Obtiene todas las solicitudes asociadas a un usuario específico y con un estado determinado.
     * @param username Nombre del usuario cuyas solicitudes se desean obtener.
     * @param estado Estado de las solicitudes que se desean filtrar.
     * @return Una lista de solicitudes en formato DTO asociadas al usuario y con el estado especificado.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerSolicitudesPorUsuarioYEstado(String username, String estado) {
        logger.info("Iniciando la consulta de solicitudes para el usuario '{}' con estado '{}'", username, estado);
// Convertir el estado a mayúsculas
        String normalizedEstado = estado.toUpperCase();

        // Validar que el estado sea uno de los valores permitidos
        if (!normalizedEstado.equals("PENDIENTE") &&
                !normalizedEstado.equals("ACEPTADO") &&
                !normalizedEstado.equals("SOLICITUD_RECHAZADA")) {
            logger.error("El estado ingresado '{}' no es válido. Debe ser 'PENDIENTE', 'ACEPTADO' o 'SOLICITUD_RECHAZADA'.", estado);
            throw new IllegalArgumentException("Estado inválido. Use 'PENDIENTE', 'ACEPTADO' o 'SOLICITUD_RECHAZADA'.");
        }
        try {
            // Buscar solicitudes por usuario y estado
            List<Solicitud> solicitudes = solicitudRepository.findByUsernameAndEstado(username, normalizedEstado, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

            if (solicitudes.isEmpty()) {
                logger.warn("No se encontraron solicitudes para el usuario '{}' con estado '{}'", username, estado);
            } else {
                logger.info("Se encontraron {} solicitudes para el usuario '{}' con estado '{}'", solicitudes.size(), username, estado);
            }

            // Convertir las solicitudes a DTO y retornar
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // Manejo de errores inesperados
            logger.error("Error obteniendo las solicitudes para el usuario '{}' con estado '{}': {}", username, estado, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo las solicitudes del usuario por estado", e);
        }
    }

    /**
     * Obtiene todas las solicitudes que tienen una prioridad específica.
     * @param prioridad Prioridad de las solicitudes que se desean obtener (ALTA, MEDIA, BAJA).
     * @return Una lista de solicitudes en formato DTO con la prioridad especificada.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerSolicitudesPorPrioridad(String prioridad) {
        logger.info("Iniciando la consulta de solicitudes con prioridad '{}'", prioridad);

        try {
            // Buscar solicitudes por prioridad
            List<Solicitud> solicitudes = solicitudRepository.findByPrioridad(prioridad, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (solicitudes.isEmpty()) {
                logger.warn("No se encontraron solicitudes con la prioridad '{}'", prioridad);
            } else {
                logger.info("Se encontraron {} solicitudes con la prioridad '{}'", solicitudes.size(), prioridad);
            }
            // Convertir las solicitudes a DTO y retornar
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Manejo de errores inesperados
            logger.error("Error obteniendo las solicitudes con prioridad '{}': {}", prioridad, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo las solicitudes por prioridad", e);
        }
    }

    /**
     * Obtiene todas las solicitudes asociadas a un usuario específico y con una prioridad determinada.
     * @param prioridad Prioridad de las solicitudes que se desean obtener (ALTA, MEDIA, BAJA).
     * @param username Nombre del usuario cuyas solicitudes se desean obtener.
     * @return Una lista de solicitudes en formato DTO asociadas al usuario y con la prioridad especificada.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerSolicitudesPorPrioridadYUsuario(String prioridad, String username) {
        logger.info("Iniciando la consulta de solicitudes para el usuario '{}' con prioridad '{}'", username, prioridad);

        try {
            try {
                SolicitudEstado.valueOf(prioridad.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Prioridad inválida ingresada: '{}'. Las prioridades válidas son: ALTO, MEDIO, BAJA", prioridad);
                throw new IllegalArgumentException("Prioridad inválida. Las prioridades válidas son: ALTO, MEDIO, BAJA");
            }
            // Buscar solicitudes por usuario y prioridad
            List<Solicitud> solicitudes = solicitudRepository.findByUsernameAndPrioridad(username, prioridad, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (solicitudes.isEmpty()) {
                logger.warn("No se encontraron solicitudes para el usuario '{}' con prioridad '{}'", username, prioridad);
            } else {
                logger.info("Se encontraron {} solicitudes para el usuario '{}' con prioridad '{}'", solicitudes.size(), username, prioridad);
            }
            // Convertir las solicitudes a DTO y retornar
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            // Manejo específico para prioridad inválida
            logger.error("Error de validación: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error obteniendo las solicitudes para el usuario '{}' con prioridad '{}': {}", username, prioridad, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo las solicitudes del usuario por prioridad", e);
        }
    }


    /**
     * Obtiene el historial completo de solicitudes en el sistema (accesible solo para administradores).
     * @return Una lista de todas las solicitudes en formato DTO.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> obtenerHistorialCompletoSolicitudes() {
        logger.info("Iniciando la consulta del historial completo de solicitudes");

        try {

            // Obtener todas las solicitudes
            List<Solicitud> solicitudes = solicitudRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (solicitudes.isEmpty()) {
                logger.warn("No se encontraron solicitudes en el sistema");
                // Lanza una excepción específica cuando no hay solicitudes
                throw new ResourceNotFoundException("No se encontraron solicitudes en el historial");
            }
            // Convertir las solicitudes a DTO y retornar
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException ex) {
            // Registra y lanza la excepción específica
            logger.error("Error: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception e) {
            // Manejo de errores inesperados
            logger.error("Error obteniendo el historial completo de solicitudes: {}", e.getMessage(), e);
            throw new RuntimeException("Error obteniendo el historial completo de solicitudes", e);
        }
    }

    /**
     * Modifica una solicitud existente asociada a un usuario autenticado.
     * @param solicitudId ID de la solicitud a modificar.
     * @param solicitudDTO DTO con los datos actualizados de la solicitud.
     * @param username Nombre del usuario que realiza la operación.
     * @return Un DTO con la información actualizada de la solicitud.
     * @throws SolicitudNotFoundException Si la solicitud no existe.
     * @throws AccessDeniedException Si el usuario no tiene permisos para modificar la solicitud o la solicitud no está en estado 'Pendiente'.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional
    public SolicitudDTO modificarSolicitud(String solicitudId, SolicitudDTO solicitudDTO, String username) {
        logger.info("Usuario '{}' intentando modificar la solicitud con ID '{}'", username, solicitudId);

        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> {
                        logger.error("Solicitud no encontrada con ID '{}'", solicitudId);
                        return new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
                    });

            if (!solicitud.getEstado().equals(SolicitudEstado.PENDIENTE.name())) {
                logger.warn("La solicitud con ID '{}' no está en estado 'Pendiente'", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'Pendiente' y no puede ser modificada.");
            }
            // Actualizar campos solo si están presentes en el DTO
            if (solicitudDTO.getDescripcionInicial() != null) {
                solicitud.setDescripcionInicial(solicitudDTO.getDescripcionInicial());
                logger.info("Descripción inicial actualizada para la solicitud ID '{}'", solicitudId);
            }
            if (solicitudDTO.getPrioridad() != null) {
                String prioridadMayusculas = solicitudDTO.getPrioridad().toUpperCase();
                solicitud.setPrioridad(prioridadMayusculas);
                logger.info("Prioridad actualizada para la solicitud ID '{}'", solicitudId);
            }
            // Guardar la solicitud modificada
            Solicitud solicitudModificada = solicitudRepository.save(solicitud);
            logger.info("Solicitud con ID '{}' modificada exitosamente por el usuario '{}'", solicitudId, username);
            // Convertir la solicitud modificada a DTO y retornar
            return solicitudConverter.entityToDto(solicitudModificada);
        } catch (SolicitudNotFoundException | AccessDeniedException | IllegalStateException e) {
            // Manejo específico de excepciones conocidas
            logger.error("Error procesando la modificación de la solicitud ID '{}': {}", solicitudId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // Manejo general de errores inesperados
            logger.error("Error inesperado al modificar la solicitud con ID '{}', Usuario: '{}'. Detalle: {}", solicitudId, username, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al modificar la solicitud", e);
        }
    }


    /**
     * Elimina una solicitud existente asociada a un usuario autenticado.
     * @param solicitudId ID de la solicitud a eliminar.
     * @param username Nombre del usuario que realiza la operación.
     * @throws SolicitudNotFoundException Si la solicitud no existe.
     * @throws AccessDeniedException Si el usuario no tiene permisos para eliminar la solicitud.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */

    @Override
    @Transactional
    public EliminarSolicitudResponse eliminarSolicitudUsuario(String solicitudId, String username) {
        logger.info("Usuario '{}' intentando eliminar la solicitud con ID '{}'", username, solicitudId);

        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> {
                        logger.error("Solicitud no encontrada con ID '{}'", solicitudId);
                        return new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
                    });

            // Validar permisos del usuario
            if (!solicitud.getUsername().equals(username)) {
                logger.warn("Acceso denegado: el usuario '{}' no es el propietario de la solicitud ID '{}'", username, solicitudId);
                throw new AccessDeniedException("No tiene permisos para eliminar esta solicitud.");
            }
            // Validar estado de la solicitud
            if (!solicitud.getEstado().equals(SolicitudEstado.PENDIENTE.name())) {
                logger.warn("Estado inválido: la solicitud con ID '{}' no está en estado 'PENDIENTE'", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'PENDIENTE', por lo que no puede ser eliminada.");
            }

            // Eliminar la solicitud
            solicitudRepository.delete(solicitud);
            logger.info("Solicitud con ID '{}' eliminada exitosamente por el usuario '{}'", solicitudId, username);

            // Retornar mensaje de éxito
            return new EliminarSolicitudResponse(200, "Solicitud con ID " + solicitudId + " eliminada exitosamente");

        } catch (SolicitudNotFoundException e) {
            logger.error("Error: Solicitud no encontrada. ID: {}", solicitudId, e);
            return new EliminarSolicitudResponse(404, "Solicitud no encontrada con ID: " + solicitudId);
        } catch (AccessDeniedException e) {
            logger.error("Error: Acceso denegado al eliminar la solicitud. ID: {}", solicitudId, e);
            return new EliminarSolicitudResponse(403, "No tiene permisos para eliminar esta solicitud.");
        } catch (IllegalStateException e) {
            logger.error("Error: Estado inválido para eliminar. ID: {}", solicitudId, e);
            return new EliminarSolicitudResponse(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar la solicitud con ID '{}', Usuario: '{}'. Detalle: {}", solicitudId, username, e.getMessage(), e);
            return new EliminarSolicitudResponse(500, "Error inesperado al eliminar la solicitud");
        }
    }


    /**
     * Elimina una solicitud específica del sistema como administrador.
     * @param solicitudId ID de la solicitud a eliminar.
     * @throws SolicitudNotFoundException Si la solicitud no existe en la base de datos.
     * @throws RuntimeException Si la solicitud no está en estado 'Pendiente' o ocurre un error inesperado.
     */
    @Override
    @Transactional
    public EliminarSolicitudResponse eliminarSolicitudAdmin(String solicitudId) {
        logger.info("El administrador está intentando eliminar la solicitud con ID '{}'", solicitudId);

        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> {
                        logger.error("Solicitud no encontrada con ID '{}'", solicitudId);
                        return new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
                    });
            // Eliminar la solicitud
            solicitudRepository.delete(solicitud);
            logger.info("Solicitud con ID '{}' eliminada exitosamente por el administrador", solicitudId);
            return new EliminarSolicitudResponse(200, "Solicitud con ID " + solicitudId + " eliminada exitosamente");
        } catch (SolicitudNotFoundException e) {
            // Manejo específico de solicitudes no encontradas
            logger.error("Error: Solicitud no encontrada al intentar eliminarla. ID: '{}'", solicitudId, e);
            throw e;
        } catch (IllegalStateException e) {
            // Manejo específico de estados no válidos para eliminar
            logger.error("Error: La solicitud con ID '{}' no puede ser eliminada debido a un estado inválido. Detalle: {}", solicitudId, e.getMessage());
            throw e;

        } catch (Exception e) {
            // Manejo general de errores inesperados
            logger.error("Error inesperado al eliminar la solicitud con ID '{}' por el administrador: {}", solicitudId, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al eliminar la solicitud por administrador", e);
        }
    }

    /**
     * Rechaza una solicitud en estado PENDIENTE.
     * @param solicitudId ID de la solicitud a rechazar.
     * @return DTO con los datos actualizados de la solicitud.
     * @throws SolicitudNotFoundException Si la solicitud no existe.
     * @throws IllegalStateException      Si la solicitud no está en estado PENDIENTE.
     * @throws RuntimeException           Para errores inesperados.
     */
    public SolicitudDTO rechazarSolicitud(String solicitudId) {
        // Validar el formato del ID
        if (!solicitudId.toUpperCase().startsWith("SOLICITUD-")) {
            logger.error("El ID ingresado '{}' no tiene el formato correcto. Debe comenzar con 'SOLICITUD-'.", solicitudId);
            throw new IllegalArgumentException("El ID proporcionado no es válido. Debe comenzar con 'SOLICITUD-' seguido de un número.");
        }
        // Convertir el ID a mayúsculas
        String normalizedSolicitudId = solicitudId.toUpperCase();
        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(normalizedSolicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId));
            // Validar que la solicitud esté en estado PENDIENTE
            if (SolicitudEstado.valueOf(solicitud.getEstado()) != SolicitudEstado.PENDIENTE) {
                logger.warn("Intento de rechazar una solicitud que no está en estado PENDIENTE. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'Pendiente, Aceptado o Rechazado'");
            }
            if (SolicitudEstado.valueOf(solicitud.getEstado()) == SolicitudEstado.ACEPTADO) {
                logger.warn("Intento de rechazar una solicitud ya aceptada. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud ya fue aceptada y no puede ser rechazada.");
            }
            // Actualizar estado a RECHAZADO
            solicitud.setEstado(SolicitudEstado.SOLICITUD_RECHAZADA.name());
            Solicitud solicitudRechazada = solicitudRepository.save(solicitud);
            // Log de éxito
            logger.info("Solicitud con ID {} rechazada exitosamente.", solicitudId);
            // Convertir entidad a DTO y retornar
            return solicitudConverter.entityToDto(solicitudRechazada);
        } catch (SolicitudNotFoundException e) {
            logger.error("Error: Solicitud no encontrada. ID: {}", solicitudId, e);
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Error: Estado inválido para rechazo. ID: {}", solicitudId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al rechazar la solicitud con ID {}: ", solicitudId, e);
            throw new RuntimeException("Error interno al rechazar la solicitud");
        }
    }

    public static String normalizarYValidarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.trim().isEmpty()) {
            throw new IllegalArgumentException("La prioridad es obligatoria y debe ser ALTA, MEDIA o BAJA.");
        }
        String normalizedPrioridad = prioridad.trim().toUpperCase();
        if (!normalizedPrioridad.equals("ALTA") && !normalizedPrioridad.equals("MEDIA") && !normalizedPrioridad.equals("BAJA")) {
            throw new IllegalArgumentException("La prioridad proporcionada no es válida. Use ALTA, MEDIA o BAJA.");
        }
        return normalizedPrioridad;
    }

    private void generarYCrearTicket(Solicitud solicitud, User user) {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId("TICKET-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.TICKET_SEQUENCE));
        ticketDTO.setSolicitudId(solicitud.getIdSolicitud());
        ticketDTO.setPrioridad(solicitud.getPrioridad());
        ticketDTO.setUsername(user.getUsername());
        ticketDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
        ticketDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
        ticketDTO.setEstado(TicketEstado.TRABAJO_PENDIENTE.name());

        // Llamar al servicio de tickets para crear el ticket automáticamente
        ticketService.crearTicketAutomatico(ticketDTO, user.getUsername());

        // Loguear la creación del ticket
        logger.info("Ticket creado exitosamente para la solicitud ID '{}'", solicitud.getIdSolicitud());
    }

}
