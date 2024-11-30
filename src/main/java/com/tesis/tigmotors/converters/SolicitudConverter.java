package com.tesis.tigmotors.converters;

import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.models.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class SolicitudConverter {

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
}
