package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.Jwt.JwtAuthenticationFilter;
import com.tesis.tigmotors.dto.Request.ChangePasswordRequest;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Request.UserSelfUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.EliminarSolicitudResponse;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.dto.Response.UserBasicInfoResponseDTO;
import com.tesis.tigmotors.service.SolicitudServiceImpl;
import com.tesis.tigmotors.service.TicketServiceImpl;
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

    private final TicketServiceImpl ticketServiceImpl;
    private final SolicitudServiceImpl solicitudServiceImpl;
    private final UserServiceUpdate userServiceUpdate;
    private final SolicitudService solicitudService;
    private final PasswordResetService passwordResetService;

    /**
     * Obtener información del perfil del usuario autenticado.
     * @param authentication Contexto de autenticación para obtener el username.
     * @return Respuesta con los datos del perfil del usuario.
     */
    @GetMapping("/informacion-usuario")
    public ResponseEntity<UserBasicInfoResponseDTO> getUserProfile(Authentication authentication) {
        String username = authentication.getName(); // Obtener el username desde el token JWT
        UserBasicInfoResponseDTO userProfile = userServiceUpdate.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Actualizar información del usuario autenticado.
     * @param updateRequest Datos que el usuario desea actualizar.
     * @param authentication Información del usuario autenticado.
     * @return Respuesta con los datos actualizados del usuario.
     */
    @PutMapping("/actualizar-informacion")
    public ResponseEntity<UserBasicInfoResponseDTO> updateMyProfile(
            @Valid @RequestBody UserSelfUpdateRequestDTO updateRequest,
            Authentication authentication) {
        // Obtener el username del token
        String usernameFromToken = authentication.getName();

        UserBasicInfoResponseDTO updatedUser = userServiceUpdate.updateMyProfile(updateRequest, usernameFromToken);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     *
     * @param authentication Contexto de autenticación para obtener el username.
     * @param changePasswordRequest Objeto con la contraseña actual y la nueva contraseña.
     * @return Respuesta indicando éxito o error en el cambio de contraseña.
     */
    @PutMapping("/cambiar-contrasena")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        String username = authentication.getName(); // Obtener el username desde el contexto de autenticación
        return passwordResetService.changePasswordAuthenticated(
                username,
                changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword()
        );
    }

    /**
     * Endpoint para crear una solicitud.
     * @param solicitudDTO Objeto con los datos de la solicitud.
     * Debe contener: nombre, email, teléfono, dirección, descripción, prioridad-
     * @return ResponseEntity con la solicitud creada.
     */
    @PostMapping("/crear-solicitud")
    public ResponseEntity<?> crearSolicitud(@RequestBody @Valid SolicitudDTO solicitudDTO, Authentication authentication) {
        String username = authentication.getName(); // Extrae el username del token
        SolicitudResponseDTO response = solicitudService.crearSolicitud(solicitudDTO, username);
        return ResponseEntity.ok(response);
    }

    // Endpoint para aceptar la cotización y generar un ticket automáticamente (solo para usuarios)
    @PutMapping("/aceptar-cotizacion/{idSolicitud}")
    public ResponseEntity<TicketDTO> aceptarCotizacion(@PathVariable String idSolicitud, Authentication authentication) {
        String username = authentication.getName();
        TicketDTO ticketGenerado = solicitudServiceImpl.aceptarCotizacionGenerarTicket(idSolicitud, username);
        return ResponseEntity.ok(ticketGenerado);
    }

    /**
     * Endpoint para que un usuario rechace la cotización de una solicitud.
     *
     * @param solicitudId ID de la solicitud cuya cotización se va a rechazar.
     * @param authentication Información del usuario autenticado.
     * @return Un ResponseEntity con el DTO de la solicitud actualizada.
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
     * @param authentication Información del usuario autenticado.
     * @return Un ResponseEntity con la lista de solicitudes en formato DTO o un mensaje de error en caso de falla.
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
     * Endpoint para obtener las solicitudes del usuario autenticado filtradas por estado (PENDIENTE Y ACEPTADO).
     * @param estado Estado de las solicitudes que se desean filtrar.
     * @param authentication Información del usuario autenticado.
     * @return Un ResponseEntity con la lista de solicitudes en formato DTO o un mensaje de error en caso de falla.
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
     * Endpoint para obtener el historial completo de todas las solicitudes en el sistema.
     * Este endpoint está diseñado exclusivamente para administradores y realiza las siguientes acciones:
     * - Llama al servicio para recuperar todas las solicitudes registradas en el sistema.
     * - Retorna la lista de solicitudes en formato DTO si la operación es exitosa.
     * @return Un ResponseEntity con la lista de todas las solicitudes en formato DTO.
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
     * Endpoint para modificar una solicitud existente del usuario autenticado.
     * @param solicitudId ID de la solicitud que se desea modificar.
     * @param solicitudDTO DTO con los datos actualizados de la solicitud (opcionalmente `descripcionInicial` y/o `prioridad`).
     * @param authentication Información del usuario autenticado.
     * @return Un ResponseEntity con el DTO de la solicitud modificada o un mensaje de error en caso de falla.
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
     * Endpoint para eliminar una solicitud existente del usuario autenticado.
     * @param solicitudId ID de la solicitud que se desea eliminar.
     * @param authentication Información del usuario autenticado.
     * @return Un ResponseEntity sin contenido (HTTP 204) si la operación es exitosa.
     */
    @DeleteMapping("/eliminar-solicitud/{solicitudId}")
    public ResponseEntity<EliminarSolicitudResponse> eliminarSolicitud(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName(); // Extraer el username del token
        EliminarSolicitudResponse resultado = solicitudService.eliminarSolicitudUsuario(solicitudId, username);
        return ResponseEntity.status(resultado.getStatusCode()).body(resultado); // Configura el código de estado HTTP
    }


// Tickets
    /*@PostMapping("/tickets/crear")
    public ResponseEntity<?> crearTicket(@Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            ticketDTO.setEstado("Pendiente"); // Establece el estado como "Pendiente" por defecto
            TicketDTO ticketCreado = ticketService.crearTicket(ticketDTO, username);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Ticket creado exitosamente");
            response.put("ticket", ticketCreado);
            logger.info("Ticket creado exitosamente: {}", ticketCreado.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al crear el ticket: ", e);
            return ResponseEntity.internalServerError().body("Error al crear el ticket");
        }
    }*/

    @GetMapping("/tickets/historial")
    public ResponseEntity<?> obtenerHistorialTickets(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TicketDTO> historialTickets = ticketServiceImpl.obtenerHistorialTickets(username);
            return ResponseEntity.ok(historialTickets);
        } catch (Exception e) {
            logger.error("Error al obtener el historial de tickets: ", e);
            return ResponseEntity.internalServerError().body("Error al obtener el historial de tickets");
        }
    }

    @PutMapping("/tickets/modificar/{ticketId}")
    public ResponseEntity<?> modificarTicket(@PathVariable String ticketId, @Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            TicketDTO ticketModificado = ticketServiceImpl.modificarTicket(ticketId, ticketDTO, username);
            return ResponseEntity.ok(ticketModificado);
        } catch (AccessDeniedException e) {
            logger.error("Acceso denegado al modificar el ticket: ", e);
            return ResponseEntity.status(403).body("Acceso denegado al modificar el ticket");
        } catch (Exception e) {
            logger.error("Error al modificar el ticket: ", e);
            return ResponseEntity.internalServerError().body("Error al modificar el ticket");
        }
    }

    @DeleteMapping("/tickets/eliminar/{ticketId}")
    public ResponseEntity<?> eliminarTicket(@PathVariable String ticketId, Authentication authentication) {
        try {
            String username = authentication.getName();
            ticketServiceImpl.eliminarTicket(ticketId, username);
            logger.info("Ticket eliminado exitosamente: {}", ticketId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            logger.error("Acceso denegado al eliminar el ticket: ", e);
            return ResponseEntity.status(403).body("Acceso denegado al eliminar el ticket");
        } catch (Exception e) {
            logger.error("Error al eliminar el ticket: ", e);
            return ResponseEntity.internalServerError().body("Error al eliminar el ticket");
        }
    }
}
