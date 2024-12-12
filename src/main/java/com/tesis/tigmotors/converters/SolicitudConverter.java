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

    // Formateadores para fechas y horas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Método para validar y formatear fecha (String -> LocalDate)
    private LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("La fecha no puede ser nula o vacía.");
        }
        try {
            return LocalDate.parse(fecha, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de fecha debe ser 'yyyy/MM/dd'.");
        }
    }

    // Método para validar y formatear hora (String -> LocalTime)
    private LocalTime parseHora(String hora) {
        if (hora == null || hora.isBlank()) {
            throw new IllegalArgumentException("La hora no puede ser nula o vacía.");
        }
        try {
            return LocalTime.parse(hora, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de hora debe ser 'HH:mm:ss'.");
        }
    }

    // Método para formatear fecha (LocalDate -> String)
    private String formatFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : null;
    }

    // Método para formatear hora (LocalTime -> String)
    private String formatHora(LocalTime hora) {
        return hora != null ? hora.format(TIME_FORMATTER) : null;
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
        solicitudDTO.setFechaCreacion(formatFecha(solicitud.getFechaCreacion()));
        solicitudDTO.setHoraCreacion(formatHora(solicitud.getHoraCreacion()));

        return solicitudDTO;
    }

    // Convertir de SolicitudDTO a Entidad
    public Solicitud dtoToEntity(SolicitudDTO solicitudDTO) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(solicitudDTO.getIdSolicitud());
        solicitud.setDescripcionInicial(solicitudDTO.getDescripcionInicial());
        solicitud.setDescripcionTrabajo(solicitudDTO.getDescripcionTrabajo());
        solicitud.setEstado(solicitudDTO.getEstado());
        solicitud.setPrioridad(solicitudDTO.getPrioridad());
        solicitud.setCotizacion(solicitudDTO.getCotizacion());
        solicitud.setCotizacionAceptada(solicitudDTO.getCotizacionAceptada());
        solicitud.setPago(solicitudDTO.getPago());

        // Validar y asignar fechas y horas
        if (solicitudDTO.getFechaCreacion() != null) {
            solicitud.setFechaCreacion(parseFecha(solicitudDTO.getFechaCreacion()));
        }
        if (solicitudDTO.getHoraCreacion() != null) {
            solicitud.setHoraCreacion(parseHora(solicitudDTO.getHoraCreacion()));
        }

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
        responseDTO.setFechaCreacion(formatFecha(solicitud.getFechaCreacion()));
        responseDTO.setHoraCreacion(formatHora(solicitud.getHoraCreacion()));

        return responseDTO;
    }
}
