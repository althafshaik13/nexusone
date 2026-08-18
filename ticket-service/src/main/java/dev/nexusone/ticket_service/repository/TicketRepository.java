package dev.nexusone.ticket_service.repository;

import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
            AND (:customerId IS NULL OR t.customerId = :customerId)
            AND (:q = ''
                 OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY t.createdAt DESC
            """)
    List<Ticket> search(@Param("status") TicketStatus status, @Param("customerId") UUID customerId, @Param("q") String q);

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countByStatus();

    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0)
            FROM tickets
            WHERE status IN ('RESOLVED', 'CLOSED')
            """, nativeQuery = true)
    Double averageResolutionHours();
}
