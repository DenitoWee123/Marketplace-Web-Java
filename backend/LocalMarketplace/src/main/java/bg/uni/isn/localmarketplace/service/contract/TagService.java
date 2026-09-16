package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.product.CreateTagDTO;
import bg.uni.isn.localmarketplace.dto.input.product.UpdateProductTagsDTO;
import bg.uni.isn.localmarketplace.dto.output.product.*;
import org.springframework.data.domain.*;

public interface TagService {
    Page<TagDTO> list(Pageable pageable);
    TagDTO create(CreateTagDTO dto, String username);
    ProductDetailsDTO replaceProductTags(Long productId, UpdateProductTagsDTO dto, String username);
}
