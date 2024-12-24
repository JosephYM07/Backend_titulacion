package com.tesis.tigmotors.controller;


import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.*;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.service.*;
import com.tesis.tigmotors.service.interfaces.*;
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

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(SolicitudServiceImpl.class);

    private final AdminVerificationUserService userService;
    private final AdminProfileService adminProfileServiceImpl;
    private final TicketService ticketServiceImpl;
    private final UserServiceUpdate crudUserService;
    private final BusquedaUsuarioService busquedaUsuarioService;
    private final AuthService authService;
    private final SolicitudService solicitudService;

    /**
     * Obtiene una lista de usuarios aprobados.
     *
     * @param authentication Detalles del usuario autenticado.
     * @return Lista de usuarios aprobados como {@link PendingUserDTO}.
     *
     * HTTP:
     * - 200 OK: Lista obtenida correctamente.
     * - 403 FORBIDDEN: Sin permisos.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @GetMapping("/lista-usuarios")
    public ResponseEntity<?> obtenerUsuariosAprobados(Authentication authentication) {
        List<PendingUserDTO> usuariosAprobados = userService.obtenerUsuariosAprobados(authentication);
        return ResponseEntity.ok(usuariosAprobados);
    }

    /**
     * Obtiene estadísticas sobre el estado de los usuarios. Uso exclusivo de administradores.
     *
     * @return Estadísticas de usuarios en formato dinámico.
     *
     * HTTP:
     * - 200 OK: Estadísticas obtenidas correctamente.
     */
    @GetMapping("/estadisticas-usuarios")
    public ResponseEntity<Object> getUsersStatus() {
        return userService.getUsersStatus();
    }

    /**
     * Obtiene una lista de nombres de usuario aprobados. Uso exclusivo de administradores.
     *
     * @param authentication Detalles del usuario autenticado.
     * @return Lista de nombres de usuario aprobados.
     *
     * HTTP:
     * - 200 OK: Lista obtenida correctamente.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @GetMapping("/lista-nombres-usuarios")
    public ResponseEntity<List<String>> obtenerUsernamesAprobados(Authentication authentication) {
        List<String> usernames = userService.obtenerUsernamesAprobados(authentication);
        return ResponseEntity.ok(usernames);
    }

    /**
     * Obtiene todos los usuarios pendientes de aprobación. Uso exclusivo de administradores.
     *
     * @return Lista de usuarios pendientes de aprobación.
     *
     * HTTP:
     * - 200 OK: Lista obtenida correctamente.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @GetMapping("/usuarios-pendientes")
    public ResponseEntity<Object> getPendingUsers() {
        return userService.getPendingUsers();
    }

    /**
     * Aprueba a un usuario identificado por su ID. Uso exclusivo de administradores.
     *
     * @param userId ID del usuario a aprobar.
     * @return Confirmación de la aprobación del usuario.
     *
     * HTTP:
     * - 200 OK: Usuario aprobado correctamente.
     * - 404 NOT FOUND: Usuario no encontrado.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @PutMapping("/usuarios/aprobar/{userId}")
    public ResponseEntity<Object> approveUser(@PathVariable Integer userId) {
        return userService.approveUser(userId);
    }

    /**
     * Obtiene la información del perfil del administrador autenticado.
     *
     * @param authentication Detalles del usuario autenticado proporcionados por Spring Security.
     * @return Información del perfil en un objeto {@link StaffProfileResponse}.
     *
     * HTTP:
     * - 200 OK: Perfil obtenido correctamente.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @GetMapping("/informacion-perfil")
    public ResponseEntity<StaffProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileServiceImpl.getProfile(username));
    }

    /* CRUD*/

    /**
     * Busca un usuario por ID, nombre de usuario o email. Uso exclusivo de administradores.
     *
     * @param request Objeto {@link UserRequestDTO} con los criterios de búsqueda.
     * @return Información del usuario encontrado o mensaje de error en caso contrario.
     *
     * HTTP:
     * - 200 OK: Usuario encontrado.
     * - 400 BAD REQUEST: Parámetros de búsqueda no válidos o incompletos.
     * - 404 NOT FOUND: Usuario no encontrado.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @PostMapping("/buscar-usuario")
    public ResponseEntity<?> buscarUsuario(@Valid @RequestBody UserRequestDTO request) {
        // Llama al servicio para procesar la búsqueda
        return busquedaUsuarioService.buscarUsuario(request);
    }

    /**
     * Modifica la información de un usuario existente. Uso exclusivo de administradores.
     *
     * @param updateRequest Objeto {@link UserUpdateRequestDTO} con los nuevos datos del usuario.
     * @param authentication Detalles del administrador autenticado proporcionados por Spring Security.
     * @return Información del usuario actualizado en un objeto {@link UserResponseUser}.
     *
     * HTTP:
     * - 200 OK: Usuario actualizado correctamente.
     * - 400 BAD REQUEST: Datos de entrada no válidos (por ejemplo, ID negativo o faltante).
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: Usuario no encontrado.
     */
    @PutMapping("/actualizar-datos-user")
    public ResponseEntity<UserResponseUser> updateUser(
            @Valid @RequestBody UserUpdateRequestDTO updateRequest,
            Authentication authentication) {
        // Obtener el username del administrador autenticado
        String adminUsername = authentication.getName();
        if (updateRequest.getUserId() < 0) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio y debe ser mayor que 0.");
        }
        UserResponseUser updatedUser = crudUserService.updateUser(updateRequest, adminUsername);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Elimina un usuario existente por su ID. Uso exclusivo de administradores.
     *
     * @param requestBody Un mapa con el campo "userId" que indica el ID del usuario a eliminar.
     * @return Confirmación de eliminación o mensaje de error en caso de datos faltantes o fallos.
     *
     * HTTP:
     * - 200 OK: Usuario eliminado correctamente.
     * - 400 BAD REQUEST: Si el campo "userId" no está presente o es inválido.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: Usuario no encontrado.
     */
    @PostMapping("/eliminar-usuarios")
    public ResponseEntity<Object> deleteUser(@RequestBody Map<String, Integer> requestBody) {
        Integer userId = requestBody.get("userId");

        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El campo 'userId' es obligatorio"));
        }

        return userService.deleteUserById(userId);
    }

    /**
     * Registra un nuevo usuario a través del administrador. Uso exclusivo de administradores.
     *
     * @param request Objeto {@link RegisterRequest} con los datos del usuario a registrar.
     * @param adminDetails Detalles del administrador autenticado proporcionados por Spring Security.
     * @return Confirmación del registro del usuario o mensaje de error.
     *
     * Validaciones:
     * - Los campos `username`, `password` y `email` son obligatorios y no pueden estar vacíos.
     *
     * HTTP:
     * - 201 CREATED: Usuario registrado correctamente.
     * - 400 BAD REQUEST: Datos obligatorios faltantes o no válidos.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
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

    /* Solicitudes */

    @GetMapping("/estadisticas-solicitudes")
    public ResponseEntity<Object> getSolicitudesStatus() {
        return solicitudService.getSolicitudesStatus();
    }

    /**
     * Crea una nueva solicitud en nombre de un usuario a través del administrador.
     *
     * @param solicitudDTO Objeto {@link SolicitudAdminRequestDTO} con los datos de la solicitud.
     * @return Un objeto {@link SolicitudResponseDTO} con los detalles de la solicitud creada.
     *
     * HTTP:
     * - 200 OK: Solicitud creada correctamente.
     * - 400 BAD REQUEST: Datos de entrada no válidos o faltantes.
     * - 403 FORBIDDEN: Sin permisos de administrador.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @PostMapping("/crear-solicitud")
    public ResponseEntity<SolicitudResponseDTO> registrarSolicitudPorAdmin(
            @RequestBody @Valid SolicitudAdminRequestDTO solicitudDTO) {
        SolicitudResponseDTO response = solicitudService.registrarSolicitudPorAdmin(solicitudDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Acepta una solicitud existente. Este endpoint está diseñado para ser utilizado por usuarios autenticados.
     *
     * @param solicitudId ID de la solicitud a aceptar.
     * @param authentication Detalles del usuario autenticado proporcionados por Spring Security.
     * @return Un objeto {@link SolicitudResponseDTO} con los detalles de la solicitud aceptada.
     *
     * HTTP:
     * - 200 OK: Solicitud aceptada correctamente.
     * - 400 BAD REQUEST: Datos de entrada no válidos.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: Solicitud no encontrada.
     */
    @PutMapping("/aceptar/{solicitudId}")
    public ResponseEntity<SolicitudResponseDTO> aceptarSolicitud(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName();
        logger.info("Usuario {} aceptando la solicitud con ID: {}", username, solicitudId);
        SolicitudResponseDTO solicitudAceptada = solicitudService.aceptarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudAceptada);
    }

    /**
     * Añade una cotización a una solicitud existente. Este endpoint está diseñado para ser utilizado por usuarios autenticados.
     *
     * @param solicitudId ID de la solicitud a la que se añadirá la cotización.
     * @param requestBody Mapa con los datos necesarios para la cotización (por ejemplo, monto, detalles adicionales, etc.).
     * @param authentication Detalles del usuario autenticado proporcionados por Spring Security.
     * @return Un objeto {@link SolicitudDTO} con los detalles de la solicitud actualizada.
     *
     * HTTP:
     * - 200 OK: Cotización añadida correctamente.
     * - 400 BAD REQUEST: Datos de entrada no válidos.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: Solicitud no encontrada.
     */
    @PutMapping("/anadir-cotizacion/{solicitudId}")
    public ResponseEntity<SolicitudDTO> anadirCotizacion(
            @PathVariable String solicitudId,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {
        String username = authentication.getName();
        SolicitudDTO solicitudActualizada = solicitudService.anadirCotizacion(solicitudId, requestBody, username);
        return ResponseEntity.ok(solicitudActualizada);
    }

    /**
     * Rechaza una solicitud existente. Este endpoint está diseñado para ser utilizado por usuarios autenticados.
     *
     * @param solicitudId ID de la solicitud que se desea rechazar.
     * @param authentication Detalles del usuario autenticado proporcionados por Spring Security.
     * @return Un objeto {@link SolicitudDTO} con los detalles de la solicitud rechazada.
     *
     * HTTP:
     * - 200 OK: Solicitud rechazada correctamente.
     * - 400 BAD REQUEST: Datos de entrada no válidos.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: Solicitud no encontrada.
     */
    @PutMapping("/rechazar-solicitud/{solicitudId}")
    public ResponseEntity<SolicitudDTO> rechazarSolicitud(
            @PathVariable String solicitudId,
            Authentication authentication) {
        String username = authentication.getName();
        SolicitudDTO solicitudRechazada = solicitudService.rechazarSolicitud(solicitudId);
        return ResponseEntity.ok(solicitudRechazada);
    }

    /**
     * Obtiene todas las solicitudes filtradas por estado. Este endpoint es exclusivo para administradores.
     *
     * @param estado Estado de las solicitudes que se desean filtrar (por ejemplo, PENDIENTE, APROBADO, RECHAZADO).
     * @return Una lista de objetos {@link SolicitudDTO} que representan las solicitudes filtradas por el estado especificado.
     *
     * HTTP:
     * - 200 OK: Solicitudes obtenidas correctamente.
     * - 400 BAD REQUEST: El estado proporcionado no es válido.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontraron solicitudes con el estado especificado.
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorEstado(@PathVariable String estado) {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Obtiene todas las solicitudes filtradas por prioridad. Este endpoint es exclusivo para administradores.
     *
     * @param prioridad Prioridad de las solicitudes que se desean filtrar (por ejemplo, ALTA, MEDIA, BAJA).
     * @return Una lista de objetos {@link SolicitudDTO} que representan las solicitudes filtradas por la prioridad especificada.
     *
     * HTTP:
     * - 200 OK: Solicitudes obtenidas correctamente.
     * - 400 BAD REQUEST: La prioridad proporcionada no es válida.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontraron solicitudes con la prioridad especificada.
     */
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<SolicitudDTO>> obtenerSolicitudesPorPrioridad(@PathVariable String prioridad) {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorPrioridad(prioridad);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Obtiene el historial completo de todas las solicitudes registradas en el sistema. Este endpoint es exclusivo para administradores.
     *
     * @return Una lista de objetos {@link SolicitudDTO} que representan el historial completo de solicitudes.
     *
     * HTTP:
     * - 200 OK: Historial de solicitudes obtenido correctamente.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontraron solicitudes en el sistema.
     */
    @GetMapping("/historial-solicitudes")
    public ResponseEntity<List<SolicitudDTO>> obtenerHistorialCompletoSolicitudes() {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerHistorialCompletoSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Elimina una solicitud específica del sistema. Este endpoint es exclusivo para administradores.
     *
     * @param solicitudId ID de la solicitud que se desea eliminar.
     * @return Un objeto {@link EliminarSolicitudResponse} con el estado de la operación de eliminación.
     *
     * HTTP:
     * - 200 OK: La solicitud se eliminó correctamente.
     * - 400 BAD REQUEST: El ID de la solicitud proporcionado no es válido.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontró la solicitud con el ID especificado.
     */
    @DeleteMapping("/eliminar-solicitud/{solicitudId}")
    public ResponseEntity<EliminarSolicitudResponse> eliminarSolicitudAdmin(@PathVariable String solicitudId) {
        EliminarSolicitudResponse response = solicitudService.eliminarSolicitudAdmin(solicitudId);
        return ResponseEntity.ok(response);
    }

    /*Tickets*/


    /**
     * Endpoint para obtener estadísticas sobre el estado de los tickets.
     *
     * @return ResponseEntity con las estadísticas de los tickets.
     *
     * HTTP:
     * - 200 OK: Estadísticas obtenidas correctamente.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     */
    @GetMapping("/estadisticas-tickets")
    public ResponseEntity<Object> getTicketsStatus() {
        return ticketServiceImpl.getTicketsStatus();
    }

    /**
     * Lista todos los tickets filtrados por un estado específico. Este endpoint es exclusivo para administradores.
     *
     * @param estado El estado de los tickets que se desea filtrar (por ejemplo, TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, TRABAJO_TERMINADO).
     * @return Una lista de objetos {@link TicketDTO} que representan los tickets filtrados por el estado especificado.
     *
     * HTTP:
     * - 200 OK: Tickets obtenidos correctamente.
     * - 400 BAD REQUEST: El estado proporcionado no es válido.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontraron tickets con el estado especificado.
     */
    @GetMapping("/estado-ticket/{estado}")
    public ResponseEntity<List<TicketDTO>> listarTicketsPorEstado(@PathVariable String estado) {
        // Llama al servicio para listar tickets por estado
        List<TicketDTO> tickets = ticketServiceImpl.listarTicketsPorEstado(String.valueOf(estado.toUpperCase()));
        return ResponseEntity.ok(tickets);
    }

    /**
     * Lista todos los tickets registrados en el sistema. Este endpoint es exclusivo para administradores.
     *
     * @return Una lista de objetos {@link TicketDTO} que representan todos los tickets disponibles.
     *
     * HTTP:
     * - 200 OK: Tickets obtenidos correctamente.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontraron tickets registrados en el sistema.
     */
    @GetMapping("/historial-tickets")
    public ResponseEntity<List<TicketDTO>> listarTodosLosTickets() {
        List<TicketDTO> tickets = ticketServiceImpl.listarTodosLosTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Actualiza el estado de un ticket existente en el sistema. Este endpoint es exclusivo para administradores.
     *
     * @param ticketId   El ID del ticket que se desea actualizar.
     * @param nuevoEstado El nuevo estado que se asignará al ticket (por ejemplo, TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, TRABAJO_TERMINADO).
     * @return Un objeto {@link TicketDTO} que representa el ticket actualizado.
     *
     * HTTP:
     * - 200 OK: El estado del ticket fue actualizado correctamente.
     * - 400 BAD REQUEST: El nuevo estado proporcionado no es válido o el ticket no permite el cambio solicitado.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontró el ticket con el ID especificado.
     * - 500 INTERNAL SERVER ERROR: Error interno al intentar actualizar el estado del ticket.
     */
    @PutMapping("/{ticketId}/estado-ticket")
    public ResponseEntity<TicketDTO> actualizarEstadoTicket(
            @PathVariable String ticketId,
            @RequestParam String nuevoEstado) {
        TicketDTO ticketActualizado = ticketServiceImpl.actualizarEstadoTicket(ticketId, nuevoEstado);
        return ResponseEntity.ok(ticketActualizado);
    }

    /**
     * Filtra y lista los tickets registrados en el sistema según los criterios especificados.
     * Este endpoint es exclusivo para administradores.
     *
     * @param requestDTO Objeto {@link TicketRequestDTO} que contiene los criterios de filtrado, como:
     *                   - `fechaInicio`: Fecha de inicio del rango (formato: "yyyy/MM/dd").
     *                   - `fechaFin`: Fecha de fin del rango (formato: "yyyy/MM/dd").
     *                   - `username`: Nombre de usuario asociado al ticket (opcional).
     *                   - `estado`: Estado del ticket (opcional, por ejemplo: TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO).
     *                   - `prioridad`: Prioridad del ticket (opcional, por ejemplo: ALTA, MEDIA, BAJA).
     * @return Una lista de objetos {@link TicketDTO} que representan los tickets que cumplen con los criterios de filtrado.
     *
     * HTTP:
     * - 200 OK: Tickets filtrados obtenidos correctamente.
     * - 400 BAD REQUEST: Algún criterio de filtrado es inválido o falta información obligatoria.
     * - 403 FORBIDDEN: Sin permisos para realizar esta acción.
     * - 401 UNAUTHORIZED: Usuario no autenticado.
     * - 404 NOT FOUND: No se encontraron tickets que cumplan con los criterios de filtrado.
     * - 500 INTERNAL SERVER ERROR: Error interno al intentar filtrar los tickets.
     */
    @PostMapping("/filtrar-tickets")
    public ResponseEntity<List<TicketDTO>> listarTicketsConFiltros(@RequestBody TicketRequestDTO requestDTO) {
        List<TicketDTO> tickets = ticketServiceImpl.listarTicketsConFiltros(requestDTO);
        return ResponseEntity.ok(tickets);
    }
}