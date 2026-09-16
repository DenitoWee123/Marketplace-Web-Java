package bg.uni.isn.localmarketplace.repository;

import bg.uni.isn.localmarketplace.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
}
