package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.*;
import com.tesis.tigmotors.service.*;
import com.tesis.tigmotors.service.interfaces.AdminVerificationUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff-cds")
public class StaffCDS {
    private final FacturaServiceImpl facturaService;
    private final TicketServiceImpl ticketServiceImpl;
    private final AdminProfileServiceImpl adminProfileServiceImpl;
    private final FacturaPdfService facturaPdfService;
    private final AdminVerificationUserService adminVerificationUserService;


    //Informacion Propia
    @GetMapping("/informacion-perfil")
    public ResponseEntity<StaffProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileServiceImpl.getProfile(username));
    }

    @GetMapping("/lista-usuarios")
    public ResponseEntity<?> obtenerUsuariosAprobados(Authentication authentication) {
        List<PendingUserDTO> usuariosAprobados = adminVerificationUserService.obtenerUsuariosAprobados(authentication);
        return ResponseEntity.ok(usuariosAprobados);
    }
    @GetMapping("/lista-nombres-usuarios")
    public ResponseEntity<List<String>> obtenerUsernamesAprobados(Authentication authentication) {
        List<String> usernames = adminVerificationUserService.obtenerUsernamesAprobados(authentication);
        return ResponseEntity.ok(usernames);
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
     * Endpoint para listar todas las facturas.
     *
     * @return Listado de todas las facturas registradas.
     */
    @GetMapping("/listado-facturas")
    public ResponseEntity<List<FacturaDetalleResponseDTO>> listarTodasLasFacturas() {
        log.info("Solicitud recibida para listar todas las facturas.");
        List<FacturaDetalleResponseDTO> facturas = facturaService.listarTodasLasFacturas();
        log.info("Se envían {} facturas en la respuesta.", facturas.size());
        return ResponseEntity.ok(facturas);
    }

    @PostMapping("/listado-con-filtros")
    public ResponseEntity<FacturaResponseDTO> listarFacturasConFiltros(@RequestBody FacturaRequestDTO requestDTO) {
        log.info("Solicitud recibida para listar facturas con filtros dinámicos: {}", requestDTO);
        FacturaResponseDTO response = facturaService.listarFacturasConFiltros(requestDTO);
        log.info("Se enviarán {} facturas en la respuesta. Total de cotización: {}",
                response.getNumeroFacturas(), response.getTotal());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/descargar-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void descargarPdf(HttpServletResponse response, @RequestBody FacturaRequestDTO filtros) {
        try {
            facturaPdfService.generarPdf(response, filtros);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar y descargar el PDF", e);
        }
    }

}
