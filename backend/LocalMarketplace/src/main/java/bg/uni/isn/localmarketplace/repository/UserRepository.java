package bg.uni.isn.localmarketplace.repository;

import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.username = :username")
    java.util.Optional<User> findForUpdate(@org.springframework.data.repository.query.Param("username") String username);

    boolean existsByUsername(@NotBlank(message = ValidationConstants.User.BLANK_USERNAME)
                             @Size(max = 50, message = ValidationConstants.User.LENGTH_USERNAME) String username);

    boolean existsByEmail(String email);

//    User save(User user);
//    Optional<User> findById(Long id);
//    List<User> findAll();
//    void deleteById(Long id);
//    boolean existsById(Long id);
//
//    Optional<User> findByEmail(String email);
}
