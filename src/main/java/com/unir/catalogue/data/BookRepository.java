package com.unir.catalogue.data;

import com.unir.catalogue.data.utils.Consts;
import com.unir.catalogue.data.utils.SearchCriteria;
import com.unir.catalogue.data.utils.SearchOperation;
import com.unir.catalogue.data.utils.SearchStatement;
import com.unir.catalogue.data.model.Book;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final BookJpaRepository repository;

    public List<Book> getBooks() {
        return repository.findAll();
    }

    public Book getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Book save(Book book) {
        return repository.save(book);
    }

    public void delete(Book book) {
        repository.delete(book);
    }

    public List<Book> search(String title, String author, Boolean visible, String isbn) {
        SearchCriteria<Book> spec = new SearchCriteria<>();

        if (StringUtils.isNotBlank(title)) {
            spec.add(new SearchStatement(Consts.TITLE, title, SearchOperation.MATCH));
        }

        if (StringUtils.isNotBlank(author)) {
            spec.add(new SearchStatement(Consts.AUTHOR, author, SearchOperation.EQUAL));
        }

        if (visible != null) {
            spec.add(new SearchStatement(Consts.VISIBLE, visible, SearchOperation.EQUAL));
        }

        if (StringUtils.isNotBlank(isbn)) {
            spec.add(new SearchStatement(Consts.ISBN, isbn, SearchOperation.MATCH));
        }

        return repository.findAll(spec);
    }
}
