package com.tesis.tigmotors.repository;

import java.util.Optional;

import com.tesis.tigmotors.enums.Role;
import com.tesis.tigmotors.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.tesis.tigmotors.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

/*
 * Autor: Joseph Yépez
 * Esta clase nos sirve para realizar operaciones de lectura y escritura en la base de datos.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
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
}
