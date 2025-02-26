package com.unir.catalogue.data.elasticsearch;

import com.unir.catalogue.data.model.Book;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookSearchRepository extends ElasticsearchRepository<Book, Long> {

    // Métodos derivados básicos
    List<Book> findByTitle(String title);

    List<Book> findByAuthor(String author);

    List<Book> findByVisible(Boolean visible);

    List<Book> findByTitleAndAuthor(String title, String author);

    List<Book> findByIsbn(String isbn);

    List<Book> findByPrice(Double price);

    List<Book> findByPriceGreaterThan(Double price);

    List<Book> findByPriceLessThan(Double price);

    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);

    // Método de búsqueda compuesta similar al search en JPA.
    // Nota: Este ejemplo utiliza una consulta DSL JSON básica.
    // Dependiendo de tus requerimientos, podrías necesitar ajustar el query para manejar parámetros opcionales.
    @Query("{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      { \"wildcard\": { \"title\": \"*?0*\" }},\n" +
            "      { \"wildcard\": { \"author\": \"*?1*\" }}\n" +
            "    ],\n" +
            "    \"filter\": [\n" +
            "      { \"term\": { \"visible\": \"?2\" }},\n" +
            "      { \"term\": { \"isbn\": \"?3\" }},\n" +
            "      { \"term\": { \"price\": \"?4\" }}\n" +
            "    ]\n" +
            "  }\n" +
            "}")
    List<Book> search(String title, String author, Boolean visible, String isbn, Double price);
}
