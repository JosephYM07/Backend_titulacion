package com.tesis.tigmotors.repository;

import java.util.Optional;

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
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    long countByPermiso(boolean permiso);

    List<User> findByPermiso(boolean permiso);

    //update
    @Modifying
    @Transactional
    @Query("UPDATE User u SET " +
            "u.business_name = COALESCE(:businessName, u.business_name), " +
            "u.email = COALESCE(:email, u.email), " +
            "u.phone_number = COALESCE(:phoneNumber, u.phone_number) " +
            "WHERE u.username = :username")
    int updateAdminProfile(
            @Param("username") String username,
            @Param("businessName") String businessName,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber);

}
