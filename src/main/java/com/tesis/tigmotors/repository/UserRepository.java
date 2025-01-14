package com.tesis.tigmotors.repository;

import java.util.Optional;

import com.tesis.tigmotors.dto.Response.UserBasicInfoResponseDTO;
import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Autor: Joseph Yépez
 * Esta clase nos sirve para realizar operaciones de lectura y escritura en la base de datos.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    // Guardar el usuario
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_sequence (user_id, created_at) VALUES (:userId, NOW())", nativeQuery = true)
    void guardarSecuencia(@Param("userId") Integer userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    long countByPermisoAndRole(boolean permiso, Role role);

    List<User> findByPermiso(boolean permiso);

    @Query(value = "SELECT * FROM user WHERE username = :username AND role = 'USER'", nativeQuery = true)
    Optional<User> buscarPorUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM user WHERE email = :email AND role = 'USER'", nativeQuery = true)
    Optional<User> buscarPorEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM user WHERE id = :id AND role = 'USER'", nativeQuery = true)
    Optional<User> buscarPorId(@Param("id") int id);
    /**
     * Encuentra usuarios por permiso y rol.
     *
     * @param permiso true si los usuarios están aprobados.
     * @param role el rol del usuario (por ejemplo, USER).
     * @return Lista de usuarios que coinciden con los criterios.
     */
    List<User> findByPermisoAndRole(boolean permiso, Role role);


}
