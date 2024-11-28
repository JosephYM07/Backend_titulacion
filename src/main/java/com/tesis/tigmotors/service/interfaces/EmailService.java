package com.tesis.tigmotors.service.interfaces;

public interface EmailService {

    /**
     * Envía un correo electrónico al destinatario especificado.
     *
     * @param to      Dirección de correo electrónico del destinatario.
     * @param subject Asunto del correo.
     * @param text    Contenido del correo (puede ser HTML).
     */
    void sendEmail(String to, String subject, String text);
}
