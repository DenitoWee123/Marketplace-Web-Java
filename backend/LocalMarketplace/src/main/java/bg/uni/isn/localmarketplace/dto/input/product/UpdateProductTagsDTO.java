package bg.uni.isn.localmarketplace.dto.input.product;

import java.util.Set;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateProductTagsDTO(
    @NotNull @Size(max = 50)
    @Schema(description = "Existing tag IDs; an empty set removes all product tags")
    Set<@NotNull @Positive Long> tagIds
) {}
