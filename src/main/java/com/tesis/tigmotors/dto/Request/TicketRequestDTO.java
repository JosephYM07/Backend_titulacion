package com.tesis.tigmotors.dto.Request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketRequestDTO {
    private String fechaInicio;
    private String fechaFin;
    private String username;   // Filtro opcional: nombre del usuario
    private String prioridad;  // Filtro opcional: ALTA, MEDIA, BAJA
    private String estado;     // Filtro opcional: TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, TRABAJO_TERMINADO
}
