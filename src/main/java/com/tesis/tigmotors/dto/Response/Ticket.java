package com.tesis.tigmotors.dto.Response;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tickets")
@Data
public class Ticket {
    @Id
    private String id;
    private String idSolicitud;
    private String username;
    private String descripcion;
    private String estado = "Pendiente";
    private boolean aprobado;
}