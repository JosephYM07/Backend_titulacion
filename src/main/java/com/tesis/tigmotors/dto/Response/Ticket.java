package com.tesis.tigmotors.dto.Response;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "tickets")
@Data
public class Ticket {
    @Id
    private String id;
    private String username;
    private String descripcion;
    private String estado = "Pendiente";
    private boolean aprobado;
}