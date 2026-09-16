package bg.uni.isn.localmarketplace.repository;

import bg.uni.isn.localmarketplace.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByOrder_Id(Long orderId, Pageable pageable);
}
