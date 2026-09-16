package bg.uni.isn.localmarketplace.repository;

import bg.uni.isn.localmarketplace.domain.Event;
import bg.uni.isn.localmarketplace.vo.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByType(EventType type, Pageable pageable);

    Page<Event> findByActive(boolean active, Pageable pageable);

    Page<Event> findByTypeAndActive(EventType type, boolean active, Pageable pageable);

    @Query("""
SELECT e FROM Event e
WHERE
(:types IS NULL OR e.type IN :types)
AND (
    (:active IS NULL AND :upcoming IS NULL)
    OR (:active IS NOT NULL AND e.active = :active)
    OR (:upcoming IS NOT NULL AND :upcoming = true AND e.startDate >= CURRENT_TIMESTAMP)
)
""")
    Page<Event> findEventsWithFilters(
        @Param("types") List<EventType> types,
        @Param("active") Boolean active,
        @Param("upcoming") Boolean upcoming,
        Pageable pageable
    );
}
