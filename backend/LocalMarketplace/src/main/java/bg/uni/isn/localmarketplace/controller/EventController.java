package bg.uni.isn.localmarketplace.controller;

import bg.uni.isn.localmarketplace.dto.input.event.CreateEventDTO;
import bg.uni.isn.localmarketplace.dto.input.event.UpdateEventDTO;
import bg.uni.isn.localmarketplace.dto.output.event.EventDetailsDTO;
import bg.uni.isn.localmarketplace.response.ValidationErrorResponse;
import bg.uni.isn.localmarketplace.service.contract.EventService;
import bg.uni.isn.localmarketplace.vo.EventType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Endpoints for managing community events (craft fairs, promotions, stories)")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "List events",
        description = "Returns a paginated list of all events. "
            + "Optionally filter by ?type= (CRAFT_FAIRS, PROMOTIONAL_CAMPAIGNS, STORYTELLING_FEATURES) "
            + "and/or ?active= (true/false) and/or upcoming. Both filters may be combined.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    })
    public ResponseEntity<Page<EventDetailsDTO>> getAllEvents(

        @RequestParam(name = "types", required = false)
        List<EventType> types,

        @RequestParam(required = false)
        Boolean active,

        @RequestParam(required = false)
        Boolean upcoming,

        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
            eventService.getAllEvents(types, active, upcoming, pageable)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Returns details for a single event.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<EventDetailsDTO> getEvent(
        @Parameter(description = "Event ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PostMapping
    @Operation(summary = "Create event",
        description = "Creates a new community event. Type-specific fields: CRAFT_FAIRS needs location + dates; "
            + "PROMOTIONAL_CAMPAIGNS needs discountType + discountValue + dates; STORYTELLING_FEATURES needs content.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<EventDetailsDTO> createEvent(
        @Valid @RequestBody CreateEventDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventService.createEvent(principal.getName(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event",
        description = "Updates an existing event. Only the owner may update. Null fields are ignored.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — not the event owner",
            content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<EventDetailsDTO> updateEvent(
        @Parameter(description = "Event ID", required = true) @PathVariable Long id,
        @Valid @RequestBody UpdateEventDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(eventService.updateEvent(id, principal.getName(), dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event", description = "Deletes an event. Only the owner may delete.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden — not the event owner",
            content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Void> deleteEvent(
        @Parameter(description = "Event ID", required = true) @PathVariable Long id,
        @Parameter(hidden = true) Principal principal) {
        eventService.deleteEvent(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
