package bg.uni.isn.localmarketplace.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bg.uni.isn.localmarketplace.dto.input.auth.ForgotPasswordRequestDTO;
import bg.uni.isn.localmarketplace.dto.input.auth.LoginRequestDTO;
import bg.uni.isn.localmarketplace.dto.input.user.CreateUserDTO;
import bg.uni.isn.localmarketplace.dto.output.auth.AuthTokenDTO;
import bg.uni.isn.localmarketplace.dto.output.user.UserDetailsDTO;
import bg.uni.isn.localmarketplace.service.contract.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register, login, logout, and password reset")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
        description = "Creates a new CUSTOMER account with a BCrypt-hashed password and returns the user profile.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or username/email already taken",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<UserDetailsDTO> register(@Valid @RequestBody CreateUserDTO dto) {
        UserDetailsDTO created = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    @Operation(summary = "Login",
        description = "Authenticates with username + password and returns a signed JWT. " +
            "Supply it in subsequent requests as: Authorization: Bearer <token>")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<AuthTokenDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthTokenDTO token = authService.login(dto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout",
        description = "Stateless JWT logout — discard the token on the client side.")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    public ResponseEntity<Map<String, String>> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully. Please discard your token."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset (stub)",
        description = "Stub endpoint — full email-reset flow is not implemented yet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reset request acknowledged"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        authService.forgotPassword(dto.email());
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a reset link will be sent."));
    }
}
