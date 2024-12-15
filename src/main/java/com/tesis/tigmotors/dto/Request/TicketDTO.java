package com.tesis.tigmotors.dto.Request;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TicketDTO {
    private String id;
    private String username;
    private String solicitudId;
    private String prioridad;
    private String estado;
    private String descripcionInicial;
    private String descripcionTrabajo;
    private String pago;
    private String fechaCreacion;
    private String horaCreacion;

}