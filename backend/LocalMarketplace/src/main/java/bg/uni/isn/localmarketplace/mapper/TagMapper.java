package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.Tag;
import bg.uni.isn.localmarketplace.dto.output.product.TagDTO;

public final class TagMapper {
    private TagMapper() {}
    public static TagDTO toDto(Tag tag) { return new TagDTO(tag.getId(), tag.getName()); }
}
