package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.service.TicketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketServiceImpl ticketServiceImpl;

  /*  @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearTicket(@Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        String username = authentication.getName();
        ticketDTO.setEstado("Pendiente"); // Establece el estado como "Pendiente" por defecto
        TicketDTO ticketCreado = ticketService.crearTicket(ticketDTO, username);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Ticket creado exitosamente");
        response.put("ticket", ticketCreado);
        return ResponseEntity.ok(response);
    }*/

    @GetMapping("/historial")
    public ResponseEntity<List<TicketDTO>> obtenerHistorialTickets(Authentication authentication) {
        String username = authentication.getName();
        List<TicketDTO> historialTickets = ticketServiceImpl.obtenerHistorialTickets(username);
        return ResponseEntity.ok(historialTickets);
    }

    @PutMapping("/modificar/{ticketId}")
    public ResponseEntity<TicketDTO> modificarTicket(@PathVariable String ticketId, @Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        String username = authentication.getName();
        TicketDTO ticketModificado = ticketServiceImpl.modificarTicket(ticketId, ticketDTO, username);
        return ResponseEntity.ok(ticketModificado);
    }

    @DeleteMapping("/eliminar/{ticketId}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable String ticketId, Authentication authentication) {
        String username = authentication.getName();
        ticketServiceImpl.eliminarTicket(ticketId, username);
        return ResponseEntity.noContent().build();
    }

}