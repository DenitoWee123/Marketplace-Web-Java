package bg.uni.isn.localmarketplace.domain;

import bg.uni.isn.localmarketplace.vo.DiscountType;
import bg.uni.isn.localmarketplace.vo.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "events")
public class Event {

    // Promotion details
    @Embeddable
    public static class PromotionDetails {
        @Enumerated(EnumType.STRING)
        @Column(name = "discount_type")
        private DiscountType discountType;

        @Column(name = "discount_value")
        private long discountValue;

        public PromotionDetails() {}

        public DiscountType getDiscountType() { return discountType; }
        public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
        public long getDiscountValue() { return discountValue; }
        public void setDiscountValue(long discountValue) { this.discountValue = discountValue; }
    }

    // Fair details
    @Embeddable
    public static class FairDetails {
        @Column(name = "location")
        private String location;

        public FairDetails() {}

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private boolean active;

    // --- Специфично за Stories ---
    @Column(columnDefinition = "TEXT")
    private String content;

    // --- EMBEDDED КЛАСОВЕ (В базата данни ще влязат като нормални колони) ---
    @Embedded
    private PromotionDetails promotionDetails;

    @Embedded
    private FairDetails fairDetails;

    protected Event() {

    }

    public Event(String title, String description, EventType type, User user,
                 LocalDateTime startDate, LocalDateTime endDate, boolean isActive,
                 String content, PromotionDetails promotionDetails, FairDetails fairDetails) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = isActive;
        this.content = content;
        this.promotionDetails = promotionDetails;
        this.fairDetails = fairDetails;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PromotionDetails getPromotionDetails() {
        return promotionDetails;
    }

    public void setPromotionDetails(PromotionDetails promotionDetails) {
        this.promotionDetails = promotionDetails;
    }

    public FairDetails getFairDetails() {
        return fairDetails;
    }

    public void setFairDetails(FairDetails fairDetails) {
        this.fairDetails = fairDetails;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Event event)) return false;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
