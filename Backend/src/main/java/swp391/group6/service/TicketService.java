package swp391.group6.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swp391.group6.dto.TicketDTO;
import swp391.group6.dto.TicketRequestDTO;
import swp391.group6.model.Priority;
import swp391.group6.model.Ticket;
import swp391.group6.model.TicketState;
import swp391.group6.model.User;
import swp391.group6.repository.TicketRepository;
import swp391.group6.repository.UserRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // UC 16: Customer creates a ticket
    public TicketDTO createTicket(TicketRequestDTO request) {
        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Customer responsible for Ticket not found"));
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDetail(request.getDetail());
        ticket.setTicketType(request.getTicketType());
        ticket.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
        ticket.setCreator(creator);
        ticket.setTicketState(TicketState.CREATED);
        ticket.setTimeCreated(new Timestamp(System.currentTimeMillis()));

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
    }

    // UC 16: Customer views their own tickets
    public List<TicketDTO> getCustomerTickets(long customerId) {
        return ticketRepository.findByCreatorId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // UC 12: Agent browses the queue of pending tickets
    public List<TicketDTO> getPendingQueue() {
        return ticketRepository.findByTicketStateNot(TicketState.RESOLVED).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // UC 12 & 16: Update ticket status
    public TicketDTO updateTicketStatus(long ticketId, String newStateStr, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketState newState = TicketState.valueOf(newStateStr.toUpperCase());
        ticket.setTicketState(newState);

        if (newState == TicketState.RESOLVED || newState == TicketState.DONE) {
            ticket.setTimeResolved(new Timestamp(System.currentTimeMillis()));
        }

        // If a Customer Support Agent is taking the ticket, assign them
        if (agentId != null) {
            User agent = userRepository.findById(agentId).orElse(null);
            ticket.setAssignee(agent);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return convertToDTO(updatedTicket);
    }

    public Optional<TicketDTO> getTicketById(long id) {
         return ticketRepository.findById(id).map(this::convertToDTO);
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDetail(ticket.getDetail());
        dto.setTicketType(ticket.getTicketType());
        dto.setPriority(ticket.getPriority().name());
        dto.setTicketState(ticket.getTicketState().name());
        dto.setTimeCreated(ticket.getTimeCreated().toString());

        if (ticket.getTimeResolved() != null) {
            dto.setTimeResolved(ticket.getTimeResolved().toString());
        }

        dto.setCreatorId(ticket.getCreator().getId());
        dto.setCreatorName(ticket.getCreator().getFullName());

        if (ticket.getAssignee() != null) {
            dto.setAssigneeId(ticket.getAssignee().getId());
            dto.setAssigneeName(ticket.getAssignee().getFullName());
        }

        return dto;
    }
}