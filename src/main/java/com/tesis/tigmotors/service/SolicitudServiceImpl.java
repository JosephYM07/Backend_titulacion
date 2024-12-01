package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.InvalidSolicitudStateException;
import com.tesis.tigmotors.Exceptions.SolicitudNotFoundException;
import com.tesis.tigmotors.converters.SolicitudConverter;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.models.Solicitud;
import com.tesis.tigmotors.enums.SolicitudEstado;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.repository.SolicitudRepository;
import com.tesis.tigmotors.service.interfaces.SolicitudService;
import com.tesis.tigmotors.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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

    @Override
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudDTO solicitudDTO, String username) {
        try {
            // Convertir el DTO a entidad
            Solicitud solicitud = solicitudConverter.dtoToEntity(solicitudDTO);

            // Generar ID único para la solicitud
            solicitud.setIdSolicitud("SOLICITUD-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.SOLICITUD_SEQUENCE));

            // Asignar datos al modelo
            solicitud.setUsername(username);
            solicitud.setEstado(SolicitudEstado.PENDIENTE.name());

            // Asignar fecha y hora de creación automáticas
            solicitud.setFechaCreacion(LocalDate.now(ZoneId.of("America/Guayaquil")));
            solicitud.setHoraCreacion(LocalTime.now(ZoneId.of("America/Guayaquil")));

            // Guardar la solicitud en la base de datos
            Solicitud solicitudGuardada = solicitudRepository.save(solicitud);

            // Convertir la entidad guardada a DTO de respuesta
            return solicitudConverter.entityToResponseDto(solicitudGuardada);

        } catch (Exception e) {
            logger.error("Error creando la solicitud: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando la solicitud", e);
        }
    }

    // Aceptar una solicitud (Administrador)
    @Override
    @Transactional
    public SolicitudResponseDTO aceptarSolicitud(String solicitudId) {
        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

            // Validar si la solicitud ya está aceptada
            if (solicitud.getEstado().equals(SolicitudEstado.ACEPTADO.name())) {
                logger.warn("Intento de aceptar una solicitud ya aceptada. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud ya está en estado 'Aceptado'");
            }
            // Validar si la solicitud está en estado PENDIENTE
            if (!solicitud.getEstado().equals(SolicitudEstado.PENDIENTE.name())) {
                logger.warn("Intento de aceptar una solicitud que no está en estado PENDIENTE. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'Pendiente'");
            }
            // Cambiar el estado a ACEPTADO
            solicitud.setEstado(SolicitudEstado.ACEPTADO.name());
            Solicitud solicitudAceptada = solicitudRepository.save(solicitud);

            // Log exitoso
            logger.info("Solicitud con ID {} aceptada exitosamente.", solicitudId);

            // Convertir la entidad actualizada a un DTO de respuesta y retornarlo
            return solicitudConverter.entityToResponseDto(solicitudAceptada);

        } catch (SolicitudNotFoundException e) {
            // Manejo específico para solicitudes no encontradas
            logger.error("Error: Solicitud no encontrada. ID: {}", solicitudId, e);
            throw new RuntimeException("Error: La solicitud no existe. ID: " + solicitudId, e);

        } catch (IllegalStateException e) {
            // Manejo de estados inválidos
            logger.error("Error: Estado inválido para aceptar la solicitud. ID: {}, Detalle: {}", solicitudId, e.getMessage());
            throw new RuntimeException("Error: " + e.getMessage(), e);

        } catch (Exception e) {
            // Manejo general de errores inesperados
            logger.error("Error inesperado al aceptar la solicitud con ID {}: ", solicitudId, e);
            throw new RuntimeException("Error interno al aceptar la solicitud", e);
        }
    }

    @Override
    @Transactional
    public SolicitudDTO anadirCotizacion(String solicitudId, Map<String, Object> requestBody, String username) {
        try {
            logger.info("Usuario {} inició el proceso para añadir cotización. ID Solicitud: {}", username, solicitudId);

            // Validar y extraer parámetros del requestBody
            Double cotizacion = Optional.ofNullable(requestBody.get("cotizacion"))
                    .map(Object::toString)
                    .map(Double::valueOf)
                    .orElseThrow(() -> new IllegalArgumentException("El valor de 'cotizacion' es inválido o no está presente."));

            String descripcionTrabajo = Optional.ofNullable(requestBody.get("descripcionTrabajo"))
                    .map(Object::toString)
                    .filter(descripcion -> !descripcion.isBlank())
                    .orElseThrow(() -> new IllegalArgumentException("La 'descripcionTrabajo' es obligatoria."));

            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
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
            // Log de éxito
            logger.info("Usuario {} añadió cotización correctamente a la solicitud con ID: {}", username, solicitudId);

            // Convertir a DTO y retornar
            return solicitudConverter.entityToDto(solicitudConCotizacion);

        } catch (Exception e) {
            logger.error("Error inesperado al añadir cotización. ID: {}, Usuario: {}", solicitudId, username, e);
            throw e;
        }
    }


    @Override
    @Transactional
    public TicketDTO aceptarCotizacionGenerarTicket(String solicitudId, String username) {
        logger.info("Usuario '{}' intentando aceptar cotización para solicitud ID '{}'", username, solicitudId);

        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

            // Validar que el usuario tiene permisos para aceptar la cotización
            if (!solicitud.getUsername().equals(username)) {
                logger.error("Acceso denegado: usuario '{}' no coincide con el propietario de la solicitud '{}'", username, solicitud.getUsername());
                throw new AccessDeniedException("No tiene permisos para aceptar esta cotización.");
            }

            // Verificar que el estado de la solicitud sea 'ACEPTADO'
            if (!SolicitudEstado.ACEPTADO.name().equals(solicitud.getEstado())) {
                logger.error("Estado inválido: la solicitud con ID '{}' no está en estado 'ACEPTADO'", solicitudId);
                throw new InvalidSolicitudStateException("La solicitud debe estar en estado 'ACEPTADO' para aceptar la cotización.");
            }

            // Verificar que la cotización no haya sido ya aceptada
            if (SolicitudEstado.COTIZACION_ACEPTADA.name().equals(solicitud.getCotizacionAceptada())) {
                logger.error("Intento de aceptar una cotización ya aceptada. Solicitud ID '{}'", solicitudId);
                throw new InvalidSolicitudStateException("La cotización ya ha sido aceptada.");
            }

            // Cambiar el estado de la cotización a 'COTIZACION_ACEPTADA'
            solicitud.setCotizacionAceptada(SolicitudEstado.COTIZACION_ACEPTADA.name());
            solicitudRepository.save(solicitud);
            logger.info("Cotización aceptada para solicitud ID '{}'", solicitudId);

            // Crear el ticket utilizando la interfaz del servicio
            TicketDTO ticketDTO = new TicketDTO();
            ticketDTO.setId("TICKET-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.TICKET_SEQUENCE));
            ticketDTO.setSolicitudId(solicitud.getIdSolicitud());
            ticketDTO.setUsername(solicitud.getUsername());
            ticketDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
            ticketDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
            ticketDTO.setEstado(TicketEstado.PENDIENTE.name()); // Estado inicial del ticket
            ticketDTO.setAprobado(true);

            // Usar el servicio a través de la interfaz para crear el ticket
            return ticketService.crearTicketAutomatico(ticketDTO, solicitud.getUsername());

        } catch (SolicitudNotFoundException | AccessDeniedException | InvalidSolicitudStateException e) {
            logger.error("Error procesando la solicitud ID '{}': {}", solicitudId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado aceptando cotización y generando ticket para solicitud ID '{}', usuario '{}': {}", solicitudId, username, e.getMessage(), e);
            throw new RuntimeException("Error aceptando la cotización y generando el ticket", e);
        }
    }


    // Usuario rechaza la cotización
    public SolicitudDTO rechazarCotizacion(String solicitudId, String username) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (!solicitud.getUsername().equals(username)) {
                throw new AccessDeniedException("No tiene permisos para rechazar esta cotización.");
            }

            if (solicitud.getEstado().equals("Aceptado")) {
                solicitud.setCotizacionAceptada(SolicitudEstado.RECHAZO_COTIZACION_USUARIO.name());
                solicitud.setEstado("Rechazada por Usuario");
                Solicitud solicitudRechazada = solicitudRepository.save(solicitud);
                return solicitudConverter.entityToDto(solicitudRechazada);
            } else {
                throw new RuntimeException("La solicitud no está en estado 'Aceptado'");
            }
        } catch (Exception e) {
            logger.error("Error rechazando la cotización: ", e);
            throw new RuntimeException("Error rechazando la cotización");
        }
    }

    // Obtener historial de solicitudes por usuario
    public List<SolicitudDTO> obtenerHistorialSolicitudesPorUsuario(String username) {
        try {
            List<Solicitud> solicitudes = solicitudRepository.findByUsername(username);
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo el historial de solicitudes del usuario: ", e);
            throw new RuntimeException("Error obteniendo el historial de solicitudes del usuario");
        }
    }

    // Obtener solicitudes por estado (administrador)
    public List<SolicitudDTO> obtenerSolicitudesPorEstado(String estado) {
        try {
            List<Solicitud> solicitudes = solicitudRepository.findByEstado(estado);
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo las solicitudes por estado: ", e);
            throw new RuntimeException("Error obteniendo las solicitudes por estado");
        }
    }

    // Obtener solicitudes por usuario y estado (usuario autenticado)
    public List<SolicitudDTO> obtenerSolicitudesPorUsuarioYEstado(String username, String estado) {
        try {
            List<Solicitud> solicitudes = solicitudRepository.findByUsernameAndEstado(username, estado);
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo las solicitudes del usuario por estado: ", e);
            throw new RuntimeException("Error obteniendo las solicitudes del usuario por estado");
        }
    }

    // Obtener solicitudes por prioridad (administrador)
    public List<SolicitudDTO> obtenerSolicitudesPorPrioridad(String prioridad) {
        try {
            List<Solicitud> solicitudes = solicitudRepository.findByPrioridad(prioridad);
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo las solicitudes por prioridad: ", e);
            throw new RuntimeException("Error obteniendo las solicitudes por prioridad");
        }
    }

    // Obtener solicitudes por prioridad y usuario (usuario autenticado)
    public List<SolicitudDTO> obtenerSolicitudesPorPrioridadYUsuario(String prioridad, String username) {
        try {
            List<Solicitud> solicitudes = solicitudRepository.findByUsernameAndPrioridad(username, prioridad);
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo las solicitudes del usuario por prioridad: ", e);
            throw new RuntimeException("Error obteniendo las solicitudes del usuario por prioridad");
        }
    }

    // Obtener historial completo de solicitudes (administrador)
    public List<SolicitudDTO> obtenerHistorialCompletoSolicitudes() {
        try {
            List<Solicitud> solicitudes = solicitudRepository.findAll();
            return solicitudes.stream()
                    .map(solicitudConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo el historial completo de solicitudes: ", e);
            throw new RuntimeException("Error obteniendo el historial completo de solicitudes");
        }
    }

    // Modificar una solicitud (usuario autenticado)
    public SolicitudDTO modificarSolicitud(String solicitudId, SolicitudDTO solicitudDTO, String username) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (!solicitud.getUsername().equals(username) || !solicitud.getEstado().equals("Pendiente")) {
                throw new AccessDeniedException("No tiene permisos para modificar esta solicitud o la solicitud no está en estado 'Pendiente'");
            }

            solicitud.setDescripcionInicial(solicitudDTO.getDescripcionInicial());
            solicitud.setPrioridad(solicitudDTO.getPrioridad());
            Solicitud solicitudModificada = solicitudRepository.save(solicitud);
            return solicitudConverter.entityToDto(solicitudModificada);
        } catch (Exception e) {
            logger.error("Error modificando la solicitud: ", e);
            throw new RuntimeException("Error modificando la solicitud");
        }
    }

    // Eliminar una solicitud (usuario autenticado)
    public void eliminarSolicitud(String solicitudId, String username) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (!solicitud.getUsername().equals(username) || !solicitud.getEstado().equals("Pendiente")) {
                throw new AccessDeniedException("No tiene permisos para eliminar esta solicitud o la solicitud no está en estado 'Pendiente'");
            }

            solicitudRepository.delete(solicitud);
            logger.info("Solicitud eliminada exitosamente: {}", solicitudId);
        } catch (SolicitudNotFoundException | AccessDeniedException e) {
            logger.error("Error al eliminar la solicitud: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar la solicitud: ", e);
            throw new RuntimeException("Error inesperado al eliminar la solicitud");
        }
    }

    // Eliminar una solicitud (administrador)
    public void eliminarSolicitudAdmin(String solicitudId) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (!solicitud.getEstado().equals("Pendiente")) {
                throw new RuntimeException("La solicitud no puede ser eliminada porque no está en estado 'Pendiente'");
            }

            solicitudRepository.delete(solicitud);
            logger.info("Solicitud eliminada por administrador: {}", solicitudId);
        } catch (SolicitudNotFoundException e) {
            logger.error("Solicitud no encontrada al intentar eliminar: ", e);
            throw e;
        } catch (RuntimeException e) {
            logger.error("La solicitud no puede ser eliminada: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar la solicitud por administrador: ", e);
            throw new RuntimeException("Error inesperado al eliminar la solicitud por administrador");
        }
    }

    // Rechazar una solicitud (Administrador)

    /**
     * Rechaza una solicitud en estado PENDIENTE.
     *
     * Este método busca la solicitud por ID, valida que esté en estado PENDIENTE,
     * y actualiza su estado a RECHAZADO. Maneja las excepciones para garantizar
     * que los errores sean informativos y registrados adecuadamente.
     *
     * @param solicitudId ID de la solicitud a rechazar.
     * @return DTO con los datos actualizados de la solicitud.
     * @throws SolicitudNotFoundException Si la solicitud no existe.
     * @throws IllegalStateException      Si la solicitud no está en estado PENDIENTE.
     * @throws RuntimeException           Para errores inesperados.
     */
    public SolicitudDTO rechazarSolicitud(String solicitudId) {
        try {
            // Buscar la solicitud por ID
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + solicitudId));
            // Validar que la solicitud esté en estado PENDIENTE
            if (SolicitudEstado.valueOf(solicitud.getEstado()) != SolicitudEstado.PENDIENTE) {
                logger.warn("Intento de rechazar una solicitud que no está en estado PENDIENTE. ID: {}", solicitudId);
                throw new IllegalStateException("La solicitud no está en estado 'Pendiente'");
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

}
