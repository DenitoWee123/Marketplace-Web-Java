package bg.uni.isn.localmarketplace.service;

import bg.uni.isn.localmarketplace.domain.Event;
import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.dto.input.event.CreateEventDTO;
import bg.uni.isn.localmarketplace.dto.input.event.UpdateEventDTO;
import bg.uni.isn.localmarketplace.dto.output.event.EventDetailsDTO;
import bg.uni.isn.localmarketplace.exception.event.EventDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.event.InvalidEventDataException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.repository.EventRepository;
import bg.uni.isn.localmarketplace.repository.UserRepository;
import bg.uni.isn.localmarketplace.service.contract.EventService;
import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public EventDetailsDTO createEvent(String username, CreateEventDTO dto) {
        User user = getUser(username);
        validateTypeSpecificFields(dto);

        String content = dto.type() == EventType.STORYTELLING_FEATURES ? dto.content() : null;
        Event.PromotionDetails promotionDetails = dto.type() == EventType.PROMOTIONAL_CAMPAIGNS
            ? buildPromotionDetails(dto) : null;
        Event.FairDetails fairDetails = dto.type() == EventType.CRAFT_FAIRS
            ? buildFairDetails(dto) : null;

        Event event = new Event(
            dto.title(),
            dto.description(),
            dto.type(),
            user,
            dto.startDate(),
            dto.endDate(),
            dto.active(),
            content,
            promotionDetails,
            fairDetails
        );

        return EventDetailsDTO.from(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailsDTO getEvent(Long id) {
        return EventDetailsDTO.from(findEvent(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventDetailsDTO> getAllEvents(List<EventType> types, Boolean active, Boolean upcoming,
                                              Pageable pageable) {

        Page<Event> page = eventRepository.findEventsWithFilters(
            types,
            active,
            upcoming,
            pageable
        );

        return page.map(EventDetailsDTO::from);
    }

    @Override
    public EventDetailsDTO updateEvent(Long id, String username, UpdateEventDTO dto) {
        Event event = findOwnedEvent(id, username);
        applyCommonFields(event, dto);
        applyTypeSpecificUpdate(event, dto);
        validateMergedDates(event);
        return EventDetailsDTO.from(event);
    }

    @Override
    public void deleteEvent(Long id, String username) {
        eventRepository.delete(findOwnedEvent(id, username));
    }

    // --- private helpers ---

    private void validateMergedDates(Event event) {
        if (event.getStartDate() != null && event.getEndDate() != null
            && !event.getEndDate().isAfter(event.getStartDate())) {
            throw new InvalidEventDataException(ValidationConstants.Event.END_DATE_BEFORE_START_DATE);
        }
    }

    private void validateTypeSpecificFields(CreateEventDTO dto) {
        switch (dto.type()) {
            case CRAFT_FAIRS -> {
                if (dto.location() == null || dto.location().isBlank()) {
                    throw new InvalidEventDataException(ValidationConstants.Event.BLANK_LOCATION);
                }
                if (dto.startDate() == null) {
                    throw new InvalidEventDataException(ValidationConstants.Event.NULL_START_DATE);
                }
                if (dto.endDate() == null) {
                    throw new InvalidEventDataException(ValidationConstants.Event.NULL_END_DATE);
                }
                if (!dto.endDate().isAfter(dto.startDate())) {
                    throw new InvalidEventDataException(ValidationConstants.Event.END_DATE_BEFORE_START_DATE);
                }
            }
            case PROMOTIONAL_CAMPAIGNS -> {
                if (dto.discountType() == null) {
                    throw new InvalidEventDataException(ValidationConstants.Event.NULL_DISCOUNT_TYPE);
                }
                if (dto.discountValue() == null) {
                    throw new InvalidEventDataException(ValidationConstants.Event.NULL_DISCOUNT_VALUE);
                }
                if (dto.startDate() == null) {
                    throw new InvalidEventDataException(ValidationConstants.Event.NULL_START_DATE);
                }
                if (dto.endDate() == null) {
                    throw new InvalidEventDataException(ValidationConstants.Event.NULL_END_DATE);
                }
                if (!dto.endDate().isAfter(dto.startDate())) {
                    throw new InvalidEventDataException(ValidationConstants.Event.END_DATE_BEFORE_START_DATE);
                }
            }
            case STORYTELLING_FEATURES -> {
                if (dto.content() == null || dto.content().isBlank()) {
                    throw new InvalidEventDataException(ValidationConstants.Event.BLANK_CONTENT);
                }
            }
        }
    }

    private Event.FairDetails buildFairDetails(CreateEventDTO dto) {
        Event.FairDetails fair = new Event.FairDetails();
        fair.setLocation(dto.location());
        return fair;
    }

    private Event.PromotionDetails buildPromotionDetails(CreateEventDTO dto) {
        Event.PromotionDetails promo = new Event.PromotionDetails();
        promo.setDiscountType(dto.discountType());
        promo.setDiscountValue(dto.discountValue());
        return promo;
    }

    private void applyCommonFields(Event event, UpdateEventDTO dto) {
        if (dto.title() != null) {
            event.setTitle(dto.title());
        }
        if (dto.description() != null) {
            event.setDescription(dto.description());
        }
        if (dto.active() != null) {
            event.setActive(dto.active());
        }
        if (dto.startDate() != null) {
            event.setStartDate(dto.startDate());
        }
        if (dto.endDate() != null) {
            event.setEndDate(dto.endDate());
        }
    }

    private void applyTypeSpecificUpdate(Event event, UpdateEventDTO dto) {
        switch (event.getType()) {
            case CRAFT_FAIRS -> {
                if (dto.location() != null) {
                    Event.FairDetails fair = event.getFairDetails();
                    if (fair == null) {
                        fair = new Event.FairDetails();
                        event.setFairDetails(fair);
                    }
                    fair.setLocation(dto.location());
                }
            }
            case PROMOTIONAL_CAMPAIGNS -> {
                if (dto.discountType() != null || dto.discountValue() != null) {
                    Event.PromotionDetails promo = event.getPromotionDetails();
                    if (promo == null) {
                        promo = new Event.PromotionDetails();
                        event.setPromotionDetails(promo);
                    }
                    if (dto.discountType() != null) {
                        promo.setDiscountType(dto.discountType());
                    }
                    if (dto.discountValue() != null) {
                        promo.setDiscountValue(dto.discountValue());
                    }
                }
            }
            case STORYTELLING_FEATURES -> {
                if (dto.content() != null) {
                    event.setContent(dto.content());
                }
            }
        }
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new EventDoesNotExistException("Event with id " + id + " does not exist"));
    }

    private Event findOwnedEvent(Long id, String username) {
        Event event = findEvent(id);
        User requester = getUser(username);
        if (!requester.isAdmin() && !event.getUser().getUsername().equals(username)) {
            throw new OwnershipMismatchException(ValidationConstants.Event.NOT_OWNED);
        }
        return event;
    }

    private User getUser(String username) {
        return userRepository.findById(username)
            .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));
    }
}
