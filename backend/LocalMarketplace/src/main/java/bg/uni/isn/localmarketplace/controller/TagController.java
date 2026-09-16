package bg.uni.isn.localmarketplace.controller;

import java.security.Principal;
import bg.uni.isn.localmarketplace.dto.input.product.*;
import bg.uni.isn.localmarketplace.dto.output.product.*;
import bg.uni.isn.localmarketplace.service.contract.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Product tags", description = "Reusable tags shared by multiple products")
public class TagController {
    private final TagService service;
    public TagController(TagService service) { this.service = service; }

    @GetMapping("/tags")
    @Operation(summary = "List available tags")
    public Page<TagDTO> list(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(pageable);
    }

    @PostMapping("/tags")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Create a tag", description = "Vendor/admin only; names are trimmed, lowercased and unique.")
    public ResponseEntity<TagDTO> create(@Valid @RequestBody CreateTagDTO dto, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, principal.getName()));
    }

    @PutMapping("/products/{id}/tags")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Replace a product's tags", description = "Product owner or admin only; all supplied IDs must exist.")
    public ProductDetailsDTO replace(@PathVariable Long id,
        @Valid @RequestBody UpdateProductTagsDTO dto, Principal principal) {
        return service.replaceProductTags(id, dto, principal.getName());
    }
}
