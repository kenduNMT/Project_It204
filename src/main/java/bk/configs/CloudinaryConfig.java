package bk.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    private static final String CLOUD_NAME = "drp8l4vuc";
    private static final String API_KEY = "184518233613941";
    private static final String API_SECRET = "W3lNmp5nDtgzazaTTxvZBy5m9Tk";

    @Bean
    public Cloudinary cloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET));
    }
}