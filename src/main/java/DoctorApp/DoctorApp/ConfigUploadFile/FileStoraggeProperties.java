package DoctorApp.DoctorApp.ConfigUploadFile;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileStoraggeProperties {
  private  String uploadDir;

}
