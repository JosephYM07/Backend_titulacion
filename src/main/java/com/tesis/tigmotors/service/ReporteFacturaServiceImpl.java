package com.tesis.tigmotors.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.tesis.tigmotors.models.Factura;
import com.tesis.tigmotors.repository.FacturaRepository;
import com.tesis.tigmotors.service.interfaces.ReporteFacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteFacturaServiceImpl implements ReporteFacturaService {

    private final FacturaRepository facturaRepository;

    @Override
    @Transactional
    public ByteArrayOutputStream generarReporteFacturasPendientes(String username) {
        List<Factura> facturas = facturaRepository.findByPagoAndUsername("PENDIENTE_PAGO", username);
        if (facturas.isEmpty()) {
            throw new RuntimeException("No hay facturas pendientes para el usuario: " + username);
        }
        return crearPDF(facturas, "Reporte de Facturas Pendientes para: " + username);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generarReporteGeneralFacturasPendientes() {
        List<Factura> facturas = facturaRepository.findByPago("PENDIENTE_PAGO");
        if (facturas.isEmpty()) {
            throw new RuntimeException("No hay facturas pendientes de pago.");
        }
        return crearPDF(facturas, "Reporte General de Facturas Pendientes");
    }

    @Override
    @Transactional
    public ByteArrayOutputStream imprimirFactura(String facturaId) {
        Factura factura = facturaRepository.findByFacturaId(facturaId);
        if (factura == null) {
            throw new RuntimeException("No se encontró la factura con ID: " + facturaId);
        }
        return crearPDF(List.of(factura), "Factura Detallada - ID: " + facturaId);
    }

    private ByteArrayOutputStream crearPDF(List<Factura> facturas, String tituloReporte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDoc);

            PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Título del reporte
            document.add(new Paragraph(tituloReporte)
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Crear tabla
            Table table = new Table(new float[]{1, 2, 2, 1, 1, 1});
            table.addCell(new Cell().add(new Paragraph("ID Factura").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph("Usuario").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph("Descripción").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph("Monto").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph("Fecha").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph("Estado").setFont(boldFont)));

            for (Factura factura : facturas) {
                table.addCell(new Cell().add(new Paragraph(factura.getFacturaId()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(factura.getUsername()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(factura.getDescripcionTrabajo()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", factura.getCotizacion())).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(factura.getFechaCreacion()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(factura.getPago()).setFont(regularFont)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
        return out;
    }
}
