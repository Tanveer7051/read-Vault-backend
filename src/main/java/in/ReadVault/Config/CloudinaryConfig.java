package in.ReadVault.Config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dvm5udmdz");
        config.put("api_key", "872983257814465");
        config.put("api_secret", "F45h1nAlEMXK9Ea2YYj_Tt46k5c");

        return new Cloudinary(config);
    }
}