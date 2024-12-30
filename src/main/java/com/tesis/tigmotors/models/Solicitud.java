package com.tesis.tigmotors.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "solicitudes")
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
    private String fechaCreacion;
    private String horaCreacion;

}