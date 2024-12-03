package com.tesis.tigmotors.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

/**
 * DTO para encapsular errores en las respuestas del servidor.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private int statusCode;
    private String message;
}
