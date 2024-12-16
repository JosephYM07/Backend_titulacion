package com.tesis.tigmotors.dto.Response;


import lombok.Data;

@Data
public class TicketDTO {
    private String id;
    private String username;
    private String solicitudId;
    private String prioridad;
    private String estado;
    private String descripcionInicial;
    private String descripcionTrabajo;
    private String fechaCreacion;
    private String horaCreacion;

}