package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.models.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.colors.DeviceRgb;


import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {

    List<Factura> findByPagoAndUsername(String pago, String username);

    List<Factura> findByPago(String pago);

    Factura findByFacturaId(String facturaId);
}