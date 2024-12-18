package com.tesis.tigmotors.service;

import com.tesis.tigmotors.Exceptions.ResourceNotFoundException;
import com.tesis.tigmotors.converters.FacturaConverter;
import com.tesis.tigmotors.dto.Request.FacturaRequestDTO;
import com.tesis.tigmotors.dto.Response.FacturaDetalleResponseDTO;
import com.tesis.tigmotors.dto.Response.FacturaResponseDTO;
import com.tesis.tigmotors.enums.EstadoPago;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.FacturaRepository;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

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
    public List<FacturaDetalleResponseDTO> listarTodasLasFacturas() {
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
    public FacturaResponseDTO listarFacturasConFiltros(FacturaRequestDTO requestDTO) {
        log.info("Iniciando proceso para listar facturas con filtros: {}", requestDTO);

        try {
            // Validar fechas
            validarFechas(requestDTO);

            // Obtener las facturas filtradas
            List<Factura> facturas = obtenerFacturasPorFiltros(requestDTO);

            // Determinar si el filtro estadoPago está presente
            boolean filtroEstadoPagoAplicado = requestDTO.getEstadoPago() != null;

            // Si estadoPago está presente, filtra por estado
            List<Factura> facturasFiltradas = facturas;
            if (filtroEstadoPagoAplicado) {
                String estadoPago = requestDTO.getEstadoPago();

                // Validar estado de pago
                if (!EstadoPago.isPagoEstado(estadoPago)) {
                    log.warn("El estado de pago '{}' no es válido.", estadoPago);
                    throw new IllegalArgumentException("El estado de pago no es válido.");
                }

                facturasFiltradas = facturas.stream()
                        .filter(f -> estadoPago.equalsIgnoreCase(f.getPago()))
                        .collect(Collectors.toList());
            }

            // Convertir facturas a DTO
            List<FacturaDetalleResponseDTO> facturasDTO = facturasFiltradas.stream()
                    .map(facturaConverter::entityToDto)
                    .collect(Collectors.toList());

            // Construir respuesta condicional
            FacturaResponseDTO.FacturaResponseDTOBuilder responseBuilder = FacturaResponseDTO.builder()
                    .facturas(facturasDTO);

            // Solo incluir estos campos si hay filtro de estadoPago
            if (filtroEstadoPagoAplicado) {
                double totalCotizacion = facturasFiltradas.stream()
                        .mapToDouble(Factura::getCotizacion)
                        .sum();

                responseBuilder
                        .numeroFacturas(facturasFiltradas.size())
                        .total(totalCotizacion);
            }

            return responseBuilder.build();

        } catch (Exception e) {
            log.error("Error inesperado al listar facturas con filtros: {}", e.getMessage(), e);
            throw new RuntimeException("Ocurrió un error inesperado al listar las facturas con filtros.", e);
        }
    }


    // Métodos auxiliares
    private void validarFechas(FacturaRequestDTO requestDTO) {
        if (requestDTO.getFechaInicio() == null || requestDTO.getFechaFin() == null) {
            log.warn("Las fechas de inicio y fin son obligatorias.");
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
    }

    private List<Factura> obtenerFacturasPorFiltros(FacturaRequestDTO requestDTO) {
        if (requestDTO.getUsername() != null && requestDTO.getEstadoPago() != null) {
            return facturaRepository.findByFechaCreacionAndUsernameAndEstadoPago(
                    requestDTO.getFechaInicio(),
                    requestDTO.getFechaFin(),
                    requestDTO.getUsername(),
                    requestDTO.getEstadoPago()
            );
        } else if (requestDTO.getUsername() != null) {
            return facturaRepository.findByFechaCreacionAndUsername(
                    requestDTO.getFechaInicio(),
                    requestDTO.getFechaFin(),
                    requestDTO.getUsername()
            );
        } else if (requestDTO.getEstadoPago() != null) {
            return facturaRepository.findByFechaCreacionAndEstadoPago(
                    requestDTO.getFechaInicio(),
                    requestDTO.getFechaFin(),
                    requestDTO.getEstadoPago()
            );
        } else {
            return facturaRepository.findByFechaCreacionBetween(
                    requestDTO.getFechaInicio(),
                    requestDTO.getFechaFin()
            );
        }
    }


}