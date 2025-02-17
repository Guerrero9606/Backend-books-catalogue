package com.unir.catalogue.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.unir.catalogue.data.BookRepository;
import com.unir.catalogue.controller.model.BookDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.unir.catalogue.data.model.Book;
import com.unir.catalogue.controller.model.CreateBookRequest;

@Service
@Slf4j
public class BooksServiceImpl implements BooksService {

	@Autowired
	private BookRepository repository;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public List<Book> getBooks(String title, String author, Boolean visible, String isbn) {

		if (StringUtils.hasLength(title) || StringUtils.hasLength(author) || visible != null || StringUtils.hasLength(isbn)) {
			return repository.search(title, author, visible, isbn);
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
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
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
					.build();

			return repository.save(book);
		} else {
			return null;
		}
	}

	@Override
	public Book updateBook(String bookId, String request) {

		Book book = repository.getById(Long.valueOf(bookId));
		if (book != null) {
			try {
				JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
				JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(book)));
				Book patched = objectMapper.treeToValue(target, Book.class);
				repository.save(patched);
				return patched;
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
			repository.save(book);
			return book;
		} else {
			return null;
		}
	}
}
