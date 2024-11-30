package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.models.Solicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class SolicitudConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Convertir de Entidad a DTO
    public SolicitudDTO entityToDto(Solicitud solicitud) {
        SolicitudDTO solicitudDTO = new SolicitudDTO();
        solicitudDTO.setIdSolicitud(solicitud.getIdSolicitud());
        solicitudDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
        solicitudDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
        solicitudDTO.setEstado(solicitud.getEstado());
        solicitudDTO.setPrioridad(solicitud.getPrioridad());
        solicitudDTO.setCotizacion(solicitud.getCotizacion());
        solicitudDTO.setCotizacionAceptada(solicitud.getCotizacionAceptada());
        return solicitudDTO;
    }

    // Convertir de DTO a Entidad
    public Solicitud dtoToEntity(SolicitudDTO solicitudDTO) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(solicitudDTO.getIdSolicitud());
        solicitud.setDescripcionInicial(solicitudDTO.getDescripcionInicial());
        solicitud.setDescripcionTrabajo(solicitudDTO.getDescripcionTrabajo());
        solicitud.setEstado(solicitudDTO.getEstado());
        solicitud.setPrioridad(solicitudDTO.getPrioridad());
        solicitud.setCotizacion(solicitudDTO.getCotizacion());
        solicitud.setCotizacionAceptada(solicitudDTO.getCotizacionAceptada());
        return solicitud;
    }

    // Convertir de Entidad a DTO de Respuesta
    public SolicitudResponseDTO entityToResponseDto(Solicitud solicitud) {
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setIdSolicitud(solicitud.getIdSolicitud());
        responseDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
        responseDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
        responseDTO.setEstado(solicitud.getEstado());
        responseDTO.setPrioridad(solicitud.getPrioridad());
        responseDTO.setCotizacion(solicitud.getCotizacion());
        responseDTO.setCotizacionAceptada(solicitud.getCotizacionAceptada());

        // Formatear fecha y hora antes de asignarlas al DTO
        if (solicitud.getFechaCreacion() != null) {
            responseDTO.setFechaCreacion(solicitud.getFechaCreacion());
        }
        if (solicitud.getHoraCreacion() != null) {
            responseDTO.setHoraCreacion(solicitud.getHoraCreacion().withNano(0)); // Eliminar nanosegundos
        }

        return responseDTO;
    }
}