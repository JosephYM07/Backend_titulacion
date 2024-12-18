package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.Jwt.JwtAuthenticationFilter;
import com.tesis.tigmotors.dto.Request.CambioContraseniaRequest;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Response.TicketDTO;
import com.tesis.tigmotors.dto.Request.UserSelfUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.EliminarSolicitudResponse;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.dto.Response.UserBasicInfoResponseDTO;
import com.tesis.tigmotors.service.SolicitudServiceImpl;
import com.tesis.tigmotors.service.interfaces.TicketService;
import com.tesis.tigmotors.service.interfaces.PasswordResetService;
import com.tesis.tigmotors.service.interfaces.SolicitudService;
import com.tesis.tigmotors.service.interfaces.UserServiceUpdate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TicketService ticketServiceImpl;
    private final SolicitudService solicitudServiceImpl;
    private final UserServiceUpdate userServiceUpdate;
    private final SolicitudService solicitudService;
    private final PasswordResetService passwordResetService;

    /**
     * Endpoint para obtener la información básica del perfil del usuario.
     * Solo disponible para usuarios autenticados.
     *
     * @param authentication Objeto de autenticación que contiene la información del usuario autenticado.
     * @return UserBasicInfoResponseDTO con los datos básicos del perfil del usuario.
     *
     * Manejo de errores:
     * - HTTP 401 (Unauthorized): Si el usuario no está autenticado.
     * - HTTP 403 (Forbidden): Si el usuario no tiene permisos para realizar esta operación.
     * - HTTP 404 (Not Found): Si el perfil del usuario no se encuentra.
     */
    @GetMapping("/informacion-usuario")
    public ResponseEntity<UserBasicInfoResponseDTO> getUserProfile(Authentication authentication) {
        String username = authentication.getName(); // Obtener el username desde el token JWT
        UserBasicInfoResponseDTO userProfile = userServiceUpdate.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Endpoint para actualizar la información básica del perfil del usuario autenticado.
     *
     * @param updateRequest Datos nuevos para actualizar el perfil del usuario.
     * @param authentication Información de autenticación del usuario.
     * @return Detalles del usuario actualizado.
     *
     * Manejo de errores:
     * - HTTP 400: Datos inválidos.
     * - HTTP 401: Usuario no autenticado.
     * - HTTP 403: Operación no permitida.
     * - HTTP 404: Usuario no encontrado.
     */
    @PutMapping("/actualizar-informacion")
    public ResponseEntity<UserBasicInfoResponseDTO> updateMyProfile(
            @Valid @RequestBody UserSelfUpdateRequestDTO updateRequest,
            Authentication authentication) {
        String usernameFromToken = authentication.getName();
        UserBasicInfoResponseDTO updatedUser = userServiceUpdate.updateMyProfile(updateRequest, usernameFromToken);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Endpoint para cambiar la contraseña del usuario autenticado.
     *
     * @param authentication Información de autenticación del usuario.
     * @param cambioContraseniaRequest Datos actuales y nueva contraseña.
     * @return Respuesta indicando el resultado del cambio de contraseña.
     *
     * Manejo de errores:
     * - HTTP 400: Datos inválidos.
     * - HTTP 401: Usuario no autenticado.
     * - HTTP 403: Operación no permitida.
     */
    @PutMapping("/cambiar-contrasena")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @Valid @RequestBody CambioContraseniaRequest cambioContraseniaRequest) {
        String username = authentication.getName();
        return passwordResetService.changePasswordAuthenticated(
                username,
                cambioContraseniaRequest.getCurrentPassword(),
                cambioContraseniaRequest.getNewPassword()
        );
    }

    /**
     * Endpoint para crear una nueva solicitud.
     *
     * @param solicitudDTO Datos de la solicitud a crear.
     * @param authentication Información de autenticación del usuario.
     * @return Detalles de la solicitud creada.
     *
     * Manejo de errores:
     * - HTTP 400: Datos inválidos.
     * - HTTP 401: Usuario no autenticado.
     */
    @PostMapping("/crear-solicitud")
    public ResponseEntity<?> crearSolicitud(@RequestBody @Valid SolicitudDTO solicitudDTO, Authentication authentication) {
        String username = authentication.getName(); // Extrae el username del token
        SolicitudResponseDTO response = solicitudService.crearSolicitud(solicitudDTO, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para aceptar la cotización y generar un ticket automáticamente.
     * Solo disponible para usuarios autenticados.
     *
     * @param idSolicitud ID de la solicitud cuya cotización será aceptada.
     * @param authentication Objeto de autenticación que contiene la información del usuario autenticado.
     * @return SolicitudResponseDTO con los detalles de la solicitud actualizada y el ticket generado.
     *
     * Manejo de errores:
     * - HTTP 400 (Bad Request): Si el ID de la solicitud no es válido o no se puede procesar.
     * - HTTP 401 (Unauthorized): Si el usuario no está autenticado.
     * - HTTP 403 (Forbidden): Si el usuario no tiene permisos para realizar la operación.
     * - HTTP 404 (Not Found): Si la solicitud no existe.
     */
    @PutMapping("/aceptar-cotizacion/{idSolicitud}")
    public ResponseEntity<SolicitudResponseDTO> aceptarCotizacion(@PathVariable String idSolicitud, Authentication authentication) {
        String username = authentication.getName();
        SolicitudResponseDTO solicitudResponse = solicitudServiceImpl.aceptarCotizacionGenerarTicket(idSolicitud, username);
        return ResponseEntity.ok(solicitudResponse);
    }

    /**
     * Endpoint para rechazar una cotización de solicitud.
     *
     * @param solicitudId ID de la solicitud cuya cotización será rechazada.
     * @param authentication Información de autenticación del usuario.
     * @return Detalles de la solicitud actualizada con el estado rechazado.
     *
     * Manejo de errores:
     * - HTTP 400: Datos inválidos.
     * - HTTP 401: Usuario no autenticado.
     * - HTTP 403: Operación no permitida.
     * - HTTP 404: Solicitud no encontrada.
     */
    @PutMapping("/rechazar-cotizacion/{solicitudId}")
    public ResponseEntity<SolicitudDTO> rechazarCotizacion(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName(); // Obtener el nombre del usuario autenticado
        SolicitudDTO solicitudRechazada = solicitudService.rechazarCotizacion(solicitudId, username);
        return ResponseEntity.ok(solicitudRechazada);
    }


    /**
     * Endpoint para obtener el historial de solicitudes del usuario autenticado.
     *
     * @param authentication Información de autenticación del usuario.
     * @return Lista de solicitudes asociadas al usuario autenticado.
     *
     * Manejo de errores:
     * - HTTP 500: Error interno al obtener el historial de solicitudes.
     */
    @GetMapping("/historial-solicitud")
    public ResponseEntity<?> obtenerHistorialSolicitudes(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SolicitudDTO> solicitudes = solicitudServiceImpl.obtenerHistorialSolicitudesPorUsuario(username);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            logger.error("Error al obtener el historial de solicitudes: ", e);
            return ResponseEntity.internalServerError().body("Error al obtener el historial de solicitudes");
        }
    }

    /**
     * Endpoint para obtener el historial de solicitudes del usuario autenticado.
     *
     * @param authentication Información de autenticación del usuario.
     * @return Lista de solicitudes asociadas al usuario autenticado.
     *
     * Manejo de errores:
     * - HTTP 500: Error interno al obtener el historial de solicitudes.
     */
    @GetMapping("/estado-solicitud/{estado}")
    public ResponseEntity<?> obtenerSolicitudesPorEstado(@PathVariable String estado, Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SolicitudDTO> solicitudes = solicitudServiceImpl.obtenerSolicitudesPorUsuarioYEstado(username, estado);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            logger.error("Error al obtener las solicitudes por estado: ", e);
            return ResponseEntity.internalServerError().body("Error al obtener las solicitudes por estado");
        }
    }

    /**
     * Endpoint para obtener las solicitudes del usuario autenticado filtradas por prioridad.
     *
     * @param prioridad Prioridad de las solicitudes a filtrar.
     * @param authentication Información de autenticación del usuario.
     * @return Lista de solicitudes filtradas por prioridad asociadas al usuario autenticado.
     *
     * Manejo de errores:
     * - HTTP 500: Error interno al obtener las solicitudes por prioridad.
     */
    @GetMapping("/prioridad-solicitud/{prioridad}")
    public ResponseEntity<?> obtenerSolicitudesPorPrioridad(@PathVariable String prioridad, Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SolicitudDTO> solicitudes = solicitudServiceImpl.obtenerSolicitudesPorPrioridadYUsuario(prioridad, username);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            logger.error("Error al obtener las solicitudes por prioridad: ", e);
            return ResponseEntity.internalServerError().body("Error al obtener las solicitudes por prioridad");
        }
    }

    /**
     * Endpoint para modificar una solicitud específica del usuario autenticado.
     *
     * @param solicitudId ID de la solicitud a modificar.
     * @param solicitudDTO Datos actualizados de la solicitud.
     * @param authentication Información de autenticación del usuario.
     * @return Detalles de la solicitud modificada.
     *
     * Manejo de errores:
     * - HTTP 403: Acceso denegado al intentar modificar la solicitud.
     * - HTTP 500: Error interno al modificar la solicitud.
     */
    @PutMapping("/modificar-solicitud/{solicitudId}")
    public ResponseEntity<?> modificarSolicitud(@PathVariable String solicitudId, @RequestBody SolicitudDTO solicitudDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            SolicitudDTO solicitudModificada = solicitudServiceImpl.modificarSolicitud(solicitudId, solicitudDTO, username);
            return ResponseEntity.ok(solicitudModificada);
        } catch (AccessDeniedException e) {
            logger.error("Acceso denegado al modificar la solicitud: ", e);
            return ResponseEntity.status(403).body("Acceso denegado al modificar la solicitud");
        } catch (Exception e) {
            logger.error("Error al modificar la solicitud: ", e);
            return ResponseEntity.internalServerError().body("Error al modificar la solicitud");
        }
    }

    /**
     * Endpoint para eliminar una solicitud específica del usuario autenticado.
     *
     * @param solicitudId ID de la solicitud a eliminar.
     * @param authentication Información de autenticación del usuario.
     * @return Respuesta con el estado de la operación y el código HTTP correspondiente.
     *
     * Manejo de errores:
     * - HTTP 403: Acceso denegado al intentar eliminar la solicitud.
     * - HTTP 404: Solicitud no encontrada.
     * - HTTP 500: Error interno al eliminar la solicitud.
     */
    @DeleteMapping("/eliminar-solicitud/{solicitudId}")
    public ResponseEntity<EliminarSolicitudResponse> eliminarSolicitud(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName(); // Extraer el username del token
        EliminarSolicitudResponse resultado = solicitudService.eliminarSolicitudUsuario(solicitudId, username);
        return ResponseEntity.status(resultado.getStatusCode()).body(resultado); // Configura el código de estado HTTP
    }

    /*TICKETS*/

    /**
     * Endpoint para obtener el historial de tickets del usuario autenticado.
     *
     * @param authentication Información de autenticación del usuario.
     * @return Lista de tickets asociados al usuario autenticado.
     *
     * Manejo de errores:
     * - HTTP 500: Error interno al obtener el historial de tickets.
     */
    @GetMapping("/historial-tickets")
    public ResponseEntity<List<TicketDTO>> obtenerHistorialTickets(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TicketDTO> tickets = ticketServiceImpl.obtenerHistorialTicketsPorUsuario(username);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            logger.error("Error al obtener el historial de tickets: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para obtener tickets filtrados por prioridad para el usuario autenticado.
     *
     * @param prioridad La prioridad del ticket (ALTA, MEDIO, BAJA).
     * @param authentication Información de autenticación del usuario actual.
     * @return Una lista de TicketDTO con los tickets del usuario autenticado.
     */
    @GetMapping("/prioridad-ticket/{prioridad}")
    public ResponseEntity<List<TicketDTO>> obtenerTicketsPorPrioridad(@PathVariable String prioridad, Authentication authentication) {
        List<TicketDTO> tickets = ticketServiceImpl.obtenerTicketsPorPrioridadYUsuario(prioridad, authentication.getName());
        return ResponseEntity.ok(tickets);
    }

    /**
     * Endpoint para obtener tickets filtrados por estado para el usuario autenticado.
     *
     * @param estado El estado del ticket (TRABAJO_PENDIENTE, TRABAJO_EN_PROCESO, TRABAJO_FINALIZADO).
     * @param authentication Información de autenticación del usuario actual.
     * @return Una lista de TicketDTO con los tickets del usuario autenticado.
     */
    @GetMapping("/estado-ticket/{estado}")
    public ResponseEntity<List<TicketDTO>> obtenerTicketsPorUsuarioYEstado(@PathVariable String estado, Authentication authentication) {
        String username = authentication.getName();
        List<TicketDTO> tickets = ticketServiceImpl.obtenerTicketsPorUsuarioYEstado(username, estado);
        return ResponseEntity.ok(tickets);
    }


}
