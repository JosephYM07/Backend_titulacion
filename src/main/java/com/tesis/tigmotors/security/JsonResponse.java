package com.tesis.tigmotors.security;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public class JsonResponse {

    public static void sendJsonResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"statusCode\": " + status.value() + ", \"message\": \"" + message + "\"}");
    }
}