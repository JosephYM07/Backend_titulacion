package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.FacturaConverter;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.FacturaRepository;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {


    private final FacturaRepository facturaRepository;
    private final TicketRepository ticketRepository;
    private final FacturaConverter facturaConverter;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    public Factura generarFacturaDesdeTicket(String ticketId, double cotizacion) {
        // Buscar el ticket asociado
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + ticketId));

        // Generar el ID único para la factura
        String facturaId = "FACTURA-" + sequenceGeneratorService.generateSequence(SequenceGeneratorService.FACTURA_SEQUENCE);

        // Crear la factura con el ID generado
        Factura factura = facturaConverter.generarFacturaDesdeTicket(ticket, cotizacion);
        factura.setFacturaId(facturaId);

        // Guardar la factura en la base de datos
        return facturaRepository.save(factura);
    }
}