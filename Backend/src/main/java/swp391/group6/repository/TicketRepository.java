package swp391.group6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swp391.group6.model.Ticket;
import swp391.group6.model.TicketState;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByTicketCreator(long id);
    List<Ticket> findByTicketStateNot(TicketState state);
}