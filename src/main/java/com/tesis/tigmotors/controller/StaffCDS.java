package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.*;
import com.tesis.tigmotors.service.*;
import com.tesis.tigmotors.service.interfaces.AdminVerificationUserService;
import com.tesis.tigmotors.service.interfaces.BusquedaUsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff-cds")
public class StaffCDS {
    // Servicios
    private final FacturaServiceImpl facturaService;
    private final TicketServiceImpl ticketServiceImpl;
    private final AdminProfileServiceImpl adminProfileServiceImpl;
    private final FacturaPdfService facturaPdfService;
    private final AdminVerificationUserService adminVerificationUserService;
    private final BusquedaUsuarioService busquedaUsuarioService;
    private final AuthServiceImpl authServiceImpl;


    /**
     * Endpoint exclusivo para el centro de servicios: Obtener información del perfil del usuario autenticado.
     * @param authentication Información de autenticación proporcionada por el contexto de seguridad.
     * @return Respuesta HTTP con los detalles del perfil del usuario autenticado.
     * Proceso:
     * - Obtiene el nombre de usuario del contexto de autenticación.
     * - Llama al servicio para recuperar los detalles del perfil del usuario.
     * Manejo de errores:
     * - 401 UNAUTHORIZED: Si el usuario no está autenticado.
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado al obtener los datos del perfil.
     */
    @GetMapping("/informacion-perfil")
    public ResponseEntity<StaffProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para Staff: {}", username);
        return ResponseEntity.ok(adminProfileServiceImpl.getProfile(username));
    }

    /**
     * Endpoint exclusivo para el centro de servicios: Listar usuarios aprobados.
     *
     * @param authentication Información de autenticación del usuario actual.
     * @return Respuesta HTTP con una lista de usuarios aprobados en formato DTO.
     *
     * Proceso:
     * - Valida que el usuario autenticado tenga los permisos necesarios.
     * - Llama al servicio encargado de obtener la lista de usuarios aprobados.
     *
     * Manejo de errores:
     * - 401 UNAUTHORIZED: Si el usuario no está autenticado.
     * - 403 FORBIDDEN: Si el usuario no tiene los permisos adecuados.
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado durante la ejecución.
     */
    @GetMapping("/lista-usuarios")
    public ResponseEntity<?> obtenerUsuariosAprobados(Authentication authentication) {
        List<PendingUserDTO> usuariosAprobados = adminVerificationUserService.obtenerUsuariosAprobados(authentication);
        return ResponseEntity.ok(usuariosAprobados);
    }

    /**
     * Endpoint exclusivo para el centro de servicios: Listar nombres de usuarios aprobados.
     *
     * @param authentication Información de autenticación del usuario actual.
     * @return Respuesta HTTP con una lista de nombres de usuarios aprobados.
     *
     * Proceso:
     * - Valida que el usuario autenticado tenga los permisos necesarios.
     * - Llama al servicio encargado de obtener los nombres de los usuarios aprobados.
     *
     * Manejo de errores:
     * - 401 UNAUTHORIZED: Si el usuario no está autenticado.
     * - 403 FORBIDDEN: Si el usuario no tiene los permisos adecuados.
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado durante la ejecución.
     */
    @GetMapping("/lista-nombres-usuarios")
    public ResponseEntity<List<String>> obtenerUsernamesAprobados(Authentication authentication) {
        List<String> usernames = adminVerificationUserService.obtenerUsernamesAprobados(authentication);
        return ResponseEntity.ok(usernames);
    }

    /**
     * Endpoint exclusivo para el centro de servicios: Buscar usuario por diferentes criterios.
     *
     * @param request Objeto con los criterios de búsqueda (ID, nombre de usuario o email).
     * @return Respuesta HTTP con la información del usuario encontrado o mensaje de error si no existe.
     *
     * Proceso:
     * - Recibe un objeto `UserRequestDTO` con los parámetros de búsqueda.
     * - Llama al servicio correspondiente para realizar la búsqueda.
     *
     * Manejo de errores:
     * - 400 BAD REQUEST: Si los parámetros de búsqueda no son válidos o están incompletos.
     * - 404 NOT FOUND: Si no se encuentra ningún usuario que cumpla con los criterios.
     */
    @PostMapping("/buscar-usuario")
    public ResponseEntity<?> buscarUsuario(@Valid @RequestBody UserRequestDTO request) {
        // Llama al servicio para procesar la búsqueda
        return busquedaUsuarioService.buscarUsuario(request);
    }
    /*TICKETS*/

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
     * Endpoint exclusivo para el centro de servicios: Listar todos los tickets registrados.
     *
     * @return Respuesta HTTP con la lista de tickets registrados.
     *
     * Proceso:
     * - Llama al servicio de tickets para obtener todos los tickets registrados.
     * - Regresa un listado de tickets en la respuesta.
     *
     * Manejo de errores:
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado al recuperar los tickets.
     */
    @GetMapping("/historial-tickets")
    public ResponseEntity<List<TicketDTO>> listarTodosLosTickets() {
        List<TicketDTO> tickets = ticketServiceImpl.listarTodosLosTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Endpoint para listar tickets por su estado.
     *
     * @param estado El estado de los tickets a listar.
     * @return ResponseEntity con una lista de objetos TicketDTO que coinciden con el estado dado.
     */
    @GetMapping("/estado-ticket/{estado}")
    public ResponseEntity<List<TicketDTO>> listarTicketsPorEstado(@PathVariable String estado) {
        // Llama al servicio para listar tickets por estado
        List<TicketDTO> tickets = ticketServiceImpl.listarTicketsPorEstado(String.valueOf(estado.toUpperCase()));
        return ResponseEntity.ok(tickets);
    }

    /**
     * Endpoint exclusivo para el centro de servicios: Buscar usuario por diferentes criterios.
     *
     * @param request Objeto con los criterios de búsqueda (ID, nombre de usuario o email).
     * @return Respuesta HTTP con la información del usuario encontrado o mensaje de error si no existe.
     *
     * Proceso:
     * - Recibe un objeto `UserRequestDTO` con los parámetros de búsqueda.
     * - Llama al servicio correspondiente para realizar la búsqueda.
     *
     * Manejo de errores:
     * - 400 BAD REQUEST: Si los parámetros de búsqueda no son válidos o están incompletos.
     * - 404 NOT FOUND: Si no se encuentra ningún usuario que cumpla con los criterios.
     */
    @PostMapping("/filtrar-tickets")
    public ResponseEntity<List<TicketDTO>> listarTicketsConFiltros(@RequestBody TicketRequestDTO requestDTO) {
        List<TicketDTO> tickets = ticketServiceImpl.listarTicketsConFiltros(requestDTO);
        return ResponseEntity.ok(tickets);
    }
    /*FACTURAS*/

    /**
     * Endpoint exclusivo para el centro de servicios: Listar todas las facturas registradas.
     *
     * @return Respuesta HTTP con la lista de facturas detalladas.
     *
     * Proceso:
     * - Llama al servicio de facturas para obtener todas las facturas registradas.
     * - Regresa un listado detallado de facturas en la respuesta.
     *
     * Manejo de errores:
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado al recuperar las facturas.
     */
    @GetMapping("/listado-facturas")
    public ResponseEntity<List<FacturaDetalleResponseDTO>> listarTodasLasFacturas() {
        log.info("Solicitud recibida para listar todas las facturas.");
        List<FacturaDetalleResponseDTO> facturas = facturaService.listarTodasLasFacturas();
        log.info("Se envían {} facturas en la respuesta.", facturas.size());
        return ResponseEntity.ok(facturas);
    }

    /**
     * Endpoint para listar facturas aplicando filtros dinámicos.
     *
     * @param requestDTO Objeto que contiene los filtros para la búsqueda de facturas, como fecha de inicio, fecha fin,
     *                   estado de pago y/o usuario.
     * @return ResponseEntity que contiene el objeto FacturaResponseDTO con el resultado de la búsqueda.
     *
     * Proceso:
     * - Recibe los parámetros de filtrado en el cuerpo de la solicitud.
     * - Llama al servicio de facturas para aplicar los filtros y obtener los resultados.
     * - Devuelve un resumen de las facturas que coinciden con los filtros aplicados.
     *
     * Manejo de errores:
     * - Devuelve un mensaje claro en caso de parámetros inválidos o errores internos.
     */
    @PostMapping("/listado-con-filtros")
    public ResponseEntity<FacturaResponseDTO> listarFacturasConFiltros(@RequestBody FacturaRequestDTO requestDTO) {
        log.info("Solicitud recibida para listar facturas con filtros dinámicos: {}", requestDTO);
        FacturaResponseDTO response = facturaService.listarFacturasConFiltros(requestDTO);
        log.info("Se enviarán {} facturas en la respuesta. Total de cotización: {}",
                response.getNumeroFacturas(), response.getTotal());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para generar y descargar un PDF con el reporte de facturas.
     *
     * @param response Objeto HttpServletResponse utilizado para configurar y enviar el archivo PDF al cliente.
     * @param filtros Objeto FacturaRequestDTO que contiene los parámetros de filtrado para generar el reporte.
     *
     * Manejo de errores:
     * - Lanza una excepción si ocurre un problema durante la generación o descarga del PDF.
     */

    @PostMapping(value = "/descargar-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void descargarPdf(HttpServletResponse response, @RequestBody FacturaRequestDTO filtros) {
        try {
            facturaPdfService.generarPdf(response, filtros);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar y descargar el PDF", e);
        }
    }

    /**
     * Endpoint exclusivo para el centro de servicios: Actualizar el estado de pago de una factura.
     *
     * @param facturaId ID de la factura a actualizar.
     * @return Respuesta HTTP con la factura actualizada.
     *
     * Proceso:
     * - Llama al servicio de facturas para actualizar el estado de pago de la factura.
     * - Regresa la factura actualizada en la respuesta.
     *
     * Manejo de errores:
     * - 500 INTERNAL SERVER ERROR: Si ocurre un error inesperado al actualizar el estado de pago.
     */
    @PutMapping("/{facturaId}/actualizar-pago")
    public ResponseEntity<FacturaDetalleResponseDTO> actualizarEstadoPago(@PathVariable String facturaId) {
        FacturaDetalleResponseDTO facturaActualizada = facturaService.actualizarEstadoPago(facturaId);
        return ResponseEntity.ok(facturaActualizada);
    }
}
