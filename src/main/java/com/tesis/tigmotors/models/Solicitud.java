package com.tesis.tigmotors.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "solicitudes")
@Data
public class Solicitud {
    @Id
    private String idSolicitud;
    private String username;
    private String descripcionInicial;
    private String descripcionTrabajo;
    private String estado;
    private String prioridad;
    private Double cotizacion;
    private String cotizacionAceptada;
    private String Pago;

    private LocalDate fechaCreacion; // Fecha de creación
    private LocalTime horaCreacion; // Hora de creación

}