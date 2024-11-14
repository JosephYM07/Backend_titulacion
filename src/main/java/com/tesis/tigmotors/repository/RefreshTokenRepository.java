package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.models.RefreshToken;
import com.tesis.tigmotors.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user); // Método para encontrar por usuario
    void deleteByUser(User user);
    void deleteByExpiryDateBefore(Instant now); // Para limpieza de tokens expirados
}
