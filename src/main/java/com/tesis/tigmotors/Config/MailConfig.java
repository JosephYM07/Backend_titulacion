package com.tesis.tigmotors.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    /**
     * Configura el servicio de envío de correos electrónicos (JavaMailSender).
     *
     * - Define las propiedades necesarias para conectarse al servidor SMTP.
     * - Los detalles como host, puerto, usuario y contraseña son inyectados desde las propiedades del entorno.
     * - Activa autenticación SMTP y encriptación mediante STARTTLS.
     *
     * Este método asegura que la aplicación pueda enviar correos electrónicos utilizando
     * un servidor SMTP configurado de manera segura.
     *
     * Propiedades configuradas:
     * - `mail.transport.protocol`: Protocolo SMTP.
     * - `mail.smtp.auth`: Habilitación de autenticación.
     * - `mail.smtp.starttls.enable`: Uso de STARTTLS para seguridad.
     * - `mail.debug`: Activación del modo de depuración para análisis.
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailHost);
        javaMailSender.setPort(mailPort);
        javaMailSender.setUsername(mailUsername);
        javaMailSender.setPassword(mailPassword);

        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return javaMailSender;
    }
}
