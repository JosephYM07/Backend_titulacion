package com.tesis.tigmotors.service;

import com.tesis.tigmotors.converters.TicketConverter;
import com.tesis.tigmotors.dto.Request.TicketDTO;
import com.tesis.tigmotors.enums.TicketEstado;
import com.tesis.tigmotors.models.Ticket;
import com.tesis.tigmotors.repository.TicketRepository;
import com.tesis.tigmotors.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;

    private final TicketConverter ticketConverter;

    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    public TicketDTO crearTicketAutomatico(TicketDTO ticketDTO, String username) {
        Ticket ticket = ticketConverter.dtoToEntity(ticketDTO);
        ticket.setUsername(username);
        Ticket ticketGuardado = ticketRepository.save(ticket);
        return ticketConverter.entityToDto(ticketGuardado);
    }

}
