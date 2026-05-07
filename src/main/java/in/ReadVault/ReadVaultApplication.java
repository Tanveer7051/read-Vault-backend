package in.ReadVault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReadVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadVaultApplication.class, args);
	}

}
