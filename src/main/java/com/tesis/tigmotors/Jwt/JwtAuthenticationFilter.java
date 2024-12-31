package com.tesis.tigmotors.Jwt;

import com.tesis.tigmotors.service.JwtServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtServiceImpl jwtServiceImpl;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        if (token != null) {
            try {
                String usernameFromToken = jwtServiceImpl.getUsernameFromToken(token);
                logger.info("Extrayendo nombre de usuario del token: {}", usernameFromToken);

                UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);
                logger.info("Cargando UserDetails para el usuario: {}", usernameFromToken);

                // Validar el token
                if (jwtServiceImpl.isTokenValid(token, userDetails)) {
                    // Verificar que no haya conflicto con el usuario autenticado actual
                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                    if (currentAuth == null || currentAuth.getName().equals(usernameFromToken)) {
                        logger.info("Token válido. Asignando contexto de seguridad para el usuario: {}", usernameFromToken);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        logger.warn("Usuario autenticado actual ({}) no coincide con el token ({})", currentAuth.getName(), usernameFromToken);
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Token no autorizado para el usuario actual.");
                        return;
                    }
                } else {
                    logger.warn("Token inválido o caducado para el usuario: {}", usernameFromToken);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token Invalido o Caducado");
                    return;
                }
            } catch (Exception e) {
                logger.error("Error procesando el token JWT: {}", e.getMessage(), e);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token Invalido o Caducado");
                return;
            }
        }

        // Continuar con el siguiente filtro si todo está bien
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"Estado\":\"Error\", \"Mensaje\":\"" + message + "\"}");
    }
}

