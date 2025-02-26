package com.unir.catalogue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication()
@EnableJpaRepositories(basePackages = "com.unir.catalogue.data.jpa")
@EnableElasticsearchRepositories(basePackages = "com.unir.catalogue.data.elasticsearch")
public class BooksApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooksApplication.class, args);
	}

}