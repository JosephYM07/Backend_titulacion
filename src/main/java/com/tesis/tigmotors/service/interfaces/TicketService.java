package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.TicketDTO;

import java.util.List;

public interface TicketService {

    /**
     * Crea un ticket automáticamente asociado a una solicitud.
     *
     * @param ticketDTO Datos del ticket a crear.
     * @param username  Usuario autenticado que genera el ticket.
     * @return TicketDTO con los datos del ticket creado.
     */
    TicketDTO crearTicketAutomatico(TicketDTO ticketDTO, String username);

}