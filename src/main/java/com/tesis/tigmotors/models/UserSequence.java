package com.tesis.tigmotors.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_sequence")
public class UserSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequenceId; // ID de la secuencia

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user; // Relación con el usuario

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}