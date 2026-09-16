package bg.uni.isn.localmarketplace.dto.output.user;

import bg.uni.isn.localmarketplace.vo.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing admin-only user information.")
public record AdminOnlyOutputUserDTO(
    @Schema(description = "Username of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(description = "First name of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
    String firstName,

    @Schema(description = "Last name of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
    String lastName,

    @Schema(description = "Email address of the user.", requiredMode = Schema.RequiredMode.REQUIRED, format = "email")
    String email,

    @Schema(description = "Phone number of the user", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber,

    @Schema(description = "Indicates the role of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
    UserType userType
) {
}
