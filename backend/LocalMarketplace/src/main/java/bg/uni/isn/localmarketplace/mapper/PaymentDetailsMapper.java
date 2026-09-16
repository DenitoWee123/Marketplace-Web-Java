package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.Payment;
import bg.uni.isn.localmarketplace.domain.order.Order;
import bg.uni.isn.localmarketplace.vo.CurrencyType;
import bg.uni.isn.localmarketplace.vo.PaymentMethod;
import bg.uni.isn.localmarketplace.dto.output.payment.*;

public final class PaymentDetailsMapper {
    private PaymentDetailsMapper() {}
    public static PaymentDetailsDTO toDto(Payment payment) {
        Order order = payment.getOrder();

        PaymentOrderDTO paymentOrderDTO =
            new PaymentOrderDTO(order.getId(), order.getUser().getUsername(), order.getStatus(), order.getTotalAmount(),
                order.getCurrency());

        return new PaymentDetailsDTO(payment.getId(), paymentOrderDTO, payment.getAmount(), payment.getCurrency(),
            payment.getPaymentMethod());
    }
}
