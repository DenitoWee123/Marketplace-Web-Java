package bg.uni.isn.localmarketplace.dto.input.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateTagDTO(
    @NotBlank @Size(max = 50) @Schema(example = "handmade") String name
) {}
