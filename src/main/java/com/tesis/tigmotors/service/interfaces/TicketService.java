package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;

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

    /**
     * Lista los tickets por estado.
     *
     * @param estado Estado del ticket.
     * @return Lista de tickets con el estado especificado.
     */
    List<TicketDTO> listarTicketsPorEstado(TicketEstado estado);

    /**
     * Lista todos los tickets sin aplicar ningún filtro.
     *
     * @return una lista de todos los tickets en la base de datos.
     */
    List<TicketDTO> listarTodosLosTickets();
}