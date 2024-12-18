package com.tesis.tigmotors.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tesis.tigmotors.dto.Request.FacturaRequestDTO;
import com.tesis.tigmotors.dto.Response.FacturaResponseDTO;
import com.tesis.tigmotors.service.interfaces.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.servlet.http.HttpServletResponse;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaPdfService {

    private final FacturaService facturaService;
    private final TemplateEngine templateEngine;

    public void generarPdf(HttpServletResponse response, FacturaRequestDTO filtros) {
        try {
            // Obtener las facturas filtradas
            FacturaResponseDTO responseDTO = facturaService.listarFacturasConFiltros(filtros);

            // Configurar el contexto de Thymeleaf
            Context context = new Context();
            context.setVariable("facturas", responseDTO.getFacturas());
            context.setVariable("totalCotizacion", responseDTO.getTotal());
            context.setVariable("numeroFacturas", responseDTO.getNumeroFacturas());
            context.setVariable("fechaActual", LocalDate.now());

            // Generar HTML desde la plantilla
            String htmlContent = templateEngine.process("facturas", context);

            // Configurar la respuesta HTTP
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=facturas.pdf");

            // Convertir HTML a PDF
            try (OutputStream os = response.getOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(htmlContent, null);
                builder.toStream(os);
                builder.run();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }
}
