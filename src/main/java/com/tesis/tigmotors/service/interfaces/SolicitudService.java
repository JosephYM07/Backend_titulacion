package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.SolicitudAdminRequestDTO;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.dto.Response.EliminarSolicitudResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface SolicitudService {

    ResponseEntity<Object> getSolicitudesStatus();

    /**
     * Crea una nueva solicitud asociada al usuario autenticado.
     *
     * @param solicitudDTO Objeto con los datos de la solicitud.
     * @param username     Nombre de usuario que crea la solicitud.
     * @return SolicitudDTO con los datos de la solicitud creada.
     */
    SolicitudResponseDTO crearSolicitud(SolicitudDTO solicitudDTO, String username);

    SolicitudResponseDTO registrarSolicitudPorAdmin(SolicitudAdminRequestDTO solicitudAdminRequestDTO);

    /**
     * Acepta una solicitud pendiente. Solo accesible por administradores.
     *
     * @param solicitudId ID de la solicitud a aceptar.
     * @return SolicitudDTO con el estado actualizado.
     */
    SolicitudResponseDTO aceptarSolicitud(String solicitudId);

    /**
     * Añade una cotización y descripción del trabajo a una solicitud aceptada.
     *
     * @param solicitudId        ID de la solicitud a actualizar.
     *                           requestBody Map con los datos de la cotización y descripción del trabajo.
     *                           username Nombre de usuario que añade la cotización.
     *                           requestBody Map con los datos de la cotización y descripción del trabajo.
     * @return SolicitudDTO con los datos actualizados.
     */
    SolicitudDTO anadirCotizacion(String solicitudId, Map<String, Object> requestBody, String username);

    /**
     * Acepta una cotización y genera un ticket automáticamente.
     *
     * @param solicitudId ID de la solicitud a aceptar.
     * @param username    Nombre de usuario que acepta la cotización.
     * @return TicketDTO con los datos del ticket generado.
     */
    SolicitudResponseDTO aceptarCotizacionGenerarTicket(String solicitudId, String username);


    /**
     * Rechaza una cotización asociada a una solicitud.
     *
     * @param solicitudId ID de la solicitud a rechazar.
     * @param username    Nombre de usuario que rechaza la cotización.
     * @return SolicitudDTO con el estado actualizado.
     */
    SolicitudDTO rechazarCotizacion(String solicitudId, String username);

    /**
     * Obtiene el historial de solicitudes realizadas por un usuario.
     *
     * @param username Nombre de usuario.
     * @return Lista de SolicitudDTO con las solicitudes del usuario.
     */
    List<SolicitudDTO> obtenerHistorialSolicitudesPorUsuario(String username);

    /**
     * Obtiene las solicitudes por estado. Solo accesible por administradores.
     *
     * @param estado Estado de las solicitudes a consultar.
     * @return Lista de SolicitudDTO con las solicitudes en el estado indicado.
     */
    List<SolicitudDTO> obtenerSolicitudesPorEstado(String estado);

    /**
     * Obtiene las solicitudes realizadas por un usuario en un estado específico.
     *
     * @param username Nombre de usuario.
     * @param estado   Estado de las solicitudes a consultar.
     * @return Lista de SolicitudDTO con las solicitudes en el estado indicado.
     */
    List<SolicitudDTO> obtenerSolicitudesPorUsuarioYEstado(String username, String estado);

    /**
     * Obtiene las solicitudes filtradas por prioridad. Solo accesible por administradores.
     *
     * @param prioridad Prioridad de las solicitudes a consultar.
     * @return Lista de SolicitudDTO con las solicitudes en la prioridad indicada.
     */
    List<SolicitudDTO> obtenerSolicitudesPorPrioridad(String prioridad);

    /**
     * Obtiene las solicitudes de un usuario filtradas por prioridad.
     *
     * @param prioridad Prioridad de las solicitudes a consultar.
     * @param username  Nombre de usuario.
     * @return Lista de SolicitudDTO con las solicitudes en la prioridad indicada.
     */
    List<SolicitudDTO> obtenerSolicitudesPorPrioridadYUsuario(String prioridad, String username);

    /**
     * Obtiene el historial completo de solicitudes. Solo accesible por administradores.
     *
     * @return Lista de SolicitudDTO con todas las solicitudes.
     */
    List<SolicitudDTO> obtenerHistorialCompletoSolicitudes();

    /**
     * Modifica una solicitud en estado pendiente. Solo accesible por el creador de la solicitud.
     *
     * @param solicitudId ID de la solicitud a modificar.
     * @param solicitudDTO Datos actualizados de la solicitud.
     * @param username     Nombre de usuario que realiza la modificación.
     * @return SolicitudDTO con los datos actualizados.
     */
    SolicitudDTO modificarSolicitud(String solicitudId, SolicitudDTO solicitudDTO, String username);

    /**
     * Elimina una solicitud en estado pendiente. Solo accesible por el creador de la solicitud.
     *
     * @param solicitudId ID de la solicitud a eliminar.
     * @param username    Nombre de usuario que realiza la eliminación.
     */
    EliminarSolicitudResponse eliminarSolicitudUsuario(String solicitudId, String username);

    /**
     * Elimina una solicitud en estado pendiente. Solo accesible por administradores.
     *
     * @param solicitudId ID de la solicitud a eliminar.
     */
    EliminarSolicitudResponse eliminarSolicitudAdmin(String solicitudId);

    /**
     * Rechaza una solicitud pendiente. Solo accesible por administradores.
     *
     * @param solicitudId ID de la solicitud a rechazar.
     * @return SolicitudDTO con el estado actualizado.
     */
    SolicitudDTO rechazarSolicitud(String solicitudId);

}
