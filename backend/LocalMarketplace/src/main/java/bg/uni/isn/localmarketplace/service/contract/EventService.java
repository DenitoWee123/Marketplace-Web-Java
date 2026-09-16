package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.event.CreateEventDTO;
import bg.uni.isn.localmarketplace.dto.input.event.UpdateEventDTO;
import bg.uni.isn.localmarketplace.dto.output.event.EventDetailsDTO;
import bg.uni.isn.localmarketplace.exception.event.EventDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.event.InvalidEventDataException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.vo.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {

    /**
     * Creates a new event owned by the given user. Type-specific fields are populated based on the
     * {@code dto.type()}: {@code CRAFT_FAIRS} → location + dates; {@code PROMOTIONAL_CAMPAIGNS} →
     * discountType + discountValue + dates; {@code STORYTELLING_FEATURES} → content.
     *
     * @param username the username of the authenticated creator
     * @param dto      event creation data
     * @return {@link EventDetailsDTO} of the newly created event
     * @throws UserNotFoundException     if the user does not exist
     * @throws InvalidEventDataException if required type-specific fields are missing or dates are invalid
     */
    EventDetailsDTO createEvent(String username, CreateEventDTO dto);

    /**
     * Retrieves the details of a single event by its id.
     *
     * @param id the event id
     * @return {@link EventDetailsDTO}
     * @throws EventDoesNotExistException if no event with the given id exists
     */
    EventDetailsDTO getEvent(Long id);

    /**
     * Returns a paginated list of all events, optionally filtered by type and/or active status.
     *
     * @param types    when non-null, only events of these types are returned
     * @param active   when non-null, only events matching this active flag are returned
     * @param upcoming the boolean whether to get upcoming events or not
     * @param pageable pagination and sorting parameters
     * @return page of {@link EventDetailsDTO}
     */
    Page<EventDetailsDTO> getAllEvents(List<EventType> types, Boolean active, Boolean upcoming , Pageable pageable);

    /**
     * Updates an existing event. Only the owner (or an admin) may update. Null fields in {@code dto} are ignored.
     *
     * @param id       the event id
     * @param username username of the requesting user
     * @param dto      fields to update
     * @return updated {@link EventDetailsDTO}
     * @throws EventDoesNotExistException if no event with the given id exists
     * @throws OwnershipMismatchException if the requesting user is not the event owner or an admin
     * @throws InvalidEventDataException  if both startDate and endDate are provided but endDate is not after startDate
     */
    EventDetailsDTO updateEvent(Long id, String username, UpdateEventDTO dto);

    /**
     * Deletes an event. Only the owner (or an admin) may delete.
     *
     * @param id       the event id
     * @param username username of the requesting user
     * @throws EventDoesNotExistException if no event with the given id exists
     * @throws OwnershipMismatchException if the requesting user is not the event owner or an admin
     */
    void deleteEvent(Long id, String username);
}
