package bg.uni.isn.localmarketplace.dto.input.user;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new user.")
public record CreateUserDTO(
    @NotBlank(message = ValidationConstants.User.BLANK_USERNAME)
    @Size(max = 50, message = ValidationConstants.User.LENGTH_USERNAME)
    @Schema(description = "Username of the user. Must not be blank and must be at most 50 characters.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50, minLength = 1)
    String username,

    @NotBlank(message = ValidationConstants.User.BLANK_FIRST_NAME)
    @Size(max = 50, message = ValidationConstants.User.LENGTH_FIRST_NAME)
    @Schema(description = "First name of the user. Must not be blank and must be at most 50 characters.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50, minLength = 1)
    String firstName,

    @NotBlank(message = ValidationConstants.User.BLANK_LAST_NAME)
    @Size(max = 50, message = ValidationConstants.User.LENGTH_LAST_NAME)
    @Schema(description = "Last name of the user. Must not be blank and must be at most 50 characters.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50, minLength = 1)
    String lastName,

    @NotBlank(message = ValidationConstants.User.BLANK_PASSWORD)
    @Size(min = 6, max = 50, message = ValidationConstants.User.LENGTH_PASSWORD)
    @Schema(description = "Password of the user. Must not be blank and must be at most 50 characters.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50, minLength = 5)
    String password,

    @NotBlank(message = ValidationConstants.User.BLANK_EMAIL)
    @Email(message = ValidationConstants.User.INVALID_EMAIL)
    @Size(max = 255, message = ValidationConstants.User.LENGTH_EMAIL)
    @Schema(description = "Email of the user. Must not be blank and must be at most 5255 characters.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    String email,

    @NotBlank(message = ValidationConstants.User.BLANK_PHONE)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = ValidationConstants.User.INVALID_PHONE)
    @Schema(description = "Phone number of the user. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
    String phone
) {
}
