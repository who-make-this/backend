package university.likelion.wmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class WmtApplication {

    public static void main(String[] args) {
        System.out.println("!!");
        SpringApplication.run(WmtApplication.class, args);
    }

}
