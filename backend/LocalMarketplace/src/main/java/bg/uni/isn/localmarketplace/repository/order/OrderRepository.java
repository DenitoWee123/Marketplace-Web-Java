package bg.uni.isn.localmarketplace.repository.order;

import bg.uni.isn.localmarketplace.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select o from Order o where o.id = :id")
    java.util.Optional<Order> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    Page<Order> findByUser_Username(String username, Pageable pageable);
}
