package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.Jwt.JwtAuthenticationFilter;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Request.UserSelfUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.SolicitudResponseDTO;
import com.tesis.tigmotors.dto.Response.UserBasicInfoResponseDTO;
import com.tesis.tigmotors.service.SolicitudServiceImpl;
import com.tesis.tigmotors.service.TicketServiceImpl;
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
     * Endpoint para crear una solicitud.
     * @param solicitudDTO Objeto con los datos de la solicitud.
     * Debe contener: nombre, email, teléfono, dirección, descripción, prioridad-
     * @return ResponseEntity con la solicitud creada.
     */
    @PostMapping("/crear-solicitud")
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudDTO solicitudDTO, Authentication authentication) {
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

    // Endpoint para obtener historial de solicitudes del usuario autenticado
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

    // Endpoint para obtener solicitudes del usuario autenticado por estado
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

    // Endpoint para obtener solicitudes del usuario autenticado por prioridad
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

    // Endpoint para modificar una solicitud del usuario autenticado
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

    // Endpoint para eliminar una solicitud del usuario autenticado
    @DeleteMapping("/eliminar-solicitud/{solicitudId}")
    public ResponseEntity<?> eliminarSolicitud(@PathVariable String solicitudId, Authentication authentication) {
        try {
            String username = authentication.getName();
            solicitudServiceImpl.eliminarSolicitud(solicitudId, username);
            logger.info("Solicitud eliminada exitosamente: {}", solicitudId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            logger.error("Acceso denegado al eliminar la solicitud: ", e);
            return ResponseEntity.status(403).body("Acceso denegado al eliminar la solicitud");
        } catch (Exception e) {
            logger.error("Error al eliminar la solicitud: ", e);
            return ResponseEntity.internalServerError().body("Error al eliminar la solicitud");
        }
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
