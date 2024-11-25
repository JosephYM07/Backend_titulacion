package com.tesis.tigmotors.dto.Response;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "solicitudes")
@Data
public class Solicitud {

    @Id
    private String idSolicitud; // Identificador único para la solicitud (secuencia)
    private String username; // Usuario que hace la solicitud
    private String descripcionInicial; // Descripción inicial del problema cuando entra el vehículo
    private String descripcionTrabajo; // Descripción del trabajo que se realizará (cotización)
    private String estado = "Pendiente"; // Estado inicial (Pendiente, Aceptado, Rechazado)
    private String prioridad; // Prioridad (ALTA, MEDIA, BAJA)
    private String cotizacion; // Valor de la cotización (si aplica)
    private boolean cotizacionAceptada = false;
}