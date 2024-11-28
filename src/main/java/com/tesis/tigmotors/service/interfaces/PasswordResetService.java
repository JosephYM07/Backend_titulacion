package com.tesis.tigmotors.service.interfaces;

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
}