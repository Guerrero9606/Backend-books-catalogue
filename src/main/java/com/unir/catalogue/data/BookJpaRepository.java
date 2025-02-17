package com.unir.catalogue.data;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.unir.catalogue.data.model.Book;

public interface BookJpaRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

	List<Book> findByTitle(String title);

	List<Book> findByAuthor(String author);

	List<Book> findByVisible(Boolean visible);

	List<Book> findByTitleAndAuthor(String title, String author);

	List<Book> findByIsbn(String isbn);
}
