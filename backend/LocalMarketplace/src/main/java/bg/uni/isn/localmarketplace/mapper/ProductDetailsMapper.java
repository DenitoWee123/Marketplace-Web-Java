package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.vo.ProductType;
import bg.uni.isn.localmarketplace.dto.output.product.*;

public final class ProductDetailsMapper {
    private ProductDetailsMapper() {}
    public static ProductDetailsDTO toDto(Product product) {

        return new ProductDetailsDTO(product.getId(), product.getProductType(), product.getName(),
            product.getDescription(), product.getProductPicturePath(), product.getPrice(), product.getQuantity(),
            new OutputProductMakerDTO(product.getMaker().getUsername(), product.getMaker().getEmail()),
            product.getTags().stream().sorted(java.util.Comparator.comparing(bg.uni.isn.localmarketplace.domain.Tag::getName))
                .map(bg.uni.isn.localmarketplace.mapper.TagMapper::toDto).toList());
    }
}
