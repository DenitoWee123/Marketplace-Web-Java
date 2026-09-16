package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.product.CreateProductDTO;
import bg.uni.isn.localmarketplace.dto.input.product.UpdateProductDTO;
import bg.uni.isn.localmarketplace.dto.output.product.ProductDetailsDTO;

import bg.uni.isn.localmarketplace.exception.file.FileServiceException;
import bg.uni.isn.localmarketplace.exception.file.InvalidFileFormatException;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.vo.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    /**
     * Retrieves the product details for the given product
     *
     * @param id the id of the product
     * @return {@link ProductDetailsDTO}
     * @throws ProductDoesNotExistException if the product with given id does not exist
     */
    ProductDetailsDTO getProduct(Long id);

    /**
     * Retrieves a paginated and optionally sorted page of all products converted to DTOs.
     *
     * @param pageable pagination and sorting details for the products
     * @return a page of product responses
     */
    Page<ProductDetailsDTO> getAllProducts(Pageable pageable);

    /**
     * Retrieves a paginated page of products optionally filtered by type and maker
     *
     * @param productTypes   the types of the products to get
     * @param makerUsername the username of the maker
     * @param inStock       true if product is in stock, otherwise false
     * @param pageable      pagination and sorting details for the products
     * @return a page of product responses
     */
    Page<ProductDetailsDTO> getProductsWithFilters(List<ProductType> productTypes, String makerUsername,
                                                   Boolean inStock, Pageable pageable);

    /**
     * Uploads or replaces the product picture for the specified product.
     * <p>
     * The picture must be in JPG format. If a picture already exists for the product,
     * it will be replaced.
     *
     * @param productId the unique identifier of the product whose picture will be set
     * @param picture   the image file to be uploaded (must be a valid JPG file)
     * @throws IllegalArgumentException     if the picture is null or empty
     * @throws InvalidFileFormatException   if the provided file is not a valid JPG image
     * @throws ProductDoesNotExistException if no product exists with the given id
     */
    void setProductPicture(Long productId, String username, MultipartFile picture);

    /**
     * Creates a new artisan product and assigns it to the current logged-in user.
     *
     * @param dto      the data for the new product
     * @param username username of the authenticated creator (maker)
     * @return {@link ProductDetailsDTO}
     * @throws UserNotFoundException if the user does not exist
     */
    ProductDetailsDTO createProduct(CreateProductDTO dto, String username);

    /**
     * Updates the product's details.
     *
     * @param id       the ID of the product to update
     * @param username username of the user making the request
     * @param dto      the new product data
     * @return {@link ProductDetailsDTO}
     * @throws ProductDoesNotExistException if the product does not exist
     * @throws UserNotFoundException        if the user does not exist
     */
    ProductDetailsDTO updateProduct(Long id, String username, UpdateProductDTO dto);

    /**
     * Removes a product from the marketplace.
     *
     * @param id       the ID of the product to delete
     * @param username username of the user making the request
     * @throws ProductDoesNotExistException if the product does not exist
     * @throws UserNotFoundException        if the user does not exist
     */
    void deleteProduct(Long id, String username);
}
