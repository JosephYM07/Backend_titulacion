package com.tesis.tigmotors.service.interfaces;

import org.springframework.http.ResponseEntity;

public interface PasswordResetService {

    /**
     * Envía un token de restablecimiento de contraseña al correo del usuario.
     *
     * @param email Correo del usuario.
     * @return Mensaje indicando que el token fue enviado.
     */
    String sendResetToken(String email);

    /**
     * Restablece la contraseña del usuario utilizando un token.
     *
     * @param token       Token de restablecimiento de contraseña.
     * @param newPassword Nueva contraseña del usuario.
     * @return Mensaje indicando que la contraseña fue actualizada.
     */
    String resetPassword(String token, String newPassword);

    /**
     * Cambia la contraseña de un usuario autenticado.
     *
     * @param username        Nombre de usuario autenticado.
     * @param currentPassword Contraseña actual ingresada por el usuario.
     * @param newPassword     Nueva contraseña que el usuario desea establecer.
     * @return Respuesta indicando éxito o error en el cambio de contraseña.
     */
    ResponseEntity<?> changePasswordAuthenticated(String username, String currentPassword, String newPassword);

}