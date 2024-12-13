package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.SolicitudAdminRequestDTO;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.models.Solicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class SolicitudConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Método para asignar automáticamente la fecha y la hora actuales a una solicitud
    public void asignarFechaYHoraActual(Solicitud solicitud) {
        solicitud.setFechaCreacion(LocalDate.now().format(DATE_FORMATTER));
        solicitud.setHoraCreacion(LocalTime.now().format(TIME_FORMATTER));
    }

    // Convertir de Entidad a SolicitudDTO
    public SolicitudDTO entityToDto(Solicitud solicitud) {
        SolicitudDTO solicitudDTO = new SolicitudDTO();
        solicitudDTO.setIdSolicitud(solicitud.getIdSolicitud());
        solicitudDTO.setUsername(solicitud.getUsername());
        solicitudDTO.setDescripcionInicial(solicitud.getDescripcionInicial());
        solicitudDTO.setDescripcionTrabajo(solicitud.getDescripcionTrabajo());
        solicitudDTO.setEstado(solicitud.getEstado());
        solicitudDTO.setPrioridad(solicitud.getPrioridad());
        solicitudDTO.setCotizacion(solicitud.getCotizacion());
        solicitudDTO.setCotizacionAceptada(solicitud.getCotizacionAceptada());
        solicitudDTO.setPago(solicitud.getPago());

        // Formatear y asignar fechas y horas
        solicitudDTO.setFechaCreacion(solicitud.getFechaCreacion());
        solicitudDTO.setHoraCreacion(solicitud.getHoraCreacion());

        return solicitudDTO;
    }

    // Convertir de SolicitudDTO a Entidad
    public Solicitud dtoToEntity(SolicitudDTO solicitudDTO) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(solicitudDTO.getIdSolicitud());
        solicitud.setUsername(solicitudDTO.getUsername());
        solicitud.setDescripcionInicial(solicitudDTO.getDescripcionInicial());
        solicitud.setDescripcionTrabajo(solicitudDTO.getDescripcionTrabajo());
        solicitud.setEstado(solicitudDTO.getEstado());
        solicitud.setPrioridad(solicitudDTO.getPrioridad());
        solicitud.setCotizacion(solicitudDTO.getCotizacion());
        solicitud.setCotizacionAceptada(solicitudDTO.getCotizacionAceptada());
        solicitud.setPago(solicitudDTO.getPago());

        // Validar y asignar fechas y horas
        solicitud.setFechaCreacion(solicitudDTO.getFechaCreacion());
        solicitud.setHoraCreacion(solicitudDTO.getHoraCreacion());

        return solicitud;
    }

    // Convertir de SolicitudAdminRequestDTO a Entidad
    public Solicitud adminRequestToEntity(SolicitudAdminRequestDTO solicitudAdminRequestDTO) {
        Solicitud solicitud = new Solicitud();
        solicitud.setUsername(solicitudAdminRequestDTO.getUsername());
        solicitud.setDescripcionInicial(solicitudAdminRequestDTO.getDescripcionInicial());
        solicitud.setDescripcionTrabajo(solicitudAdminRequestDTO.getDescripcionTrabajo());
        solicitud.setPrioridad(solicitudAdminRequestDTO.getPrioridad());
        solicitud.setCotizacion(solicitudAdminRequestDTO.getCotizacion());

        // Asignar fecha y hora actuales
        asignarFechaYHoraActual(solicitud);

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
        responseDTO.setCotizacion(solicitud.getCotizacion());
        responseDTO.setCotizacionAceptada(solicitud.getCotizacionAceptada());
        responseDTO.setPago(solicitud.getPago());

        // Formatear y asignar fechas y horas
        responseDTO.setFechaCreacion(solicitud.getFechaCreacion());
        responseDTO.setHoraCreacion(solicitud.getHoraCreacion());

        return responseDTO;
    }
}

