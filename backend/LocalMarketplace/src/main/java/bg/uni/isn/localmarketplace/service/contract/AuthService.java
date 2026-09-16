package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.auth.LoginRequestDTO;
import bg.uni.isn.localmarketplace.dto.input.user.CreateUserDTO;
import bg.uni.isn.localmarketplace.dto.output.auth.AuthTokenDTO;
import bg.uni.isn.localmarketplace.dto.output.user.UserDetailsDTO;
import bg.uni.isn.localmarketplace.exception.auth.InvalidCredentialsException;
import bg.uni.isn.localmarketplace.exception.user.UserAlreadyExistsException;

public interface AuthService {

    /**
     * Registers a new user with a BCrypt-hashed password.
     *
     * @param dto the registration data
     * @return the created user details (no password field)
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    UserDetailsDTO register(CreateUserDTO dto);

    /**
     * Authenticates the given credentials and issues a JWT.
     *
     * @param dto username + plain-text password
     * @return a {@link AuthTokenDTO} containing the signed JWT
     * @throws InvalidCredentialsException if credentials are invalid
     */
    AuthTokenDTO login(LoginRequestDTO dto);

    /**
     * Client-side logout for stateless JWT — always succeeds.
     * Callers should discard the token on their side.
     */
    void logout();

    /**
     * Stub for password-reset initiation. Full email flow is out of scope.
     *
     * @param email the email address to send the reset link to
     */
    void forgotPassword(String email);
}
