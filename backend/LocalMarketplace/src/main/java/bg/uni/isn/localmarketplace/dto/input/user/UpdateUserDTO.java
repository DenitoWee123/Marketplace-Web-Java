package bg.uni.isn.localmarketplace.dto.input.user;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserDTO(

    @Size(min = 6, max = 50, message = ValidationConstants.User.LENGTH_PASSWORD)
    @Schema(description = "New password of the user. Must not be blank and must be between 6 and 50 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, minLength = 6, maxLength = 50)
    String password,

    @Email(message = ValidationConstants.User.INVALID_EMAIL)
    @Size(max = 255, message = ValidationConstants.User.LENGTH_EMAIL)
    @Schema(description = "New email of the user. Must not be blank and must be at most 255 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 255)
    String email,

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = ValidationConstants.User.INVALID_PHONE)
    @Schema(description = "New phone number of the user. Must not be blank and must be in valid international format.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String phone
) {
}
