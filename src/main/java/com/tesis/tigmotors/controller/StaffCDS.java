package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.*;
import com.tesis.tigmotors.enums.TicketEstado;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/staff-cds")
public class StaffCDS {
    private final FacturaServiceImpl facturaService;

    /**
     * Endpoint para listar todas las facturas.
     *
     * @return Listado de todas las facturas registradas.
     */
    @GetMapping("/listado-facturas")
    public ResponseEntity<List<FacturaResponseDTO>> listarTodasLasFacturas() {
        log.info("Solicitud recibida para listar todas las facturas.");
        List<FacturaResponseDTO> facturas = facturaService.listarTodasLasFacturas();
        log.info("Se envían {} facturas en la respuesta.", facturas.size());
        return ResponseEntity.ok(facturas);
    }

    @PostMapping("/listado-por-fechas")
    public ResponseEntity<List<FacturaResponseDTO>> listarFacturasPorFechas(
            @RequestBody FacturaRequestDTO requestDTO) {
        log.info("Solicitud recibida para listar facturas por rango de fechas: {} - {}",
                requestDTO.getFechaInicio(), requestDTO.getFechaFin());
        List<FacturaResponseDTO> facturas = facturaService.listarFacturasPorFechas(
                requestDTO.getFechaInicio(), requestDTO.getFechaFin());
        log.info("Se envían {} facturas en la respuesta.", facturas.size());
        return ResponseEntity.ok(facturas);
    }

}
