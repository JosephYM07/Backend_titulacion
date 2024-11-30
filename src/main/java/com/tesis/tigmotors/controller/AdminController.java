package com.tesis.tigmotors.controller;


import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.AdminProfileResponse;
import com.tesis.tigmotors.dto.Response.UserResponseUser;
import com.tesis.tigmotors.service.*;
import com.tesis.tigmotors.service.interfaces.AuthService;
import com.tesis.tigmotors.service.interfaces.BusquedaUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(SolicitudServiceImpl.class);

    private final AdminVerificationUserServiceImpl userService;
    private final AdminProfileServiceImpl adminProfileServiceImpl;
    private final TicketServiceImpl ticketServiceImpl;
    private final SolicitudServiceImpl solicitudServiceImpl;
    private final CrudUserImpl crudUserService;
    private final BusquedaUsuarioService busquedaUsuarioService;
    private final AuthService authService;

    // Endpoint para obtener el estado de los usuarios (solo para administradores)
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

    //Informacion Propia Adminsitrador
    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileServiceImpl.getProfile(username));
    }

    /* CRUD*/

    //Buscar usuario por id, username o email (solo para administradores)
    @PostMapping("/buscar-usuario")
    public ResponseEntity<?> buscarUsuario(@Valid @RequestBody UserRequestDTO request) {
        // Llama al servicio para procesar la búsqueda
        return busquedaUsuarioService.buscarUsuario(request);
    }

    //Modiciar informacion de usuario (solo para administradores)
    @PutMapping("/actualizar-datos-user")
    public ResponseEntity<UserResponseUser> updateUser(
            @Valid @RequestBody UserUpdateRequestDTO updateRequest,
            Authentication authentication) {
        // Obtener el username del administrador autenticado
        String adminUsername = authentication.getName();
        logger.info("Administrador '{}' actualizando información del usuario con ID: {}", adminUsername, updateRequest.getUserId());
        if (updateRequest.getUserId() < 0) {
            logger.error("El ID del usuario es inválido o no proporcionado.");
            throw new IllegalArgumentException("El ID del usuario es obligatorio y debe ser mayor que 0.");
        }
        UserResponseUser updatedUser = crudUserService.updateUser(updateRequest, adminUsername);
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

    // Registro de usuario por administrador
    @PostMapping("/registrar-usuario")
    public ResponseEntity<?> registerUserByAdmin(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails adminDetails) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Campos obligatorios están vacíos o faltan");
        }
        return authService.registerByAdmin(request, adminDetails.getUsername());
    }

    //Solicitudes
    // Endpoint para aceptar una solicitud (solo para administradores)
    @PutMapping("/aceptar/{solicitudId}")
    public ResponseEntity<SolicitudDTO> aceptarSolicitud(@PathVariable String solicitudId, Authentication authentication) {
        String username = authentication.getName();
        logger.info("Usuario {} aceptando la solicitud con ID: {}", username, solicitudId);
        SolicitudDTO solicitudAceptada = solicitudServiceImpl.aceptarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudAceptada);
    }

    // Endpoint para añadir cotización y descripción del trabajo (solo para administradores)
    @PutMapping("/anadir-cotizacion/{solicitudId}")
    public ResponseEntity<SolicitudDTO> anadirCotizacion(
            @PathVariable String solicitudId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {
        String username = authentication.getName();
        logger.info("Usuario {} añadiendo cotización a la solicitud con ID: {}", username, solicitudId);
        Double cotizacion;
        try {
            cotizacion = Double.parseDouble(requestBody.get("cotizacion"));
        } catch (NumberFormatException e) {
            logger.error("El valor de 'cotizacion' no es un número válido: {}", requestBody.get("cotizacion"));
            return ResponseEntity.badRequest().body(null);
        }
        String descripcionTrabajo = requestBody.get("descripcionTrabajo");
        SolicitudDTO solicitudConCotizacion = solicitudServiceImpl.añadirCotizacion(solicitudId, cotizacion, descripcionTrabajo);
        return ResponseEntity.ok(solicitudConCotizacion);
    }

    // Endpoint para rechazar una solicitud (solo para administradores)
    @PutMapping("/rechazar/{solicitudId}")
    public ResponseEntity<SolicitudDTO> rechazarSolicitud(@PathVariable String solicitudId) {
        SolicitudDTO solicitudRechazada = solicitudServiceImpl.rechazarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudRechazada);
    }

    // Endpoint para obtener todas las solicitudes filtradas por estado (solo para administradores)
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorEstado(@PathVariable String estado) {
        List<SolicitudDTO> solicitudes = solicitudServiceImpl.obtenerSolicitudesPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para obtener todas las solicitudes filtradas por prioridad (solo para administradores)
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorPrioridad(@PathVariable String prioridad) {
        List<SolicitudDTO> solicitudes = solicitudServiceImpl.obtenerSolicitudesPorPrioridad(prioridad);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para obtener historial completo de todas las solicitudes (solo para administradores)
    @GetMapping("/historial")
    public ResponseEntity<List<SolicitudDTO>> obtenerHistorialCompletoSolicitudes() {
        List<SolicitudDTO> solicitudes = solicitudServiceImpl.obtenerHistorialCompletoSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para eliminar cualquier solicitud (solo para administradores)
    @DeleteMapping("/eliminar/{solicitudId}")
    public ResponseEntity<Void> eliminarSolicitudAdmin(@PathVariable String solicitudId) {
        solicitudServiceImpl.eliminarSolicitudAdmin(solicitudId);
        return ResponseEntity.noContent().build();
    }

    //TIckets
    @PutMapping("/aprobar/{ticketId}")
    public ResponseEntity<Map<String, Object>> aprobarTicket(@PathVariable String ticketId) {
        TicketDTO ticketAprobado = ticketServiceImpl.aprobarTicket(ticketId);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Ticket aprobado exitosamente" + "para el ticket con id: " + ticketId + "Usuario: " + ticketAprobado.getUsername());
        response.put("ticket", ticketAprobado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rechazar/{ticketId}")
    public ResponseEntity<Map<String, Object>> rechazarTicket(@PathVariable String ticketId) {
        TicketDTO ticketRechazado = ticketServiceImpl.rechazarTicket(ticketId);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Ticket rechazado exitosamente" + "para el ticket con id: " + ticketId + "Usuario: " + ticketRechazado.getUsername());
        response.put("ticket", ticketRechazado);
        return ResponseEntity.ok(response);
    }
}