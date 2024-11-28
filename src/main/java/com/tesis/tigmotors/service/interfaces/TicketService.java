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

    /**
     * Obtiene el historial de tickets para un usuario autenticado.
     *
     * @param username Nombre del usuario.
     * @return Lista de TicketDTO con los tickets del usuario.
     */
    List<TicketDTO> obtenerHistorialTickets(String username);

    /**
     * Modifica un ticket en estado pendiente. Solo accesible por el usuario propietario.
     *
     * @param ticketId  ID del ticket a modificar.
     * @param ticketDTO Nuevos datos para el ticket.
     * @param username  Usuario autenticado que realiza la modificación.
     * @return TicketDTO con los datos actualizados.
     */
    TicketDTO modificarTicket(String ticketId, TicketDTO ticketDTO, String username);

    /**
     * Elimina un ticket en estado pendiente. Solo accesible por el usuario propietario.
     *
     * @param ticketId ID del ticket a eliminar.
     * @param username Usuario autenticado que realiza la eliminación.
     */
    void eliminarTicket(String ticketId, String username);

    /**
     * Aprueba un ticket pendiente. Accesible por administradores o roles específicos.
     *
     * @param ticketId ID del ticket a aprobar.
     * @return TicketDTO con los datos del ticket aprobado.
     */
    TicketDTO aprobarTicket(String ticketId);

    /**
     * Rechaza un ticket pendiente. Accesible por administradores o roles específicos.
     *
     * @param ticketId ID del ticket a rechazar.
     * @return TicketDTO con los datos del ticket rechazado.
     */
    TicketDTO rechazarTicket(String ticketId);
}