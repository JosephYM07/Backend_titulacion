package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.dto.Response.Solicitud;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends MongoRepository<Solicitud, String> {

    // Método para obtener todas las solicitudes de un usuario específico
    List<Solicitud> findByUsername(String username);

    // Método para buscar solicitudes por estado
    List<Solicitud> findByEstado(String estado);

    // Método para buscar solicitudes por Username y Prioridad
    List<Solicitud> findByUsernameAndPrioridad(String username, String prioridad);

    // Método para buscar solicitudes por prioridad
    List<Solicitud> findByPrioridad(String prioridad);

    // Método para buscar solicitudes por estado y usuario
    List<Solicitud> findByUsernameAndEstado(String username, String estado);

}