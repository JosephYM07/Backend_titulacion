package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.UserSelfUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.UserBasicInfoResponseDTO;
import com.tesis.tigmotors.dto.Request.UserUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.UserResponseUser;

public interface UserServiceUpdate {

    /**
     * Actualiza los datos de un usuario.
     *
     * @param updateRequest Objeto con los datos a actualizar.
     * @return UserResponseUser con los datos actualizados.
     */
    UserResponseUser updateUser(UserUpdateRequestDTO updateRequest, String username);

    /**
     * Actualiza la información del usuario autenticado.
     *
     * @param updateRequest Objeto con los datos que se desean actualizar.
     * @param usernameFromToken Username extraído del token JWT del usuario autenticado.
     * @return UserResponseUser con los datos actualizados.
     */
    UserBasicInfoResponseDTO updateMyProfile(UserSelfUpdateRequestDTO updateRequest, String usernameFromToken);
    /**
     * Obtener información básica del usuario por username.
     * @param username Username del usuario.
     * @return UserBasicInfoResponseDTO con los datos básicos del usuario.
     */
    UserBasicInfoResponseDTO getUserProfile(String username);
}