package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.models.Factura;

public interface FacturaService {
    Factura generarFacturaDesdeTicket(String ticketId, double cotizacion);
}
