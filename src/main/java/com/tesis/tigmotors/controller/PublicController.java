package com.tesis.tigmotors.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        String saludo = generarSaludo();
        String mensaje = saludo + " El sistema está funcionando correctamente! "
                + "Consulta la documentación en: https://documenter.getpostman.com/view/34383022/2sAYHwKQaJ";
        return ResponseEntity.ok(mensaje);
    }

    private String generarSaludo() {
        LocalTime ahora = LocalTime.now();
        if (ahora.isBefore(LocalTime.NOON)) {
            return "¡Buenos días!";
        } else if (ahora.isBefore(LocalTime.of(18, 0))) {
            return "¡Buenas tardes!";
        } else {
            return "¡Buenas noches!";
        }
    }
}
