package bg.uni.isn.localmarketplace.dto.input.event;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.DiscountType;
import bg.uni.isn.localmarketplace.vo.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Request body for creating a new event")
public record CreateEventDTO(

    @NotBlank(message = ValidationConstants.Event.BLANK_TITLE)
    @Size(max = 255, message = ValidationConstants.Event.LENGTH_TITLE)
    @Schema(description = "Event title", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Size(max = 1000, message = ValidationConstants.Event.LENGTH_DESCRIPTION)
    @Schema(description = "Event description")
    String description,

    @NotNull(message = ValidationConstants.Event.NULL_TYPE)
    @Schema(description = "Event type: CRAFT_FAIRS, PROMOTIONAL_CAMPAIGNS, or STORYTELLING_FEATURES",
        requiredMode = Schema.RequiredMode.REQUIRED)
    EventType type,

    @Schema(description = "Whether the event is active")
    boolean active,

    // STORYTELLING_FEATURES
    @Schema(description = "Story content — required for STORYTELLING_FEATURES")
    String content,

    // CRAFT_FAIRS
    @Schema(description = "Fair location — required for CRAFT_FAIRS")
    String location,

    @Schema(description = "Start date — required for CRAFT_FAIRS and PROMOTIONAL_CAMPAIGNS")
    LocalDateTime startDate,

    @Schema(description = "End date — required for CRAFT_FAIRS and PROMOTIONAL_CAMPAIGNS")
    LocalDateTime endDate,

    // PROMOTIONAL_CAMPAIGNS
    @Schema(description = "Discount type — required for PROMOTIONAL_CAMPAIGNS")
    DiscountType discountType,

    @Min(value = 0, message = ValidationConstants.Event.MIN_DISCOUNT_VALUE)
    @Schema(description = "Discount value — required for PROMOTIONAL_CAMPAIGNS", minimum = "0")
    Long discountValue
) {
}
