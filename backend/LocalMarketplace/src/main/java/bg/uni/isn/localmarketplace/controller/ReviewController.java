package bg.uni.isn.localmarketplace.controller;

import bg.uni.isn.localmarketplace.dto.input.review.UpdateReviewDTO;
import bg.uni.isn.localmarketplace.dto.output.review.ReviewDetailsDTO;
import bg.uni.isn.localmarketplace.response.ValidationErrorResponse;
import bg.uni.isn.localmarketplace.service.contract.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Management", description = "Endpoints for handling product reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get a single review", description = "Retrieves the details of a specific product review by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReviewDetailsDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing auth credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Review not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDetailsDTO> getReviewById(
        @Parameter(description = "ID of the review", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @Operation(summary = "Update review information", description = "Updates an existing review text and rating. Only the author can modify it.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Review updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid update data",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing auth credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "User tried updating someone else's review",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Review not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateReview(
        @Parameter(description = "ID of the review to update", required = true) @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Review update data", required = true, content = @Content)
        @Valid @RequestBody UpdateReviewDTO dto) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reviewService.updateReview(id, dto, username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a product review", description = "Removes a review from the product. Only the author can delete it.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing auth credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "User tried deleting someone else's review",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Review not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
        @Parameter(description = "ID of the review to delete", required = true) @PathVariable Long id) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reviewService.deleteReview(id, username);
        return ResponseEntity.noContent().build();
    }
}
