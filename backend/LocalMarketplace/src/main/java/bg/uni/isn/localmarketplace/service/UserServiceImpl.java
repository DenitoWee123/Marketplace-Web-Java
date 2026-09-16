package bg.uni.isn.localmarketplace.service;

import java.util.Optional;

import bg.uni.isn.localmarketplace.exception.user.UserAlreadyVendorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.dto.input.user.CreateUserDTO;
import bg.uni.isn.localmarketplace.dto.input.user.UpdateUserDTO;
import bg.uni.isn.localmarketplace.dto.output.user.UserDetailsDTO;
import bg.uni.isn.localmarketplace.exception.user.EmailAlreadyExistsException;
import bg.uni.isn.localmarketplace.exception.user.UserAlreadyExistsException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.repository.UserRepository;
import bg.uni.isn.localmarketplace.service.contract.UserService;
import bg.uni.isn.localmarketplace.vo.UserType;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isAdmin(String username) {
        User user = getUserByUsername(username);
        return user.isAdmin();
    }

    @Override
    public UserDetailsDTO getUser(String username) {
        User user = getUserByUsername(username);
        return UserDetailsDTO.from(user);
    }

    @Override
    public Page<UserDetailsDTO> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(UserDetailsDTO::from);
    }

    @Override
    public UserDetailsDTO createUser(CreateUserDTO dto) {

        if (userRepository.existsByUsername(dto.username())) {
            throw new UserAlreadyExistsException(dto.username());
        }

       if (userRepository.existsByEmail(dto.email())) {
        throw new EmailAlreadyExistsException("Email address is already in use."); 
       }

        User newUser =
            new User(dto.username(), dto.firstName(), dto.lastName(),
                passwordEncoder.encode(dto.password()), dto.email(), dto.phone(),
                UserType.CUSTOMER);

        userRepository.save(newUser);
        return UserDetailsDTO.from(newUser);
    }

    @Override
    public UserDetailsDTO updateUser(String username, UpdateUserDTO updateDTO) {

        User existingUser = getUserByUsername(username);

        if (!existingUser.getEmail().equals(updateDTO.email()) && userRepository.existsByEmail(updateDTO.email())) {
            throw new EmailAlreadyExistsException("Email already in use!");
        }

        if (updateDTO.email() != null && !updateDTO.email().isBlank()) {
            existingUser.setEmail(updateDTO.email());
        }
        if (updateDTO.password() != null && !updateDTO.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updateDTO.password()));
        }
        if (updateDTO.phone() != null && !updateDTO.phone().isBlank()) {
            existingUser.setPhone(updateDTO.phone());
        }

        return UserDetailsDTO.from(existingUser);
    }

    @Override
    public void makeUserVendor(String username) {
        User user = getUserByUsername(username);
        if (user.getUserType().equals(UserType.VENDOR)) {
            throw new UserAlreadyVendorException("User " +  username + " is already a vendor.");
        }

        user.setUserType(UserType.VENDOR);
    }

    @Override
    public void deleteUser(String username) {
        User user = getUserByUsername(username);
        userRepository.delete(user);
    }

    private User getUserByUsername(String username) {
        Optional<User> user = userRepository.findById(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        return user.get();
    }
}
