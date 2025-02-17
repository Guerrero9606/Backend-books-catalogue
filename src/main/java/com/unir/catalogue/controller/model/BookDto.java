package com.unir.catalogue.controller.model;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BookDto {

	private String title;
	private String author;
	private LocalDate publication_Date;
	private String category;
	private String isbn;
	private Integer rating;
	private Boolean visible;
}
