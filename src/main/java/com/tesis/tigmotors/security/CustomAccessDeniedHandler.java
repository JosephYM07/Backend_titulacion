package com.tesis.tigmotors.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesis.tigmotors.dto.Response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                "Acceso denegado. No tienes permisos para acceder a este recurso.");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
