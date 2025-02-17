package com.unir.catalogue.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookRequest {

	// Título del libro
	private String title;

	// Autor del libro
	private String author;

	// Fecha de publicación del libro
	private LocalDate publication_Date;

	// Categoría del libro (por ejemplo: "Ficción", "No Ficción", "Ciencia", etc.)
	private String category;

	// Código ISBN único del libro
	private String isbn;

	// Valoración del libro (nota de 1 a 5)
	private Integer rating;

	// Indica si el libro es visible en el catálogo
	private Boolean visible;
}

