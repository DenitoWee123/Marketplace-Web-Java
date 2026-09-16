package bg.uni.isn.localmarketplace.dto.output.user;

import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.vo.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response body containing detailed information about a user profile.")
public record UserDetailsDTO(
    @Schema(description = "Username of the user.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    String username,

    @Schema(description = "Email of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Schema(description = "The type of the user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    UserType userType,

    @Schema(description = "List of the products the user sells", requiredMode = Schema.RequiredMode.REQUIRED)
    List<OutputUserProductDTO> products
) {
    public static UserDetailsDTO from(User user) {
        return bg.uni.isn.localmarketplace.mapper.UserDetailsMapper.toDto(user);
    }
}
