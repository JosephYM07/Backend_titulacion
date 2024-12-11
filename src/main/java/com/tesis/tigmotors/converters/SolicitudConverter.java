package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.SolicitudAdminRequestDTO;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.models.Solicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class SolicitudConverter {

    // Formateador para la hora
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Convertir de Entidad a DTO (SolicitudDTO para Request)
    public SolicitudDTO entityToDto(Solicitud solicitud) {
        SolicitudDTO solicitudDTO = new SolicitudDTO();
        solicitudDTO.setIdSolicitud(solicitud.getIdSolicitud());
        solicitudDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
        solicitudDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
        solicitudDTO.setEstado(solicitud.getEstado());
        solicitudDTO.setPrioridad(solicitud.getPrioridad());
        solicitudDTO.setCotizacion(solicitud.getCotizacion());
        solicitudDTO.setPago(solicitud.getPago());
        solicitudDTO.setCotizacionAceptada(solicitud.getCotizacionAceptada());

        if (solicitud.getFechaCreacion() != null) {
            solicitudDTO.setFechaCreacion(solicitud.getFechaCreacion().toString());
        }
        if (solicitud.getHoraCreacion() != null) {
            solicitudDTO.setHoraCreacion(solicitud.getHoraCreacion().format(TIME_FORMATTER));
        }
        return solicitudDTO;
    }

    // Convertir de DTO (SolicitudDTO para Request) a Entidad
    public Solicitud dtoToEntity(SolicitudDTO solicitudDTO) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(solicitudDTO.getIdSolicitud());
        solicitud.setDescripcionInicial(solicitudDTO.getDescripcionInicial());
        solicitud.setDescripcionTrabajo(solicitudDTO.getDescripcionTrabajo());
        solicitud.setEstado(solicitudDTO.getEstado());
        solicitud.setPrioridad(solicitudDTO.getPrioridad());
        solicitud.setCotizacion(solicitudDTO.getCotizacion());
        solicitud.setPago(solicitud.getPago());
        solicitud.setCotizacionAceptada(solicitudDTO.getCotizacionAceptada());

        if (solicitudDTO.getFechaCreacion() != null) {
            solicitud.setFechaCreacion(LocalDate.parse(solicitudDTO.getFechaCreacion()));
        }
        if (solicitudDTO.getHoraCreacion() != null) {
            solicitud.setHoraCreacion(LocalTime.parse(solicitudDTO.getHoraCreacion(), TIME_FORMATTER));
        }
        return solicitud;
    }

    // Convertir de SolicitudAdminRequestDTO a Entidad (Uso específico para solicitudes de admin)
    public Solicitud adminRequestToEntity(SolicitudAdminRequestDTO solicitudAdminRequestDTO) {
        Solicitud solicitud = new Solicitud();
        solicitud.setUsername(solicitudAdminRequestDTO.getUsername());
        solicitud.setDescripcionInicial(solicitudAdminRequestDTO.getDescripcionInicial());
        solicitud.setDescripcionTrabajo(solicitudAdminRequestDTO.getDescripcionTrabajo());
        solicitud.setPrioridad(solicitudAdminRequestDTO.getPrioridad());
        solicitud.setCotizacion(solicitudAdminRequestDTO.getCotizacion());
        return solicitud;
    }

    // Convertir de Entidad a SolicitudResponseDTO
    public SolicitudResponseDTO entityToResponseDto(Solicitud solicitud) {
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setIdSolicitud(solicitud.getIdSolicitud());
        responseDTO.setUsername(solicitud.getUsername());
        responseDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
        responseDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
        responseDTO.setEstado(solicitud.getEstado());
        responseDTO.setPrioridad(solicitud.getPrioridad());
        responseDTO.setPago(solicitud.getPago());
        responseDTO.setCotizacion(solicitud.getCotizacion());
        responseDTO.setCotizacionAceptada(solicitud.getCotizacionAceptada());


        if (solicitud.getFechaCreacion() != null) {
            responseDTO.setFechaCreacion(solicitud.getFechaCreacion());
        }
        if (solicitud.getHoraCreacion() != null) {
            responseDTO.setHoraCreacion(solicitud.getHoraCreacion().withNano(0));
        }
        return responseDTO;
    }
}
