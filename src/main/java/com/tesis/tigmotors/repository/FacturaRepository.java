package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.models.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.colors.DeviceRgb;


import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
    /**
     * Filtrar facturas por rango de fechas.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @return Lista de facturas dentro del rango de fechas.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 } }")
    List<Factura> findByFechaCreacionBetween(String fechaInicio, String fechaFin);

    /**
     * Filtrar facturas por rango de fechas y usuario.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param username    Nombre del usuario.
     * @return Lista de facturas que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'username': ?2 }")
    List<Factura> findByFechaCreacionAndUsername(String fechaInicio, String fechaFin, String username);

    /**
     * Filtrar facturas por rango de fechas, usuario y estado de pago.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param username    Nombre del usuario.
     * @param estadoPago  Estado del pago.
     * @return Lista de facturas que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'username': ?2, 'pago': ?3 }")
    List<Factura> findByFechaCreacionAndUsernameAndEstadoPago(String fechaInicio, String fechaFin, String username, String estadoPago);

}