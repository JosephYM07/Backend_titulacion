package com.tesis.tigmotors.controller;


import com.tesis.tigmotors.dto.Request.AdminProfileUpdateRequest;
import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Response.AdminProfileResponse;
import com.tesis.tigmotors.service.AdminProfileService;
import com.tesis.tigmotors.service.SolicitudService;
import com.tesis.tigmotors.service.TicketService;
import com.tesis.tigmotors.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(SolicitudService.class);


    @Autowired
    private UserService userService;

    @Autowired
    private AdminProfileService adminProfileService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SolicitudService solicitudService;

    @GetMapping("/users/status")
    public ResponseEntity<Object> getUsersStatus() {
        return userService.getUsersStatus();
    }

    @GetMapping("/users/pending")
    public ResponseEntity<Object> getPendingUsers() {
        return userService.getPendingUsers();
    }

    @PutMapping("/users/approve/{userId}")
    public ResponseEntity<Object> approveUser(@PathVariable Integer userId) {
        return userService.approveUser(userId);
    }

    //Crud

    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileService.getProfile(username));
    }

    @PostMapping("/update")
    public ResponseEntity<AdminProfileResponse> updateProfile(Authentication authentication, @RequestBody @Valid AdminProfileUpdateRequest request) {
        String username = authentication.getName();
        log.info("Actualizando perfil del administrador: {}", username);
        return ResponseEntity.ok(adminProfileService.updateProfile(username, request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Eliminando perfil del administrador: {}", username);
        adminProfileService.deleteProfile(username);
        return ResponseEntity.ok(Map.of("message", "Perfil eliminado con éxito"));
    }

    //Solicitudes
    // Endpoint para aceptar una solicitud (solo para administradores)
    @PutMapping("/aceptar/{solicitudId}")
    public ResponseEntity<SolicitudDTO> aceptarSolicitud(@PathVariable String solicitudId, Authentication authentication) {
        String username = authentication.getName();
        logger.info("Usuario {} aceptando la solicitud con ID: {}", username, solicitudId);
        SolicitudDTO solicitudAceptada = solicitudService.aceptarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudAceptada);
    }

    // Endpoint para añadir cotización y descripción del trabajo (solo para administradores)
    @PutMapping("/anadir-cotizacion/{solicitudId}")
    public ResponseEntity<SolicitudDTO> anadirCotizacion(@PathVariable String solicitudId,
                                                         @RequestBody Map<String, String> requestBody,
                                                         Authentication authentication) {
        String username = authentication.getName();
        logger.info("Usuario {} añadiendo cotización a la solicitud con ID: {}", username, solicitudId);

        String cotizacion = requestBody.get("cotizacion");
        String descripcionTrabajo = requestBody.get("descripcionTrabajo");

        SolicitudDTO solicitudConCotizacion = solicitudService.añadirCotizacion(solicitudId, cotizacion, descripcionTrabajo);
        return ResponseEntity.ok(solicitudConCotizacion);
    }

    // Endpoint para rechazar una solicitud (solo para administradores)
    @PutMapping("/rechazar/{solicitudId}")
    public ResponseEntity<SolicitudDTO> rechazarSolicitud(@PathVariable String solicitudId) {
        SolicitudDTO solicitudRechazada = solicitudService.rechazarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudRechazada);
    }

    // Endpoint para obtener todas las solicitudes filtradas por estado (solo para administradores)
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorEstado(@PathVariable String estado) {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para obtener todas las solicitudes filtradas por prioridad (solo para administradores)
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorPrioridad(@PathVariable String prioridad) {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorPrioridad(prioridad);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para obtener historial completo de todas las solicitudes (solo para administradores)
    @GetMapping("/historial")
    public ResponseEntity<List<SolicitudDTO>> obtenerHistorialCompletoSolicitudes() {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerHistorialCompletoSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para eliminar cualquier solicitud (solo para administradores)
    @DeleteMapping("/eliminar/{solicitudId}")
    public ResponseEntity<Void> eliminarSolicitudAdmin(@PathVariable String solicitudId) {
        solicitudService.eliminarSolicitudAdmin(solicitudId);
        return ResponseEntity.noContent().build();
    }

    //TIckets
    @PutMapping("/aprobar/{ticketId}")
    public ResponseEntity<Map<String, Object>> aprobarTicket(@PathVariable String ticketId) {
        TicketDTO ticketAprobado = ticketService.aprobarTicket(ticketId);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Ticket aprobado exitosamente" + "para el ticket con id: " + ticketId + "Usuario: " + ticketAprobado.getUsername());
        response.put("ticket", ticketAprobado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rechazar/{ticketId}")
    public ResponseEntity<Map<String, Object>> rechazarTicket(@PathVariable String ticketId) {
        TicketDTO ticketRechazado = ticketService.rechazarTicket(ticketId);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Ticket rechazado exitosamente" + "para el ticket con id: " + ticketId + "Usuario: " + ticketRechazado.getUsername());
        response.put("ticket", ticketRechazado);
        return ResponseEntity.ok(response);
    }
}