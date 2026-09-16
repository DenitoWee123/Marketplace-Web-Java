package bg.uni.isn.localmarketplace.service;

import java.util.HashSet;
import java.util.Locale;
import bg.uni.isn.localmarketplace.domain.Tag;
import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.dto.input.product.*;
import bg.uni.isn.localmarketplace.dto.output.product.*;
import bg.uni.isn.localmarketplace.exception.NotFoundException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.mapper.TagMapper;
import bg.uni.isn.localmarketplace.repository.*;
import bg.uni.isn.localmarketplace.service.contract.TagService;
import bg.uni.isn.localmarketplace.vo.UserType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TagServiceImpl implements TagService {
    private final TagRepository tags;
    private final ProductRepository products;
    private final UserRepository users;

    public TagServiceImpl(TagRepository tags, ProductRepository products, UserRepository users) {
        this.tags = tags;
        this.products = products;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<TagDTO> list(Pageable pageable) {
        return tags.findAll(pageable).map(TagMapper::toDto);
    }

    public TagDTO create(CreateTagDTO dto, String username) {
        User user = users.findById(username).orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.isAdmin() && user.getUserType() != UserType.VENDOR) {
            throw new OwnershipMismatchException("Only vendors and admins can create tags");
        }
        String name = dto.name().strip().toLowerCase(Locale.ROOT);
        if (name.isBlank() || name.length() > 50) {
            throw new IllegalArgumentException("Tag name must contain 1 to 50 characters");
        }
        if (tags.existsByName(name)) {
            throw new DataIntegrityViolationException("Tag already exists");
        }
        return TagMapper.toDto(tags.saveAndFlush(new Tag(name)));
    }

    public ProductDetailsDTO replaceProductTags(Long productId, UpdateProductTagsDTO dto, String username) {
        User user = users.findById(username).orElseThrow(() -> new NotFoundException("User not found"));
        var product = products.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        if (!user.isAdmin() && !product.getMaker().getUsername().equals(username)) {
            throw new OwnershipMismatchException("Only the product owner or an admin can change its tags");
        }
        var selected = tags.findAllById(dto.tagIds());
        if (selected.size() != dto.tagIds().size()) {
            throw new NotFoundException("One or more tags do not exist");
        }
        product.getTags().clear();
        product.getTags().addAll(new HashSet<>(selected));
        return ProductDetailsDTO.from(product);
    }
}
