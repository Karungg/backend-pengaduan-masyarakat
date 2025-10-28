package com.miftah.pengaduan_masyarakat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PengaduanMasyarakatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PengaduanMasyarakatApplication.class, args);
	}

}
