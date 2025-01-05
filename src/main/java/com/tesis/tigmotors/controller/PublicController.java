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
        String mensaje = " El sistema está funcionando correctamente! "
                + "Consulta la documentación en: https://documenter.getpostman.com/view/34383022/2sAYHwKQaJ";
        return ResponseEntity.ok(mensaje);
    }
}
