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

                // Validación para asegurarnos de que el token pertenece al usuario autenticado
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                if (jwtServiceImpl.isTokenValid(token, userDetails) && currentAuth == null) {
                    logger.info("Token válido para el usuario: {}, asignando contexto de seguridad.", usernameFromToken);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // Log para verificar las autoridades que se están asignando
                    userDetails.getAuthorities().forEach(authority ->
                            logger.info("Rol asignado al usuario {}: {}", usernameFromToken, authority.getAuthority()));

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else if (currentAuth != null && !currentAuth.getName().equals(usernameFromToken)) {
                    // Si el usuario autenticado actual no coincide con el token, devuelve error
                    logger.warn("Usuario autenticado actual ({}) no coincide con el usuario del token ({}).", currentAuth.getName(), usernameFromToken);
                    /*response.setStatus(HttpServletResponse.SC_FORBIDDEN);*/
                    /*response.setContentType("application/json");
                    response.getWriter().write("{\"Estado\":\"Error\", \"Mensaje\":\"Token no autorizado para el usuario actual.\"}");*/
                    /*response.setStatus(HttpServletResponse.SC_FORBIDDEN);*/
                    redirigirLogin(response, HttpServletResponse.SC_FORBIDDEN, "Token no autorizado para el usuario actual.");
                    return;
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Manejo específico para token expirado
                logger.warn("Token expirado detectado: {}", e.getMessage());
                redirigirLogin(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado. Por favor, inicie sesión nuevamente.");
                return;
            } catch (Exception e) {
                logger.error("Error procesando el token JWT: ", e);
                /*response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);*/
                /*response.setContentType("application/json");
                response.getWriter().write("{\"Estado\":\"Error\", \"Mensaje\":\"Token Invalido o Caducado\"}");*/
                /*response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);*/
                redirigirLogin(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado. Por favor, inicie sesión nuevamente.");
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

    /**
     * Método reutilizable para manejar redirecciones al login global.
     *
     * @param response      Respuesta HTTP.
     * @param status        Código de estado HTTP.
     * @param mensaje       Mensaje adicional para los logs.
     */
    private void redirigirLogin(HttpServletResponse response, int status, String mensaje) throws IOException {
        logger.warn("Redirigiendo al login global: {}", mensaje);
        response.setStatus(status);
        response.sendRedirect("/api/v1/login-global");
    }
}
