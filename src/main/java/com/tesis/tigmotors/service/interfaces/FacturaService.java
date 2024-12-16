package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.FacturaRequestDTO;
import com.tesis.tigmotors.dto.Response.FacturaResponseDTO;
import com.tesis.tigmotors.models.Factura;

import java.util.List;

public interface FacturaService {
    Factura generarFacturaDesdeTicket(String ticketId, double cotizacion);
    List<FacturaResponseDTO> listarTodasLasFacturas();
    List<FacturaResponseDTO> listarFacturasPorFechas(String fechaInicio, String fechaFin); // Método para filtrar por fechas
}
