package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.user.CreateUserDTO;
import bg.uni.isn.localmarketplace.dto.input.user.UpdateUserDTO;
import bg.uni.isn.localmarketplace.dto.output.user.UserDetailsDTO;
import bg.uni.isn.localmarketplace.exception.user.UserAlreadyExistsException;
import bg.uni.isn.localmarketplace.exception.user.UserAlreadyVendorException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Checks whether the given user has administrator privileges.
     *
     * @param username the name to check
     * @return {@code true} if the user is an admin, {@code false} otherwise
     * @throws UserNotFoundException if no user with the given username exists
     */
    boolean isAdmin(String username);

    /**
     * Retrieves the user details for the given username.
     *
     * @param username the username to load
     * @return a {@link UserDetailsDTO} containing profile information, roles, and project data
     * @throws UserNotFoundException if no user with the given username exists
     */
    UserDetailsDTO getUser(String username);

    /**
     * Retrieves a paginated and optionally sorted page of users converted to DTOs.
     *
     * @param pageable the pagination and sorting configuration (e.g., page number,
     *                 page size, and sort fields/direction)
     * @return a {@link Page} containing the requested slice of {@link UserDetailsDTO} objects,
     * along with pagination metadata (total elements, total pages, etc.)
     */
    Page<UserDetailsDTO> getAllUsers(Pageable pageable);

    /**
     * Registers and creates a new user in the system.
     *
     * @param registrationDTO the user data for account creation
     * @return the created user details DTO
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    UserDetailsDTO createUser(CreateUserDTO registrationDTO);

    /**
     * Updates an existing user profile details.
     *
     * @param username  the username of the user to update
     * @param updateDTO the new data to apply to the profile
     * @return the updated user details DTO
     * @throws UserNotFoundException if no user with the given username exists
     */
    UserDetailsDTO updateUser(String username, UpdateUserDTO updateDTO);

    /**
     * Makes a user able to sell products
     *
     * @param username the username of the user
     * @throws UserNotFoundException      if the user does not exist
     * @throws UserAlreadyVendorException if the user is already a vendor
     */
    void makeUserVendor(String username);

    /**
     * Deletes the user.
     *
     * @param username the username of the user to delete
     * @throws UserNotFoundException if the user does not exist or is already marked deleted
     */
    void deleteUser(String username);
}
