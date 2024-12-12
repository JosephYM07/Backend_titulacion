package com.tesis.tigmotors.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.function.Function;

public interface JwtService {

    /**
     * Genera un token de acceso para un usuario.
     * @param user Usuario del cual se genera el token.
     * @return Token de acceso.
     */
    String generateAccessToken(UserDetails user);

    /**
     * Genera un token de actualización para un usuario.
     * @param user Usuario del cual se genera el token.
     * @return Token de actualización.
     */
    String generateRefreshToken(UserDetails user);

    /**
     * Obtiene el nombre de usuario a partir del token JWT.
     * @param token Token JWT.
     * @return Username contenido en el token.
     */
    String getUsernameFromToken(String token);

    /**
     * Verifica si un token es válido para un usuario.
     * @param token Token JWT.
     * @param userDetails Detalles del usuario.
     * @return true si el token es válido, false de lo contrario.
     */
    boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * Obtiene un valor (claim) específico del token JWT.
     * @param token Token JWT.
     * @param claimsResolver Resolución de los claims.
     * @return Valor del claim.
     */
    <T> T getClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver);
}