package com.unir.catalogue.service;

import java.util.List;

import com.unir.catalogue.data.model.Book;
import com.unir.catalogue.controller.model.BookDto;
import com.unir.catalogue.controller.model.CreateBookRequest;

public interface BooksService {

	List<Book> getBooks(String title, String author, Boolean visible, String isbn);

	Book getBook(String bookId);

	Boolean removeBook(String bookId);

	Book createBook(CreateBookRequest request);

	Book updateBook(String bookId, String updateRequest);

	Book updateBook(String bookId, BookDto updateRequest);
}
