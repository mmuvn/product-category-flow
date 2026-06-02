package swp391.group6.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swp391.group6.model.Ticket;
import swp391.group6.dto.TicketRequest;
import swp391.group6.service.TicketService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService){
        this.ticketService = ticketService;
    }

    // Customer creates a ticket
    @PostMapping
    public ResponseEntity<Ticket>createTicket(@RequestBody TicketRequest request) {
        try {
            Ticket ticket = ticketService.createTicket(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Agent & Customer views their own tickets
    @GetMapping("/{userId}")
    public ResponseEntity<List<Ticket>> getAuthorizedTickets(@PathVariable long userId) {
        try {
            List<Ticket> tickets = ticketService.getAuthorizedTickets(userId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // View specific ticket details
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable long id) {
        try {
            Optional<Ticket> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent()) {
                return ResponseEntity.ok(ticket.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Update ticket status
    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(
            @PathVariable long id,
            @RequestParam String newState,
            @RequestParam(required = false) Long agentId) {
        try {
            Ticket updatedTicket = ticketService.updateTicketStatus(id, newState, agentId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}