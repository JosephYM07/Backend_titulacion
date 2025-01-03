package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.FacturaRequestDTO;
import com.tesis.tigmotors.dto.Response.FacturaDetalleResponseDTO;
import com.tesis.tigmotors.dto.Response.FacturaResponseDTO;
import com.tesis.tigmotors.models.Factura;

import java.util.List;

public interface FacturaService {
    List<FacturaDetalleResponseDTO> filtrarFacturasPorEstadoPagoUsuario(String username, String estadoPago);

    /**
     * Genera una factura a partir de un ticket y una cotización.
     *
     * @param ticketId   ID del ticket asociado.
     * @param cotizacion Monto de la cotización.
     * @return La factura generada.
     */
    Factura generarFacturaDesdeTicket(String ticketId, double cotizacion);

    /**
     * Lista todas las facturas registradas en la base de datos.
     *
     * @return Lista de facturas.
     */

    /**
     * Lista facturas aplicando filtros dinámicos (fechas, usuario, estado de pago).
     *
     * @param requestDTO DTO con los filtros opcionales.
     * @return Lista de facturas que coincidan con los filtros.
     */
    FacturaResponseDTO listarFacturasConFiltros(FacturaRequestDTO requestDTO);

    ;

    /**
     * Lista todas las facturas registradas en la base de datos.
     *
     * @return Lista de todas las facturas.
     */
    List<FacturaDetalleResponseDTO> listarTodasLasFacturas();

    /**
     * Actualiza el estado de pago de una factura.
     *
     * @param facturaId ID de la factura a actualizar.
     * @return La factura actualizada.
     */
    FacturaDetalleResponseDTO actualizarEstadoPago(String facturaId);

    List<FacturaDetalleResponseDTO> obtenerHistorialFacturasPorUsuario(String username);

}
