package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByEstado(TicketEstado estado);
    List<Ticket> findByUsername(String username);

    /**
     * Busca los tickets asociados a un usuario específico y filtrados por prioridad.
     *
     * @param username El nombre del usuario.
     * @param prioridad La prioridad del ticket (ALTA, MEDIO, BAJA).
     * @return Una lista de tickets que cumplen con los criterios de búsqueda.
     */
    List<Ticket> findByUsernameAndPrioridad(String username, String prioridad);

    /**
     * Busca los tickets asociados a un usuario específico y filtrados por estado.
     *
     * @param username El nombre del usuario.
     * @param estado El estado del ticket.
     * @return Una lista de tickets que cumplen con los criterios de búsqueda.
     */
    List<Ticket> findByUsernameAndEstado(String username, String estado);

}