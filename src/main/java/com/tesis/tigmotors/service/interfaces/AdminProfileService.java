package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Response.AdminProfileResponse;

public interface AdminProfileService {
    /**
     * Método para obtener el perfil de un administrador por su username.
     *
     * @param username Nombre de usuario del administrador.
     * @return Respuesta con la información del perfil.
     */
    AdminProfileResponse getProfile(String username);
}