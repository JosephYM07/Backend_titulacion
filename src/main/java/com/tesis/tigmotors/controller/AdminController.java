package com.tesis.tigmotors.controller;


import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.*;
import com.tesis.tigmotors.service.*;
import com.tesis.tigmotors.service.interfaces.AdminVerificationUserService;
import com.tesis.tigmotors.service.interfaces.AuthService;
import com.tesis.tigmotors.service.interfaces.BusquedaUsuarioService;
import com.tesis.tigmotors.service.interfaces.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final CrudUserImpl crudUserService;
    private final BusquedaUsuarioService busquedaUsuarioService;
    private final AdminVerificationUserService adminVerificationUserService;
    private final AuthService authService;
    private final SolicitudService solicitudService;

    /**
     * Endpoint para obtener la lista de usuarios aprobados con el rol USER.
     *
     * @return Lista de usuarios aprobados.
     */
    @GetMapping("/lista-usuarios")
    public ResponseEntity<?> obtenerUsuariosAprobados(Authentication authentication) {
        List<PendingUserDTO> usuariosAprobados = adminVerificationUserService.obtenerUsuariosAprobados(authentication);
        return ResponseEntity.ok(usuariosAprobados);
    }


    // Endpoint para obtener el estado de los usuarios (solo para administradores)
    @GetMapping("/users/status")
    public ResponseEntity<Object> getUsersStatus() {
        return userService.getUsersStatus();
    }

    @GetMapping("/lista-nombres-usuarios")
    public ResponseEntity<List<String>> obtenerUsernamesAprobados(Authentication authentication) {
        List<String> usernames = adminVerificationUserService.obtenerUsernamesAprobados(authentication);
        return ResponseEntity.ok(usernames);
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

    @PostMapping("/crear-solicitud")
    public ResponseEntity<SolicitudResponseDTO> registrarSolicitudPorAdmin(
            @RequestBody @Valid SolicitudAdminRequestDTO solicitudDTO) {
        SolicitudResponseDTO response = solicitudService.registrarSolicitudPorAdmin(solicitudDTO);
        return ResponseEntity.ok(response);
    }


    //Solicitudes
    @PutMapping("/aceptar/{solicitudId}")
    public ResponseEntity<SolicitudResponseDTO> aceptarSolicitud(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName(); // Obtener el nombre del usuario autenticado
        logger.info("Usuario {} aceptando la solicitud con ID: {}", username, solicitudId);
        SolicitudResponseDTO solicitudAceptada = solicitudService.aceptarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudAceptada); // Retorna directamente la respuesta del servicio
    }

    /**
     * Endpoint para añadir una cotización y descripción del trabajo a una solicitud.
     *
     * @param solicitudId ID de la solicitud a actualizar.
     * @param requestBody Cuerpo de la solicitud que contiene cotización y descripción.
     * @return ResponseEntity con la solicitud actualizada en formato DTO.
     */
    @PutMapping("/anadir-cotizacion/{solicitudId}")
    public ResponseEntity<SolicitudDTO> anadirCotizacion(
            @PathVariable String solicitudId,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        // Obtener el nombre del usuario autenticado
        String username = authentication.getName();
        // Delegar la lógica completa al servicio
        SolicitudDTO solicitudActualizada = solicitudService.anadirCotizacion(solicitudId, requestBody, username);
        // Retornar la respuesta con el DTO actualizado
        return ResponseEntity.ok(solicitudActualizada);
    }

    /**
     * Rechaza una solicitud pendiente. Solo accesible por administradores.
     *
     * @param solicitudId ID de la solicitud a rechazar.
     * @return ResponseEntity con el DTO de la solicitud rechazada.
     */
    @PutMapping("/rechazar-solicitud/{solicitudId}")
    public ResponseEntity<SolicitudDTO> rechazarSolicitud(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName();
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
    @GetMapping("/historial-solicitudes")
    public ResponseEntity<List<SolicitudDTO>> obtenerHistorialCompletoSolicitudes() {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerHistorialCompletoSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint para eliminar cualquier solicitud (solo para administradores)
    @DeleteMapping("/eliminar-solicitud/{solicitudId}")
    public ResponseEntity<EliminarSolicitudResponse> eliminarSolicitudAdmin(@PathVariable String solicitudId) {
        EliminarSolicitudResponse response = solicitudService.eliminarSolicitudAdmin(solicitudId);
        return ResponseEntity.ok(response);
    }

}