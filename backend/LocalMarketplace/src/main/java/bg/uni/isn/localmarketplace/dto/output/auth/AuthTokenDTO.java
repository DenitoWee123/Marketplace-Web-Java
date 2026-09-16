package bg.uni.isn.localmarketplace.dto.output.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT bearer token returned after successful authentication.")
public record AuthTokenDTO(

    @Schema(description = "The JWT access token.", requiredMode = Schema.RequiredMode.REQUIRED)
    String token,

    @Schema(description = "Username of the authenticated user.", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(description = "Token type, always 'Bearer'.", requiredMode = Schema.RequiredMode.REQUIRED)
    String tokenType,

    @Schema(description = "Role of the authenticated user (CUSTOMER, VENDOR, ADMIN).", requiredMode = Schema.RequiredMode.REQUIRED)
    String userType
) {
    public static AuthTokenDTO of(String token, String username, String userType) {
        return new AuthTokenDTO(token, username, "Bearer", userType);
    }
}
