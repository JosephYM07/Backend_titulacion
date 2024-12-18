package com.tesis.tigmotors.dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tesis.tigmotors.dto.Response.FacturaDetalleResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacturaResponseDTO {
    private List<FacturaDetalleResponseDTO> facturas; // Lista de facturas individuales
    private Integer numeroFacturas;            // Total de facturas (Integer permite nulos)
    private Double total;          // Suma total de cotizaciones
}
