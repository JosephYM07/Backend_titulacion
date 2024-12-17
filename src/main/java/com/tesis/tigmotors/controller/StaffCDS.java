package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.*;
import com.tesis.tigmotors.dto.Response.*;
import com.tesis.tigmotors.service.*;
import com.tesis.tigmotors.service.interfaces.AdminVerificationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AdminProfileServiceImpl adminProfileServiceImpl;
    private final AdminVerificationUserService adminVerificationUserService;


    //Informacion Propia
    @GetMapping("/informacion-perfil")
    public ResponseEntity<StaffProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo perfil para el administrador: {}", username);
        return ResponseEntity.ok(adminProfileServiceImpl.getProfile(username));
    }

    @GetMapping("/lista-nombres-usuarios")
    public ResponseEntity<List<String>> obtenerUsernamesAprobados(Authentication authentication) {
        List<String> usernames = adminVerificationUserService.obtenerUsernamesAprobados(authentication);
        return ResponseEntity.ok(usernames);
    }

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

    @PostMapping("/listado-con-filtros")
    public ResponseEntity<List<FacturaResponseDTO>> listarFacturasConFiltros(@RequestBody FacturaRequestDTO requestDTO) {
        log.info("Solicitud recibida para listar facturas con filtros dinámicos: {}", requestDTO);
        List<FacturaResponseDTO> facturas = facturaService.listarFacturasConFiltros(requestDTO);
        log.info("Se enviarán {} facturas en la respuesta.", facturas.size());
        return ResponseEntity.ok(facturas);
    }


}
