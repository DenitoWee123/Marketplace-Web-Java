package bg.uni.isn.localmarketplace.controller;

import bg.uni.isn.localmarketplace.dto.input.user.CreateUserDTO;
import bg.uni.isn.localmarketplace.dto.input.user.UpdateUserDTO;
import bg.uni.isn.localmarketplace.dto.output.user.UserDetailsDTO;
import bg.uni.isn.localmarketplace.service.contract.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users in the local marketplace")
public class UserController {

    private static final int PAGE_SIZE = 10;

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get user details", description = "Retrieves profile information for a specific user by their unique username.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile found successfully"),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<UserDetailsDTO> getUser(
        @Parameter(description = "The unique username of the user", required = true)
        @PathVariable String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination", description = "Retrieves a paginated list of all registered users in the platform.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated list of users")
    })
    public ResponseEntity<Page<UserDetailsDTO>> getAllUsers(
        @Parameter(
            in = ParameterIn.QUERY,
            description = "Zero-based page index (0..N), page size, and optional sorting criteria (e.g., username,asc)",
            schema = @Schema(type = "string", defaultValue = "page=0&size=10")
        )
        @PageableDefault(size = PAGE_SIZE) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PostMapping
    @Operation(summary = "Register a new user", description = "Creates a new user profile in the system. Validates username, email, and phone format.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or email/username already taken", content = @Content)
    })
    public ResponseEntity<UserDetailsDTO> createUser(
        @Valid @RequestBody CreateUserDTO createUserDTO) {
        UserDetailsDTO createdUser = userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update user details", description = "Updates credentials or contact information for an existing user. Allows partial updates for email, password, or phone.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already in use by another account", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<UserDetailsDTO> updateUser(
        @Parameter(description = "The unique username of the user to be updated", required = true, example = "john_doe")
        @PathVariable String username,
        @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.ok(userService.updateUser(username, updateUserDTO));
    }

    @PatchMapping("/become-vendor")
    @Operation(summary = "Make a user vendor", description = "Makes the user able to sell products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorised entry. User must be logged."),
        @ApiResponse(responseCode = "404", description = "User with username not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> becomeVendor(@Parameter(hidden = true) Principal principal) {

        userService.makeUserVendor(principal.getName());
        return ResponseEntity.ok("The user is a Vendor. Please generate a new JWT token.");
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete a user", description = "Removes a user account completely from the local marketplace database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "The unique username of the user to be deleted", required = true, example = "john_doe")
        @PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}/is-admin")
    @Operation(summary = "Check user role privileges", description = "Verifies if the specified user has administrator authority.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked administration status"),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<Boolean> isAdmin(
        @Parameter(description = "The unique username to check", required = true, example = "admin_user")
        @PathVariable String username) {
        return ResponseEntity.ok(userService.isAdmin(username));
    }
}
