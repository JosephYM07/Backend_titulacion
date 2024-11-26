package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

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
        List<TicketDTO> historialTickets = ticketService.obtenerHistorialTickets(username);
        return ResponseEntity.ok(historialTickets);
    }

    @PutMapping("/modificar/{ticketId}")
    public ResponseEntity<TicketDTO> modificarTicket(@PathVariable String ticketId, @Valid @RequestBody TicketDTO ticketDTO, Authentication authentication) {
        String username = authentication.getName();
        TicketDTO ticketModificado = ticketService.modificarTicket(ticketId, ticketDTO, username);
        return ResponseEntity.ok(ticketModificado);
    }

    @DeleteMapping("/eliminar/{ticketId}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable String ticketId, Authentication authentication) {
        String username = authentication.getName();
        ticketService.eliminarTicket(ticketId, username);
        return ResponseEntity.noContent().build();
    }

}