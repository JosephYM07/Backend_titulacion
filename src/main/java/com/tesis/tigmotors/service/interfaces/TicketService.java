package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.TicketRequestDTO;
import com.tesis.tigmotors.dto.Response.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TicketService {

    /**
     * Obtiene el estado de los tickets.
     *
     * @return ResponseEntity con el estado de los tickets.
     */
    ResponseEntity<Object> getTicketsStatus();

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
    List<TicketDTO> listarTicketsPorEstado(String estado);

    /**
     * Lista todos los tickets sin aplicar ningún filtro.
     *
     * @return una lista de todos los tickets en la base de datos.
     */
    List<TicketDTO> listarTodosLosTickets();

    /**
     * Obtiene el historial de tickets asociados a un usuario específico.
     *
     * @param username El nombre del usuario cuyos tickets se desean consultar.
     * @return Una lista de TicketDTO que representan los tickets del usuario.
     */
    List<TicketDTO> obtenerHistorialTicketsPorUsuario(String username);

    /**
     * Obtiene los tickets asociados a un usuario específico y con una prioridad específica.
     *
     * @param prioridad La prioridad de los tickets a consultar.
     * @param username  El nombre del usuario cuyos tickets se desean consultar.
     * @return Una lista de TicketDTO que representan los tickets del usuario con la prioridad especificada.
     */
    List<TicketDTO> obtenerTicketsPorPrioridadYUsuario(String prioridad, String username);

    /**
     * Obtiene una lista de tickets asociados a un usuario específico y filtrados por estado.
     *
     * @param username El nombre del usuario al que están asociados los tickets.
     * @param estado El estado del ticket (por ejemplo: TRABAJO_PENDIENTE, COMPLETADO).
     * @return Una lista de TicketDTO que representan los tickets del usuario con el estado especificado.
     */
    List<TicketDTO> obtenerTicketsPorUsuarioYEstado(String username, String estado);

    /**
     * Actualiza el estado de un ticket.
     *
     * @param ticketId ID del ticket a actualizar.
     * @param nuevoEstadoStr Nuevo estado del ticket.
     * @return DTO con los datos actualizados del ticket.
     */
    TicketDTO actualizarEstadoTicket(String ticketId, String nuevoEstadoStr);

    /**
     * Lista los tickets aplicando filtros dinámicos.
     * @param requestDTO Contiene los filtros: fecha, usuario, prioridad y estado.
     * @return Lista de tickets filtrados como TicketDTO.
     */
    List<TicketDTO> listarTicketsConFiltros(TicketRequestDTO requestDTO);
}