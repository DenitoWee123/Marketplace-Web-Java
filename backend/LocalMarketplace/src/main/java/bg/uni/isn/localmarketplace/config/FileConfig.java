package bg.uni.isn.localmarketplace.config;

import bg.uni.isn.localmarketplace.utils.FileServiceRoot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileConfig {

    @Bean
    public FileServiceRoot fileServiceRoot() {
        Path path = Paths.get("uploads");
        return new FileServiceRoot(path);
    }
}
