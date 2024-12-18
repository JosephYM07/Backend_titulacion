package com.tesis.tigmotors.repository;

import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    /**
     * Filtrar tickets por rango de fechas.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @return Lista de tickets dentro del rango de fechas.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 } }")
    List<Ticket> findByFechaCreacionBetween(String fechaInicio, String fechaFin);

    /**
     * Filtrar tickets por rango de fechas y usuario.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param username    Nombre del usuario.
     * @return Lista de tickets que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'username': ?2 }")
    List<Ticket> findByFechaCreacionAndUsername(String fechaInicio, String fechaFin, String username);

    /**
     * Filtrar tickets por rango de fechas y estado.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param estado      Estado del ticket.
     * @return Lista de tickets que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'estado': ?2 }")
    List<Ticket> findByFechaCreacionAndEstado(String fechaInicio, String fechaFin, String estado);

    /**
     * Filtrar tickets por rango de fechas, estado y prioridad.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param estado      Estado del ticket.
     * @param prioridad   Prioridad del ticket (ALTA, MEDIA, BAJA).
     * @return Lista de tickets que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'estado': ?2, 'prioridad': ?3 }")
    List<Ticket> findByFechaCreacionAndEstadoAndPrioridad(String fechaInicio, String fechaFin, String estado, String prioridad);

    /**
     * Filtrar tickets por rango de fechas, usuario y estado.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param username    Nombre del usuario.
     * @param estado      Estado del ticket.
     * @return Lista de tickets que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'username': ?2, 'estado': ?3 }")
    List<Ticket> findByFechaCreacionAndUsernameAndEstado(String fechaInicio, String fechaFin, String username, String estado);

    /**
     * Filtrar tickets por rango de fechas, usuario, estado y prioridad.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param username    Nombre del usuario.
     * @param estado      Estado del ticket.
     * @param prioridad   Prioridad del ticket (ALTA, MEDIA, BAJA).
     * @return Lista de tickets que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'username': ?2, 'estado': ?3, 'prioridad': ?4 }")
    List<Ticket> findByFechaCreacionAndUsernameAndEstadoAndPrioridad(
            String fechaInicio, String fechaFin, String username, String estado, String prioridad);

    /**
     * Filtrar tickets por rango de fechas y prioridad.
     *
     * @param fechaInicio Fecha de inicio (inclusive).
     * @param fechaFin    Fecha de fin (inclusive).
     * @param prioridad   Prioridad del ticket (ALTA, MEDIA, BAJA).
     * @return Lista de tickets que coincidan con los filtros.
     */
    @Query("{ 'fechaCreacion': { $gte: ?0, $lte: ?1 }, 'prioridad': ?2 }")
    List<Ticket> findByFechaCreacionAndPrioridad(String fechaInicio, String fechaFin, String prioridad);

}
