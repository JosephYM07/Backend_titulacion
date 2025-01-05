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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
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
    @Transactional(readOnly = true)
    public List<FacturaDetalleResponseDTO> filtrarFacturasPorEstadoPagoUsuario(String username, String estadoPago) {
        log.info("Iniciando el filtrado de facturas para el usuario '{}' con estado de pago '{}'", username, estadoPago);

        try {
            // Buscar facturas asociadas al usuario y estado de pago
            List<Factura> facturas = facturaRepository.findByUsernameAndPago(username, estadoPago, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (facturas.isEmpty()) {
                log.warn("No se encontraron facturas para el usuario '{}' con estado de pago '{}'", username, estadoPago);
            } else {
                log.info("Se encontraron {} facturas para el usuario '{}' con estado de pago '{}'", facturas.size(), username, estadoPago);
            }

            // Convertir las facturas a DTO detallado y retornar
            return facturas.stream()
                    .map(facturaConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error filtrando las facturas para el usuario '{}' con estado de pago '{}': {}", username, estadoPago, e.getMessage(), e);
            throw new RuntimeException("Error filtrando las facturas del usuario por estado de pago.", e);
        }
    }


    /**
     * Obtiene el historial de facturas asociadas a un usuario específico (perfil USER).
     *
     * @param username Nombre del usuario cuyas facturas se desean obtener.
     * @return Una lista de facturas en formato DTO detallado asociadas al usuario.
     * @throws RuntimeException Si ocurre un error inesperado durante el proceso.
     */
    @Override
    @Transactional(readOnly = true)
    public List<FacturaDetalleResponseDTO> obtenerHistorialFacturasPorUsuario(String username) {
        try {
            List<Factura> facturas = facturaRepository.findByUsername(username, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
            if (facturas.isEmpty()) {
                log.warn("No se encontraron facturas para el usuario '{}'", username);
            }
            return facturas.stream()
                    .map(facturaConverter::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo el historial de facturas para el usuario '{}': {}", username, e.getMessage(), e);
            throw new RuntimeException("Error obteniendo el historial de facturas del usuario", e);
        }
    }

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

            List<Factura> facturas = facturaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));

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

    // Validar fechas con manejo de errores
    private void validarFechas(FacturaRequestDTO requestDTO) {
        String fechaRegex = "\\d{4}/\\d{2}/\\d{2}";

        if (requestDTO.getFechaInicio() == null || !requestDTO.getFechaInicio().matches(fechaRegex)) {
            log.warn("La fecha de inicio es obligatoria y debe estar en el formato 'yyyy/MM/dd'.");
            throw new IllegalArgumentException("La fecha de inicio es obligatoria y debe estar en el formato 'yyyy/MM/dd'.");
        }

        if (requestDTO.getFechaFin() == null || !requestDTO.getFechaFin().matches(fechaRegex)) {
            log.warn("La fecha de fin es obligatoria y debe estar en el formato 'yyyy/MM/dd'.");
            throw new IllegalArgumentException("La fecha de fin es obligatoria y debe estar en el formato 'yyyy/MM/dd'.");
        }

        if (requestDTO.getFechaInicio().compareTo(requestDTO.getFechaFin()) > 0) {
            log.warn("La fecha de inicio no puede ser posterior a la fecha de fin.");
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }


    // Obtener facturas con validación de filtros
    private List<Factura> obtenerFacturasPorFiltros(FacturaRequestDTO requestDTO) {
        try {
            if (requestDTO.getUsername() != null && requestDTO.getEstadoPago() != null) {
                validarEstadoPago(requestDTO.getEstadoPago());
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
                validarEstadoPago(requestDTO.getEstadoPago());
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
        } catch (IllegalArgumentException e) {
            log.error("Error en los filtros: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al filtrar facturas: {}", e.getMessage(), e);
            throw new RuntimeException("Ocurrió un error inesperado al filtrar facturas.", e);
        }
    }

    // Validar estado de pago
    private void validarEstadoPago(String estadoPago) {
        Set<String> estadosValidos = Set.of("PENDIENTE_PAGO", "VALOR_PAGADO");
        if (!estadosValidos.contains(estadoPago.toUpperCase())) {
            log.warn("El estado de pago '{}' no es válido. Estados válidos: {}", estadoPago, estadosValidos);
            throw new IllegalArgumentException("El estado de pago no es válido. Estados válidos: " + estadosValidos);
        }
    }

    @Override
    public FacturaDetalleResponseDTO actualizarEstadoPago(String facturaId) {
        log.info("Iniciando proceso para actualizar el estado de pago de la factura con ID '{}'.", facturaId);

        try {
            // Validar que el ID de la factura no sea nulo o vacío
            if (facturaId == null || facturaId.trim().isEmpty()) {
                log.error("El ID de la factura no puede ser nulo o vacío.");
                throw new IllegalArgumentException("El ID de la factura no puede ser nulo o vacío.");
            }

            // Validar que el formato del ID sea correcto
            if (!facturaId.toUpperCase().startsWith("FACTURA-")) {
                log.error("El ID de la factura '{}' no tiene el formato correcto. Debe comenzar con 'FACTURA-'.", facturaId);
                throw new IllegalArgumentException("El ID de la factura no es válido. Debe comenzar con 'FACTURA-' seguido de un número.");
            }

            // Convertir el ID a mayúsculas para garantizar consistencia
            String normalizedFacturaId = facturaId.toUpperCase();

            // Buscar la factura por ID normalizado
            Factura factura = facturaRepository.findById(normalizedFacturaId)
                    .orElseThrow(() -> {
                        log.error("Factura no encontrada con ID '{}'.", normalizedFacturaId);
                        return new ResourceNotFoundException("Factura no encontrada con ID: " + normalizedFacturaId);
                    });

            // Validar el estado actual del pago
            if (EstadoPago.VALOR_PAGADO.name().equals(factura.getPago())) {
                log.warn("El estado de la factura ya está en 'VALOR_PAGADO'. ID: {}", facturaId);
                throw new IllegalStateException("El estado de la factura ya está en 'VALOR_PAGADO'.");
            }

            if (!EstadoPago.PENDIENTE_PAGO.name().equals(factura.getPago())) {
                log.warn("El estado de la factura no es 'PENDIENTE_PAGO', no se puede actualizar. ID: {}", facturaId);
                throw new IllegalStateException("El estado de la factura no es 'PENDIENTE_PAGO', no se puede actualizar.");
            }

            // Actualizar el estado de pago
            factura.setPago(EstadoPago.VALOR_PAGADO.name());
            Factura facturaActualizada = facturaRepository.save(factura);

            log.info("Estado de pago actualizado a 'VALOR_PAGADO' para la factura con ID '{}'.", facturaId);

            // Convertir a DTO y retornar
            return facturaConverter.entityToDto(facturaActualizada);

        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            log.error("Error controlado al actualizar el estado de pago: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar el estado de pago para la factura con ID '{}': {}", facturaId, e.getMessage(), e);
            throw new RuntimeException("Error interno al actualizar el estado de pago.", e);
        }
    }


}