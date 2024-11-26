package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.models.PasswordResetToken;
import com.tesis.tigmotors.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByUserId(Integer userId);
}