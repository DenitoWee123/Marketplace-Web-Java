package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.vo.UserType;
import java.util.List;
import bg.uni.isn.localmarketplace.dto.output.user.*;

public final class UserDetailsMapper {
    private UserDetailsMapper() {}
    public static UserDetailsDTO toDto(User user) {
        List<OutputUserProductDTO> products = user.getProducts().stream()
            .map(product -> new OutputUserProductDTO(product.getDescription(), product.getPrice(), product.getQuantity()))
            .toList();

        return new UserDetailsDTO(user.getUsername(), user.getEmail(), user.getUserType(), products);
    }
}
