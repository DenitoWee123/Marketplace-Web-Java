package bg.uni.isn.localmarketplace.dto.input.event;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Request body for updating an existing event")
public record UpdateEventDTO(

    @Size(max = 255, message = ValidationConstants.Event.LENGTH_TITLE)
    @Schema(description = "New event title")
    String title,

    @Size(max = 1000, message = ValidationConstants.Event.LENGTH_DESCRIPTION)
    @Schema(description = "New event description")
    String description,

    @Schema(description = "Whether the event should be active")
    Boolean active,

    // STORYTELLING_FEATURES
    @Schema(description = "Story content — for STORYTELLING_FEATURES events")
    String content,

    // CRAFT_FAIRS
    @Schema(description = "Fair location — for CRAFT_FAIRS events")
    String location,

    @Schema(description = "Start date — for CRAFT_FAIRS and PROMOTIONAL_CAMPAIGNS events")
    LocalDateTime startDate,

    @Schema(description = "End date — for CRAFT_FAIRS and PROMOTIONAL_CAMPAIGNS events")
    LocalDateTime endDate,

    // PROMOTIONAL_CAMPAIGNS
    @Schema(description = "Discount type — for PROMOTIONAL_CAMPAIGNS events")
    DiscountType discountType,

    @Min(value = 0, message = ValidationConstants.Event.MIN_DISCOUNT_VALUE)
    @Schema(description = "Discount value — for PROMOTIONAL_CAMPAIGNS events", minimum = "0")
    Long discountValue
) {
}
