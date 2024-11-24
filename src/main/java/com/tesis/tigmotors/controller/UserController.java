package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.service.SolicitudService;
import com.tesis.tigmotors.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private SolicitudService solicitudService;

    // Solicitud
    // Endpoint para crear una solicitud
    @PostMapping("/crear")
    public ResponseEntity<SolicitudDTO> crearSolicitud(@RequestBody SolicitudDTO solicitudDTO, Authentication authentication) {
        String username = authentication.getName();
        SolicitudDTO nuevaSolicitud = solicitudService.crearSolicitud(solicitudDTO, username);
        return ResponseEntity.ok(nuevaSolicitud);
    }

    // Endpoint para obtener historial de solicitudes del usuario autenticado
    @GetMapping("/historial")
    public ResponseEntity<List<SolicitudDTO>> obtenerHistorialSolicitudes(Authentication authentication) {
        String username = authentication.getName();
        List<SolicitudDTO> solicitudes = solicitudService.obtenerHistorialSolicitudesPorUsuario(username);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para obtener solicitudes del usuario autenticado por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorEstado(@PathVariable String estado, Authentication authentication) {
        String username = authentication.getName();
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorUsuarioYEstado(username, estado);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para obtener solicitudes del usuario autenticado por prioridad
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorPrioridad(@PathVariable String prioridad, Authentication authentication) {
        String username = authentication.getName();
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorPrioridadYUsuario(prioridad, username);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para modificar una solicitud del usuario autenticado
    @PutMapping("/modificar/{solicitudId}")
    public ResponseEntity<SolicitudDTO> modificarSolicitud(@PathVariable String solicitudId, @RequestBody SolicitudDTO solicitudDTO, Authentication authentication) {
        String username = authentication.getName();
        SolicitudDTO solicitudModificada = solicitudService.modificarSolicitud(solicitudId, solicitudDTO, username);
        return ResponseEntity.ok(solicitudModificada);
    }

    // Endpoint para eliminar una solicitud del usuario autenticado
    @DeleteMapping("/eliminar/{solicitudId}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable String solicitudId, Authentication authentication) {
        String username = authentication.getName();
        solicitudService.eliminarSolicitud(solicitudId, username);
        return ResponseEntity.noContent().build();
    }

    //Tickets
    @PostMapping("/tickets/crear")
    public ResponseEntity<Map<String, Object>> crearTicket(@Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        String username = authentication.getName();
        ticketDTO.setEstado("Pendiente"); // Establece el estado como "Pendiente" por defecto
        TicketDTO ticketCreado = ticketService.crearTicket(ticketDTO, username);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Ticket creado exitosamente");
        response.put("ticket", ticketCreado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets/historial")
    public ResponseEntity<List<TicketDTO>> obtenerHistorialTickets(Authentication authentication) {
        String username = authentication.getName();
        List<TicketDTO> historialTickets = ticketService.obtenerHistorialTickets(username);
        return ResponseEntity.ok(historialTickets);
    }

    @PutMapping("/tickets/modificar/{ticketId}")
    public ResponseEntity<TicketDTO> modificarTicket(@PathVariable String ticketId, @Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        String username = authentication.getName();
        TicketDTO ticketModificado = ticketService.modificarTicket(ticketId, ticketDTO, username);
        return ResponseEntity.ok(ticketModificado);
    }

    @DeleteMapping("/tickets/eliminar/{ticketId}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable String ticketId, Authentication authentication) {
        String username = authentication.getName();
        ticketService.eliminarTicket(ticketId, username);
        return ResponseEntity.noContent().build();
    }

}