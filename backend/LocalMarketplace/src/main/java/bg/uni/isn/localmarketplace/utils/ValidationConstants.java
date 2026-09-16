package bg.uni.isn.localmarketplace.utils;

public class ValidationConstants {

    public static class User {
        public static final String BLANK_FIRST_NAME = "First name should not be blank or empty!";
        public static final String BLANK_LAST_NAME = "Last name should not be blank or empty!";
        public static final String BLANK_USERNAME = "Username should not be blank or empty!";
        public static final String BLANK_EMAIL = "Email should not be blank or empty!";
        public static final String BLANK_PASSWORD = "Password should not be blank or empty!";
        public static final String BLANK_PHONE = "Phone number should not be blank or empty!";

        public static final String LENGTH_FIRST_NAME = "First name should be at most 50 characters long!";
        public static final String LENGTH_LAST_NAME = "Last name should be at most 50 characters long!";
        public static final String LENGTH_USERNAME = "Username should be at most 50 characters long!";
        public static final String LENGTH_EMAIL = "Email should be at most 255 characters long!";
        public static final String INVALID_EMAIL = "Invalid email!";
        public static final String LENGTH_PASSWORD = "Password should be between 6 and 50 characters long!";
        public static final String INVALID_PHONE = "Invalid number format! Must start with + and include only number.";
    }

    public static class Product {
        public static final String BLANK_NAME = "Product name should not be blank or empty!";
        public static final String BLANK_DESCRIPTION = "Product description should not be blank or empty!";
        public static final String NULL_TYPE = "Product type should not be null!";

        public static final String MIN_PRICE = "Minimum price should be 0!";
        public static final String MIN_QUANTITY = "Product quantity should be at least 1!";
        public static final String MIN_UPDATE_QUANTITY = "Product quantity should be at least 0!";
        public static final String LENGTH_NAME = "Product name should not be over 100 characters long!";
        public static final String LENGTH_DESCRIPTION = "Product description should not be over 100 characters long!";
    }

    public static class Review {
        public static final String LENGTH_TEXT = "Review text should not be over 500 characters long!";
        public static final String MIN_RATING = "Rating should be minimum 0 including!";
        public static final String MAX_RATING = "Rating should be max 5 including!";
    }

    public static class Cart {
        public static final String NULL_PRODUCT_ID = "Product ID should not be null!";
        public static final String MIN_QUANTITY = "Quantity should be at least 1!";
        public static final String INSUFFICIENT_STOCK = "Requested quantity exceeds available stock!";
        public static final String ITEM_NOT_OWNED = "Cart item does not belong to the requesting user!";
    }

    public static class Event {
        public static final String BLANK_TITLE = "Event title should not be blank or empty!";
        public static final String NULL_TYPE = "Event type should not be null!";
        public static final String LENGTH_TITLE = "Event title should be at most 255 characters long!";
        public static final String LENGTH_DESCRIPTION = "Event description should be at most 1000 characters long!";
        public static final String NOT_OWNED = "Event does not belong to the requesting user!";

        // CRAFT_FAIRS
        public static final String BLANK_LOCATION = "Location should not be blank for a craft fair event!";
        public static final String NULL_START_DATE = "Start date should not be null for a craft fair or promotional campaign!";
        public static final String NULL_END_DATE = "End date should not be null for a craft fair or promotional campaign!";
        public static final String END_DATE_BEFORE_START_DATE = "End date must be after start date!";

        // PROMOTIONAL_CAMPAIGNS
        public static final String NULL_DISCOUNT_TYPE = "Discount type should not be null for a promotional campaign!";
        public static final String NULL_DISCOUNT_VALUE = "Discount value should not be null for a promotional campaign!";
        public static final String MIN_DISCOUNT_VALUE = "Discount value should be at least 0!";

        // STORYTELLING_FEATURES
        public static final String BLANK_CONTENT = "Content should not be blank for a storytelling event!";
    }
    public static class Order {
        public static final String NULL_PAYMENT_METHOD = "Payment method should not be null!";
        public static final String NULL_CURRENCY = "Currency should not be null!";
        public static final String NULL_STATUS = "Order status should not be null!";
        public static final String EMPTY_CART = "Cannot place an order from an empty cart!";
        public static final String INSUFFICIENT_STOCK = "Insufficient stock to fulfil the order!";
        public static final String INVALID_STATUS_TRANSITION = "The requested status transition is not allowed!";
    }

    public static class Payment {
        public static final String ORDER_NOT_IN_PENDING_PAYMENT =
            "Order must be in PENDING_PAYMENT status to proceed with payment!";
    }
}
