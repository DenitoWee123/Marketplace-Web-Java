package bg.uni.isn.localmarketplace.response;

import java.util.Map;

public record ValidationErrorResponse(Map<String, String> errors) {
}