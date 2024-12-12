package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.PendingUserDTO;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AdminVerificationUserService {


    List<String> obtenerUsernamesAprobados(Authentication authentication);

    /**
     * Obtiene el estado de los usuarios (pendientes y aprobados).
     *
     * @return ResponseEntity con el conteo de usuarios pendientes y aprobados.
     */
    ResponseEntity<Object> getUsersStatus();

    /**
     * Obtiene una lista de usuarios aprobados con el rol USER.
     *
     * @return Lista de PendingUserDTO que representan a los usuarios aprobados.
     */
    List<PendingUserDTO> obtenerUsuariosAprobados(Authentication authentication);


    /**
     * Obtiene una lista de usuarios pendientes de aprobación.
     *
     * @return ResponseEntity con la lista de usuarios pendientes o un error.
     */
    ResponseEntity<Object> getPendingUsers();

    /**
     * Aprueba un usuario dado su ID.
     *
     * @param userId ID del usuario a aprobar.
     * @return ResponseEntity con el resultado de la operación.
     */
    ResponseEntity<Object> approveUser(Integer userId);

    /**
     * Elimina un usuario dado su ID.
     *
     * @param userId ID del usuario a eliminar.
     * @return ResponseEntity con el resultado de la operación.
     */
    ResponseEntity<Object> deleteUserById(Integer userId);
}