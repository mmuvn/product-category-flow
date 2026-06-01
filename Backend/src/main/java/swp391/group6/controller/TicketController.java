package swp391.group6.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swp391.group6.dto.TicketResponse;
import swp391.group6.dto.TicketDTO;
import swp391.group6.dto.TicketRequest;
import swp391.group6.service.TicketService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // Customer creates a ticket
    @PostMapping
    public ResponseEntity<TicketResponse<TicketDTO>> createTicket(@RequestBody TicketRequest request) {
        try {
            TicketDTO ticket = ticketService.createTicket(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new TicketResponse<>(201, "Ticket created successfully", ticket));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TicketResponse<>(500, e.getMessage(), null));
        }
    }

    // Customer views their own tickets
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<TicketResponse<List<TicketDTO>>> getCustomerTickets(@PathVariable long customerId) {
        try {
            List<TicketDTO> tickets = ticketService.getCustomerTickets(customerId);
            return ResponseEntity.ok(new TicketResponse<>(200, "Customer tickets retrieved", tickets));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TicketResponse<>(500, "Error retrieving tickets", null));
        }
    }

    // Agent views all pending tickets
    @GetMapping("/queue")
    public ResponseEntity<TicketResponse<List<TicketDTO>>> getTicketQueue() {
        try {
            List<TicketDTO> queue = ticketService.getPendingQueue();
            return ResponseEntity.ok(new TicketResponse<>(200, "Ticket queue retrieved", queue));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TicketResponse<>(500, "Error retrieving queue", null));
        }
    }

    // View specific ticket details
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse<TicketDTO>> getTicketById(@PathVariable long id) {
        try {
            Optional<TicketDTO> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent()) {
                return ResponseEntity.ok(new TicketResponse<>(200, "Ticket retrieved", ticket.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new TicketResponse<>(404, "Ticket not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TicketResponse<>(500, "Error retrieving ticket", null));
        }
    }

    // Update ticket status
    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse<TicketDTO>> updateTicketStatus(
            @PathVariable long id,
            @RequestParam String newState,
            @RequestParam(required = false) Long agentId) {
        try {
            TicketDTO updatedTicket = ticketService.updateTicketStatus(id, newState, agentId);
            return ResponseEntity.ok(new TicketResponse<>(200, "Ticket status updated", updatedTicket));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TicketResponse<>(500, e.getMessage(), null));
        }
    }
}