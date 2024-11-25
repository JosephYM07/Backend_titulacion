package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.Exceptions.SolicitudNotFoundException;
import com.tesis.tigmotors.Jwt.JwtAuthenticationFilter;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.service.SolicitudService;
import com.tesis.tigmotors.service.TicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SolicitudService solicitudService;

    //Endpoints de prueba
    @GetMapping("/hello")
    public String hello() {
        return "Hello, User!";
    }

    // Solicitud
    @PostMapping("/crear-solicitud")
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudDTO solicitudDTO, Authentication authentication) {
        try {
            if (authentication == null) {
                logger.error("No se pudo autenticar al usuario.");
                throw new AccessDeniedException("No se pudo autenticar al usuario.");
            }

            String username = authentication.getName();
            logger.info("Usuario autenticado: {}", username);

            SolicitudDTO nuevaSolicitud = solicitudService.crearSolicitud(solicitudDTO, username);
            logger.info("Solicitud creada exitosamente: {}", nuevaSolicitud.getIdSolicitud());

            return ResponseEntity.ok(nuevaSolicitud);
        } catch (Exception e) {
            logger.error("Error al crear la solicitud: ", e);
            return ResponseEntity.internalServerError().body("Error al crear la solicitud");
        }
    }

    // Endpoint para aceptar la cotización y generar un ticket automáticamente (solo para usuarios)
    @PutMapping("/aceptar-cotizacion/{idSolicitud}")
    public ResponseEntity<TicketDTO> aceptarCotizacion(@PathVariable String idSolicitud, Authentication authentication) {
        String username = authentication.getName();
        TicketDTO ticketGenerado = solicitudService.aceptarCotizacionGenerarTicket(idSolicitud, username);
        return ResponseEntity.ok(ticketGenerado);
    }

    // Endpoint para obtener historial de solicitudes del usuario autenticado
    @GetMapping("/historial-solicitud")
    public ResponseEntity<?> obtenerHistorialSolicitudes(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<SolicitudDTO> solicitudes = solicitudService.obtenerHistorialSolicitudesPorUsuario(username);
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
            List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorUsuarioYEstado(username, estado);
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
            List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorPrioridadYUsuario(prioridad, username);
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
            SolicitudDTO solicitudModificada = solicitudService.modificarSolicitud(solicitudId, solicitudDTO, username);
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
            solicitudService.eliminarSolicitud(solicitudId, username);
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
            List<TicketDTO> historialTickets = ticketService.obtenerHistorialTickets(username);
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
            TicketDTO ticketModificado = ticketService.modificarTicket(ticketId, ticketDTO, username);
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
            ticketService.eliminarTicket(ticketId, username);
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
