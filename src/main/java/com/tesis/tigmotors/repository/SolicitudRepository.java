package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.models.Solicitud;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends MongoRepository<Solicitud, String> {

    long countByEstado(String estado);

    // Método para obtener todas las solicitudes de un usuario específico
    List<Solicitud> findByUsername(String username, Sort sort);

    // Método para buscar solicitudes por estado
    List<Solicitud> findByEstado(String estado, Sort sort);

    // Método para buscar solicitudes por Username y Prioridad
    List<Solicitud> findByUsernameAndPrioridad(String username, String prioridad, Sort sort);

    // Método para buscar solicitudes por prioridad
    List<Solicitud> findByPrioridad(String prioridad, Sort sort);

    // Método para buscar solicitudes por estado y usuario
    List<Solicitud> findByUsernameAndEstado(String username, String estado, Sort sort);


}