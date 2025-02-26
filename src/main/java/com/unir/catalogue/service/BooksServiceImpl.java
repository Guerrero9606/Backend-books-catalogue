package com.unir.catalogue.service;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.unir.catalogue.data.elasticsearch.BookElasticRepository;
import com.unir.catalogue.data.jpa.BookRepository;
import com.unir.catalogue.data.elasticsearch.BookSearchRepository;
import com.unir.catalogue.controller.model.BookDto;
import com.unir.catalogue.controller.model.CreateBookRequest;
import com.unir.catalogue.data.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class BooksServiceImpl implements BooksService {

	@Autowired
	private BookRepository repository;

	// Repositorio para la indexación en Elasticsearch
	@Autowired
	private BookElasticRepository searchRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public List<Book> getBooks(String title, String author, Boolean visible, String isbn, Double price) {
		if (StringUtils.hasLength(title) || StringUtils.hasLength(author)
				|| visible != null || StringUtils.hasLength(isbn) || Objects.nonNull(price)) {
			return repository.search(title, author, visible, isbn, price);
		}
		List<Book> books = repository.getBooks();
		return books.isEmpty() ? null : books;
	}

	@Override
	public Book getBook(String bookId) {
		return repository.getById(Long.valueOf(bookId));
	}

	@Override
	public Boolean removeBook(String bookId) {
		Book book = repository.getById(Long.valueOf(bookId));
		if (book != null) {
			repository.delete(book);
			searchRepository.delete(book); // Elimina del índice de Elasticsearch
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Book createBook(CreateBookRequest request) {
		if (request != null && StringUtils.hasLength(request.getTitle().trim())
				&& StringUtils.hasLength(request.getAuthor().trim()) && request.getVisible() != null) {

			Book book = Book.builder()
					.title(request.getTitle())
					.author(request.getAuthor())
					.publication_Date(request.getPublication_Date())
					.category(request.getCategory())
					.isbn(request.getIsbn())
					.rating(request.getRating())
					.visible(request.getVisible())
					.price(request.getPrice())
					.url(request.getUrl())
					.build();

			Book savedBook = repository.save(book);
			searchRepository.save(savedBook); // Indexa el libro en Elasticsearch
			return savedBook;
		} else {
			return null;
		}
	}

	@Override
	public Book updateBook(String bookId, String updateRequest) {
		Book book = repository.getById(Long.valueOf(bookId));
		if (book != null) {
			try {
				JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(updateRequest));
				JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(book)));
				Book patched = objectMapper.treeToValue(target, Book.class);
				Book savedBook = repository.save(patched);
				searchRepository.save(savedBook); // Actualiza el índice de Elasticsearch
				return savedBook;
			} catch (JsonProcessingException | JsonPatchException e) {
				log.error("Error updating book {}", bookId, e);
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public Book updateBook(String bookId, BookDto updateRequest) {
		Book book = repository.getById(Long.valueOf(bookId));
		if (book != null) {
			book.update(updateRequest);
			Book savedBook = repository.save(book);
			searchRepository.save(savedBook); // Sincroniza la actualización en Elasticsearch
			return savedBook;
		} else {
			return null;
		}
	}
}
