package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.SolicitudNotFoundException;
import com.tesis.tigmotors.converters.SolicitudConverter;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Response.Solicitud;
import com.tesis.tigmotors.repository.SolicitudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudService.class);

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private SolicitudConverter solicitudConverter;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private TicketService ticketService;

    // Crear una solicitud
    public SolicitudDTO crearSolicitud(SolicitudDTO solicitudDTO, String username) {
        try {
            Solicitud solicitud = solicitudConverter.dtoToEntity(solicitudDTO);
            solicitud.setIdSolicitud("SOLICITUD-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.SOLICITUD_SEQUENCE));
            solicitud.setUsername(username);
            solicitud.setEstado("Pendiente"); // Estado inicial
            Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
            return solicitudConverter.entityToDto(solicitudGuardada);
        } catch (Exception e) {
            logger.error("Error creando la solicitud: ", e);
            throw new RuntimeException("Error creando la solicitud");
        }
    }

    // Aceptar una solicitud (Administrador)
    public SolicitudDTO aceptarSolicitud(String solicitudId) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (solicitud.getEstado().equals("Pendiente")) {
                solicitud.setEstado("Aceptado");
                Solicitud solicitudAceptada = solicitudRepository.save(solicitud);
                return solicitudConverter.entityToDto(solicitudAceptada);
            } else {
                throw new RuntimeException("La solicitud no está en estado 'Pendiente'");
            }
        } catch (Exception e) {
            logger.error("Error aceptando la solicitud: ", e);
            throw new RuntimeException("Error aceptando la solicitud");
        }
    }

    // Añadir cotización y descripción del trabajo (Administrador)
    public SolicitudDTO añadirCotizacion(String solicitudId, String cotizacion, String descripcionTrabajo) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (solicitud.getEstado().equals("Aceptado")) {
                solicitud.setCotizacion(cotizacion);
                solicitud.setDescripcionTrabajo(descripcionTrabajo);
                Solicitud solicitudConCotizacion = solicitudRepository.save(solicitud);
                return solicitudConverter.entityToDto(solicitudConCotizacion);
            } else {
                throw new RuntimeException("La solicitud no está en estado 'Aceptado'");
            }
        } catch (Exception e) {
            logger.error("Error añadiendo la cotización: ", e);
            throw new RuntimeException("Error añadiendo la cotización");
        }
    }

    // Usuario acepta la cotización y se genera el ticket automáticamente
    public TicketDTO aceptarCotizacionGenerarTicket(String solicitudId, String username) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (!solicitud.getUsername().equals(username)) {
                throw new AccessDeniedException("No tiene permisos para aceptar esta cotización.");
            }

            if (solicitud.getEstado().equals("Aceptado")) {
                // Cambiar el estado de la solicitud para reflejar que la cotización fue aceptada
                solicitud.setCotizacionAceptada(true);
                solicitud.setEstado("Cotización Aceptada");
                solicitudRepository.save(solicitud);

                // Generar ticket automáticamente al aceptar la cotización
                TicketDTO ticketDTO = new TicketDTO();
                ticketDTO.setId("TICKET-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.TICKET_SEQUENCE));
                ticketDTO.setSolicitudId(solicitudId);
                ticketDTO.setEstado("Generado");
                ticketDTO.setUsername(solicitud.getUsername());
                ticketDTO.setDescripcion(solicitud.getDescripcionTrabajo());
                ticketDTO.setAprobado(true);
                return ticketService.crearTicket(ticketDTO, solicitud.getUsername());
            } else {
                throw new RuntimeException("La solicitud no está en estado 'Aceptado'");
            }
        } catch (Exception e) {
            logger.error("Error aceptando la cotización y generando el ticket: ", e);
            throw new RuntimeException("Error aceptando la cotización y generando el ticket");
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
                solicitud.setCotizacionAceptada(false);
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
    public SolicitudDTO rechazarSolicitud(String solicitudId) {
        try {
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

            if (solicitud.getEstado().equals("Pendiente")) {
                solicitud.setEstado("Rechazado");
                Solicitud solicitudRechazada = solicitudRepository.save(solicitud);
                return solicitudConverter.entityToDto(solicitudRechazada);
            } else {
                throw new RuntimeException("La solicitud no está en estado 'Pendiente'");
            }
        } catch (Exception e) {
            logger.error("Error rechazando la solicitud: ", e);
            throw new RuntimeException("Error rechazando la solicitud");
        }
    }
}