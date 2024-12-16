package com.tesis.tigmotors.service.interfaces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ReporteFacturaService {
    ByteArrayOutputStream generarReporteFacturasPendientes(String username) throws IOException;

    ByteArrayOutputStream generarReporteGeneralFacturasPendientes() throws IOException;

    ByteArrayOutputStream imprimirFactura(String facturaId) throws IOException;
}