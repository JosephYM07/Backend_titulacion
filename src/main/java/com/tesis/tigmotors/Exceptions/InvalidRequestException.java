package com.tesis.tigmotors.Exceptions;

/**
 * Excepción personalizada para manejar solicitudes inválidas.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
