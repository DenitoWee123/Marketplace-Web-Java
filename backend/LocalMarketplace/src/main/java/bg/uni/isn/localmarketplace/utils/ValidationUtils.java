package bg.uni.isn.localmarketplace.utils;

import org.springframework.web.multipart.MultipartFile;

public class ValidationUtils {

    public static boolean isJpgFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        return "image/jpeg".equalsIgnoreCase(contentType)
            || (originalFilename != null && originalFilename.toLowerCase().endsWith(".jpg"))
            || (originalFilename != null && originalFilename.toLowerCase().endsWith(".jpeg"));
    }

    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }

        // позволява само букви, цифри, _ и -
        return filename.matches("^[a-zA-Z0-9_-]+$");
    }
}
