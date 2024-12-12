package com.tesis.tigmotors.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "Tickets")
@Data
public class Ticket {
    @Id
    private String id;
    private String solicitudId;
    private String username;
    private String descripcion;
    private String estado;

    // Nuevos campos añadidos
    private String descripcionInicial; // Descripción inicial del problema
    private String descripcionTrabajo;
    private LocalDate fechaCreacion;
    private LocalTime horaCreacion;
}