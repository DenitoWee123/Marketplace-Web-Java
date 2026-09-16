package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.Event;
import bg.uni.isn.localmarketplace.vo.DiscountType;
import bg.uni.isn.localmarketplace.vo.EventType;
import java.time.LocalDateTime;
import bg.uni.isn.localmarketplace.dto.output.event.*;

public final class EventDetailsMapper {
    private EventDetailsMapper() {}
    public static EventDetailsDTO toDto(Event event) {
        String location = event.getFairDetails() != null ? event.getFairDetails().getLocation() : null;

        DiscountType discountType = null;
        Long discountValue = null;
        if (event.getPromotionDetails() != null) {
            discountType = event.getPromotionDetails().getDiscountType();
            discountValue = event.getPromotionDetails().getDiscountValue();
        }

        return new EventDetailsDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getType(),
            event.getUser().getUsername(),
            event.getStartDate(),
            event.getEndDate(),
            event.isActive(),
            event.getContent(),
            location,
            discountType,
            discountValue
        );
    }
}
