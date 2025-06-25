package bk.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class UploadFile {

    private static final Logger logger = LoggerFactory.getLogger(UploadFile.class);

    @Autowired
    private Cloudinary cloudinary;

    // Danh sách các loại file được phép upload
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");
    private final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // Kích thước file tối đa (5MB)
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Upload file CV lên Cloudinary
     * @param file File cần upload
     * @return URL của file đã upload
     * @throws IOException
     */
    public String uploadCVFile(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        try {
            // Cấu hình upload options cho CV
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "cv_files", // Thư mục chứa CV
                    "resource_type", "raw", // Upload as raw file (không phải image)
                    "public_id", generatePublicId(file.getOriginalFilename()), // Tên file
                    "use_filename", true,
                    "unique_filename", true
            );

            // Upload file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            // Lấy ra đường dẫn truy cập
            String secureUrl = (String) uploadResult.get("secure_url");

            if (secureUrl == null || secureUrl.isEmpty()) {
                throw new IOException("Secure URL not found in Cloudinary upload response");
            }

            logger.info("File uploaded successfully: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            logger.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Upload file ảnh (avatar, logo công ty...)
     * @param file File ảnh cần upload
     * @return URL của ảnh đã upload
     * @throws IOException
     */
    public String uploadImageFile(MultipartFile file) throws IOException {
        validateImageFile(file);

        try {
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "images",
                    "transformation", Arrays.asList(
                            ObjectUtils.asMap("width", 500, "height", 500, "crop", "limit"),
                            ObjectUtils.asMap("quality", "auto"),
                            ObjectUtils.asMap("format", "auto")
                    )
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            String secureUrl = (String) uploadResult.get("secure_url");

            if (secureUrl == null || secureUrl.isEmpty()) {
                throw new IOException("Secure URL not found in Cloudinary upload response");
            }

            logger.info("Image uploaded successfully: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            logger.error("Error uploading image to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Xóa file từ Cloudinary
     * @param publicId ID của file cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteFile(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");
            boolean success = "ok".equals(status);

            if (success) {
                logger.info("File deleted successfully: {}", publicId);
            } else {
                logger.warn("Failed to delete file: {}, result: {}", publicId, status);
            }

            return success;
        } catch (IOException e) {
            logger.error("Error deleting file from Cloudinary: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate file CV
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Invalid filename");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("File type not allowed. Only PDF, DOC, DOCX files are supported");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IOException("Invalid file content type");
        }
    }

    /**
     * Validate file ảnh
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }
    }

    /**
     * Tạo public ID cho file
     */
    private String generatePublicId(String originalFilename) {
        String nameWithoutExtension = removeFileExtension(originalFilename);
        return nameWithoutExtension + "_" + System.currentTimeMillis();
    }

    /**
     * Lấy extension của file
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Xóa extension khỏi tên file
     */
    private String removeFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }
        return filename.substring(0, lastDotIndex);
    }

    /**
     * Lấy public ID từ Cloudinary URL
     */
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }

        try {
            // URL format: https://res.cloudinary.com/[cloud_name]/[resource_type]/upload/[public_id].[format]
            String[] parts = cloudinaryUrl.split("/");
            if (parts.length >= 2) {
                String lastPart = parts[parts.length - 1];
                // Remove file extension
                return removeFileExtension(lastPart);
            }
        } catch (Exception e) {
            logger.error("Error extracting public ID from URL: {}", e.getMessage());
        }

        return null;
    }
}