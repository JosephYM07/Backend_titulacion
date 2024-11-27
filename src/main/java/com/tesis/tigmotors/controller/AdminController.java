package com.tesis.tigmotors.controller;


import com.tesis.tigmotors.dto.Request.SolicitudDTO;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.dto.Request.UserUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.AdminProfileResponse;
import com.tesis.tigmotors.dto.Response.UserResponseUser;
import com.tesis.tigmotors.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(SolicitudService.class);

    private final AdminVerificationUserService userService;
    private final AdminProfileService adminProfileService;
    private final TicketService ticketService;
    private final SolicitudService solicitudService;
    private final UserServiceUpdate UserServiceUpdate;


    @GetMapping("/users/status")
    public ResponseEntity<Object> getUsersStatus() {
        return userService.getUsersStatus();
    }

    // Endpoint para obtener todos los usuarios pendientes de aprobación (solo para administradores)
    @GetMapping("/users/pending")
    public ResponseEntity<Object> getPendingUsers() {
        return userService.getPendingUsers();
    }

    // Endpoint para aprobar un usuario (solo para administradores)
    @PutMapping("/users/approve/{userId}")
    public ResponseEntity<Object> approveUser(@PathVariable Integer userId) {
        return userService.approveUser(userId);
    }


    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileService.getProfile(username));
    }

    //Modiciar informacion de usuario (solo para administradores)
    @PutMapping("/actualizar-datos-user")
    public ResponseEntity<UserResponseUser> updateUser(@Valid @RequestBody UserUpdateRequestDTO updateRequest) {
        UserResponseUser updatedUser = UserServiceUpdate.updateUser(updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Endpoint para eliminar un usuario (solo para administradores)
    @PostMapping("/users/delete")
    public ResponseEntity<Object> deleteUser(@RequestBody Map<String, Integer> requestBody) {
        Integer userId = requestBody.get("userId");

        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El campo 'userId' es obligatorio"));
        }

        return userService.deleteUserById(userId);
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