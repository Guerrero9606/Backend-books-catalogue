package com.unir.catalogue.data.model;

import com.unir.catalogue.controller.model.BookDto;
import com.unir.catalogue.data.utils.Consts;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = Consts.TITLE, nullable = false)
	private String title;

	@Column(name = Consts.AUTHOR, nullable = false)
	private String author;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = Consts.PUBLICATION_DATE)
	private LocalDate publication_Date;

	@Column(name = Consts.CATEGORY)
	private String category;

	@Column(name = Consts.ISBN, unique = true, nullable = false)
	private String isbn;

	@Column(name = Consts.RATING)
	private Integer rating;

	@Column(name = Consts.VISIBLE)
	private Boolean visible;

	public void update(BookDto bookDto) {
		this.title = bookDto.getTitle();
		this.author = bookDto.getAuthor();
		this.publication_Date = bookDto.getPublication_Date();
		this.category = bookDto.getCategory();
		this.isbn = bookDto.getIsbn();
		this.rating = bookDto.getRating();
		this.visible = bookDto.getVisible();
	}
}
