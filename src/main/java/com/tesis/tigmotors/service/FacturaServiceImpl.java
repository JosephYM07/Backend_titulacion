package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.converters.FacturaConverter;
import com.tesis.tigmotors.dto.Response.FacturaResponseDTO;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.FacturaRepository;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.FacturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
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

    @Override
    public List<FacturaResponseDTO> listarTodasLasFacturas() {
        log.info("Iniciando proceso para listar todas las facturas...");
        try {
            List<Factura> facturas = facturaRepository.findAll();

            if (facturas.isEmpty()) {
                log.warn("No se encontraron facturas registradas.");
                throw new ResourceNotFoundException("No hay facturas registradas.");
            }

            log.info("Se encontraron {} facturas registradas.", facturas.size());
            return facturas.stream()
                    .map(facturaConverter::entityToDto)
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al listar facturas: {}", e.getMessage(), e);
            throw new RuntimeException("Ocurrió un error inesperado al listar las facturas.", e);
        }
    }
    @Override
    public List<FacturaResponseDTO> listarFacturasPorFechas(String fechaInicio, String fechaFin) {
        log.info("Iniciando proceso para listar facturas por rango de fechas: {} - {}", fechaInicio, fechaFin);
        try {
            // Validar que las fechas no sean nulas
            if (fechaInicio == null || fechaFin == null) {
                log.warn("Las fechas de inicio y fin son obligatorias.");
                throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
            }

            // Consultar el repositorio con las fechas
            List<Factura> facturas = facturaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

            // Validar si no hay facturas en el rango
            if (facturas.isEmpty()) {
                log.warn("No se encontraron facturas en el rango de fechas.");
                throw new ResourceNotFoundException("No se encontraron facturas en el rango de fechas.");
            }

            log.info("Se encontraron {} facturas en el rango de fechas.", facturas.size());
            return facturas.stream()
                    .map(facturaConverter::entityToDto)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            log.error("Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al listar facturas por fechas: {}", e.getMessage());
            throw new RuntimeException("Ocurrió un error inesperado al listar las facturas por fechas.", e);
        }
    }


}