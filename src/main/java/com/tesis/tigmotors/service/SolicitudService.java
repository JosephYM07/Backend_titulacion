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
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        if (solicitud.getUsername().equals(username) && solicitud.getEstado().equals("Pendiente")) {
            solicitud.setDescripcion(solicitudDTO.getDescripcion());
            solicitud.setPrioridad(solicitudDTO.getPrioridad());
            Solicitud solicitudModificada = solicitudRepository.save(solicitud);
            return solicitudConverter.entityToDto(solicitudModificada);
        } else {
            throw new RuntimeException("La solicitud no puede ser modificada o no pertenece al usuario");
        }
    }


    // Eliminar una solicitud (usuario autenticado)
    public void eliminarSolicitud(String solicitudId, String username) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        if (solicitud.getUsername().equals(username) && solicitud.getEstado().equals("Pendiente")) {
            solicitudRepository.delete(solicitud);
        } else {
            throw new RuntimeException("La solicitud no puede ser eliminada o no pertenece al usuario");
        }
    }

    // Eliminar una solicitud (administrador)
    public void eliminarSolicitudAdmin(String solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        if (solicitud.getEstado().equals("Pendiente")) {
            solicitudRepository.delete(solicitud);
        } else {
            throw new RuntimeException("La solicitud no puede ser eliminada porque no está en estado 'Pendiente'");
        }
    }

    // Aceptar una solicitud (Administrador)
    public SolicitudDTO aceptarSolicitud(String solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        if (solicitud.getEstado().equals("Pendiente")) {
            solicitud.setEstado("Aceptado");
            Solicitud solicitudAceptada = solicitudRepository.save(solicitud);

            // Generar ticket automáticamente al aceptar la solicitud
            TicketDTO ticketDTO = new TicketDTO();
            ticketDTO.setId("TICKET-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.TICKET_SEQUENCE));
            ticketDTO.setSolicitudId(solicitudId);
            ticketDTO.setEstado("Generado");
            ticketDTO.setUsername(solicitud.getUsername());
            ticketService.crearTicket(ticketDTO, solicitud.getUsername());

            return solicitudConverter.entityToDto(solicitudAceptada);
        } else {
            throw new RuntimeException("La solicitud no está en estado 'Pendiente'");
        }
    }

    // Rechazar una solicitud (Administrador)
    public SolicitudDTO rechazarSolicitud(String solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
        if (solicitud.getEstado().equals("Pendiente")) {
            solicitud.setEstado("Rechazado");
            Solicitud solicitudRechazada = solicitudRepository.save(solicitud);
            return solicitudConverter.entityToDto(solicitudRechazada);
        } else {
            throw new RuntimeException("La solicitud no está en estado 'Pendiente'");
        }
    }
}
