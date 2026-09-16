package bg.uni.isn.localmarketplace.repository;

import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.vo.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByProductTypeAndMaker_Username(ProductType productType, String makerUsername, Pageable pageable);

    Page<Product> findByProductType(ProductType productType, Pageable pageable);

    Page<Product> findByMakerUsername(String makerUsername, Pageable pageable);

    @Query("""
SELECT p FROM Product p
WHERE
(:productTypes IS NULL OR p.productType IN :productTypes)
AND (:makerUsername IS NULL OR p.maker.username = :makerUsername)
AND (
    :inStock IS NULL
    OR (:inStock = true AND p.quantity > 0)
    OR (:inStock = false AND p.quantity = 0)
)
""")
    Page<Product> findProductsWithFilters(
        @Param("productTypes") List<ProductType> productTypes,
        @Param("makerUsername") String makerUsername,
        @Param("inStock") Boolean inStock,
        Pageable pageable
    );
}
