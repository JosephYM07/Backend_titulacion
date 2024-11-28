package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.UserUpdateRequestDTO;
import com.tesis.tigmotors.dto.Response.UserResponseUser;

public interface UserServiceUpdate {

    /**
     * Actualiza los datos de un usuario.
     *
     * @param updateRequest Objeto con los datos a actualizar.
     * @return UserResponseUser con los datos actualizados.
     */
    UserResponseUser updateUser(UserUpdateRequestDTO updateRequest);
}