package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByEstado(TicketEstado estado);
}