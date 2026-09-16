package bg.uni.isn.localmarketplace.dto.input.auth;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email address for password reset.")
public record ForgotPasswordRequestDTO(

    @NotBlank(message = ValidationConstants.User.BLANK_EMAIL)
    @Email(message = ValidationConstants.User.INVALID_EMAIL)
    @Schema(description = "Email address associated with the account.", requiredMode = Schema.RequiredMode.REQUIRED)
    String email
) {
}
