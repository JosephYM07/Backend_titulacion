package com.tesis.tigmotors.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(String message) {
        super(message);
    }

}
