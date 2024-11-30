package com.tesis.tigmotors.dto.Response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SolicitudResponseDTO {
    private String idSolicitud;
    private String descripcionInicial;
    private String descripcionTrabajo;
    private String estado;
    private String prioridad;
    private Double cotizacion;
    private String cotizacionAceptada;
    private LocalDate fechaCreacion; // Fecha de creación
    private LocalTime horaCreacion; // Hora de creación
}