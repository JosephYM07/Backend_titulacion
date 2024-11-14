package com.tesis.tigmotors.repository;

import java.util.Optional;

import com.tesis.tigmotors.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

/*
* Autor: Joseph Yépez
 * Esta clase nos sirve para realizar operaciones de lectura y escritura en la base de datos.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
