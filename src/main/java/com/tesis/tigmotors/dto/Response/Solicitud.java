package com.tesis.tigmotors.dto.Response;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Solicitudes")
@Data
public class Solicitud {

    @Id
    private String idSolicitud; // Identificador único para la solicitud (secuencia)
    private String username; // Usuario que hace la solicitud
    private String descripcion; // Descripción del problema
    private String estado = "Pendiente"; // Estado inicial
    private String prioridad; // Prioridad (ALTA, MEDIA, BAJA)
    private boolean cotizacionAceptada = false; // Para saber si la cotización ha sido aceptada
}