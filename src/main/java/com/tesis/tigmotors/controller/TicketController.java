package com.tesis.tigmotors.controller;

import com.tesis.tigmotors.service.TicketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketServiceImpl ticketServiceImpl;


}