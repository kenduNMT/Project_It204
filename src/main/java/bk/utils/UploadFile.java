package bk.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class UploadFile {
    @Autowired
    private Cloudinary cloudinary;
    // phuong thuc xu li upload
    public String uploadFileToCloudinary(MultipartFile file)  {

        try {
            // Sử dụng phương thức upload để gửi file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            // Lấy ra đường dẫn truy cập
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isEmpty()) {
                throw new IOException("Secure URL not found in Cloudinary upload response");
            }
            return secureUrl;
        } catch (IOException e) {
            // Log lỗi hoặc xử lý ngoại lệ theo ý của bạn
            e.printStackTrace();
        }
        return null; // Trả về null hoặc xử lý lỗi theo ý bạn
    }
}