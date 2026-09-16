package bg.uni.isn.localmarketplace.service;

import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.dto.input.product.CreateProductDTO;
import bg.uni.isn.localmarketplace.dto.input.product.UpdateProductDTO;
import bg.uni.isn.localmarketplace.dto.output.product.ProductDetailsDTO;
import bg.uni.isn.localmarketplace.exception.file.InvalidFileFormatException;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotBelongToUserExeption;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.repository.ProductRepository;
import bg.uni.isn.localmarketplace.repository.UserRepository;
import bg.uni.isn.localmarketplace.service.contract.FileService;
import bg.uni.isn.localmarketplace.service.contract.ProductService;
import bg.uni.isn.localmarketplace.utils.ValidationUtils;
import bg.uni.isn.localmarketplace.vo.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository,
                              FileService fileService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    @Override
    public ProductDetailsDTO getProduct(Long id) {
        return ProductDetailsDTO.from(getProductById(id));
    }

    @Override
    public Page<ProductDetailsDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductDetailsDTO::from);
    }

    @Override
    public Page<ProductDetailsDTO> getProductsWithFilters(
        List<ProductType> productTypes, String makerUsername, Boolean inStock, Pageable pageable) {

        if (productTypes != null && productTypes.isEmpty()) {
            productTypes = null;
        }

        String sanitizedUsername =
            (makerUsername != null && !makerUsername.isBlank())
                ? makerUsername
                : null;

        Page<Product> page = productRepository.findProductsWithFilters(productTypes, sanitizedUsername,
            inStock,
            pageable
        );

        return page.map(ProductDetailsDTO::from);
    }

    @Override
    @Transactional
    public void setProductPicture(Long productId, String username, MultipartFile picture) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductDoesNotExistException("Product not found: " + productId));

        User user = getUser(username);
        checkIfTheMakerOfTheProductIsTheSame(product.getMaker(), user, productId);

        if (picture == null || picture.isEmpty()) {
            throw new IllegalArgumentException("Missing picture file");
        }

        if (!ValidationUtils.isJpgFile(picture)) {
            throw new InvalidFileFormatException("Only JPG files are allowed");
        }

        String filename = fileService.saveOrReplaceJpg(
            picture,
            Path.of("products"),
            String.valueOf(productId)
        );

        product.setProductPicturePath(Path.of("products", filename).toString().replace("\\", "/"));
        productRepository.save(product);
    }

    @Override
    public ProductDetailsDTO createProduct(CreateProductDTO dto, String username) {
        User user = getUser(username);
        Product product =
            new Product(dto.productType(), dto.name(), dto.description(), dto.price(), user, dto.quantity());

        productRepository.save(product);
        return ProductDetailsDTO.from(product);
    }

    @Override
    public ProductDetailsDTO updateProduct(Long id, String username, UpdateProductDTO dto) {
        User user = getUser(username);
        Product toUpdate = getProductById(id);

        checkIfTheMakerOfTheProductIsTheSame(toUpdate.getMaker(), user, id);

        if (dto.name() != null) {
            toUpdate.setName(dto.name());
        }
        toUpdate.setDescription(dto.description());
        toUpdate.setPrice(dto.price());
        toUpdate.setQuantity(dto.quantity());

        return ProductDetailsDTO.from(toUpdate);
    }

    @Override
    public void deleteProduct(Long id, String username) {
        Product toDelete = getProductById(id);
        User user = getUser(username);

        checkIfTheMakerOfTheProductIsTheSame(toDelete.getMaker(), user, id);

        fileService.deleteFile(Path.of("products", id + ".jpg"));
        productRepository.delete(toDelete);
    }

    private Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return product.get();
        } else {
            throw new ProductDoesNotExistException("Product with id " + id + " does not exist");
        }
    }

    private User getUser(String username) {
        Optional<User> user = userRepository.findById(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserNotFoundException("User with name " + username + " does not exist");
        }
    }

    private void checkIfTheMakerOfTheProductIsTheSame(User first, User second, Long productId) {
        if (!first.getUsername().equals(second.getUsername())) {
            throw new ProductDoesNotBelongToUserExeption("Product with id " + productId + " does not belong to user " +
                "with username " + second.getUsername());
        }
    }
}
