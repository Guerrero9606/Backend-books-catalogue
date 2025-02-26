package com.unir.catalogue.data.elasticsearch;

import com.unir.catalogue.data.model.Book;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookElasticRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    // Método para obtener todos los libros sin filtros
    public List<Book> getBooks() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .build();
        SearchHits<Book> searchHits = elasticsearchOperations.search(searchQuery, Book.class);
        return searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    // Método para búsqueda filtrada sin facetas
    public List<Book> search(String title, String author, Boolean visible, String isbn, Double price) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(title)) {
            query.must(QueryBuilders.matchQuery("title", title));
        }
        if (StringUtils.isNotBlank(author)) {
            query.must(QueryBuilders.matchQuery("author", author));
        }
        if (visible != null) {
            query.filter(QueryBuilders.termQuery("visible", visible));
        }
        if (StringUtils.isNotBlank(isbn)) {
            query.must(QueryBuilders.matchQuery("isbn", isbn));
        }
        if (price != null) {
            query.filter(QueryBuilders.termQuery("price", price));
        }
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(query)
                .build();
        SearchHits<Book> searchHits = elasticsearchOperations.search(searchQuery, Book.class);
        return searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    // Método para búsqueda con facetas (aggregations)
    public FacetResult searchBooksWithFacets(String title, String author, Boolean visible, String isbn, Double price) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(title)) {
            query.must(QueryBuilders.matchQuery("title", title));
        }
        if (StringUtils.isNotBlank(author)) {
            query.must(QueryBuilders.matchQuery("author", author));
        }
        if (visible != null) {
            query.filter(QueryBuilders.termQuery("visible", visible));
        }
        if (StringUtils.isNotBlank(isbn)) {
            query.must(QueryBuilders.matchQuery("isbn", isbn));
        }
        if (price != null) {
            query.filter(QueryBuilders.termQuery("price", price));
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(query)
                // Agregación para agrupar por categoría (se asume que en el mapping existe "category.keyword")
                .addAggregation(AggregationBuilders.terms("categoryFacet").field("category.keyword"))
                // Agregación para agrupar por autor
                .addAggregation(AggregationBuilders.terms("authorFacet").field("author.keyword"))
                // Agregación para rangos de precio
                .addAggregation(AggregationBuilders.range("priceRangeFacet")
                        .field("price")
                        .addRange(0, 10)
                        .addRange(10, 20)
                        .addRange(20, 50))
                .build();

        SearchHits<Book> searchHits = elasticsearchOperations.search(searchQuery, Book.class);
        Aggregations aggregations = searchHits.getAggregations();

        Map<String, Long> categoryFacets = new HashMap<>();
        Terms categoryTerms = aggregations.get("categoryFacet");
        if (categoryTerms != null) {
            categoryFacets = categoryTerms.getBuckets()
                    .stream()
                    .collect(Collectors.toMap(
                            bucket -> bucket.getKeyAsString(),
                            bucket -> bucket.getDocCount()
                    ));
        }

        Map<String, Long> authorFacets = new HashMap<>();
        Terms authorTerms = aggregations.get("authorFacet");
        if (authorTerms != null) {
            authorFacets = authorTerms.getBuckets()
                    .stream()
                    .collect(Collectors.toMap(
                            bucket -> bucket.getKeyAsString(),
                            bucket -> bucket.getDocCount()
                    ));
        }

        Map<String, Long> priceRangeFacets = new HashMap<>();
        Range priceRange = aggregations.get("priceRangeFacet");
        if (priceRange != null) {
            priceRangeFacets = priceRange.getBuckets()
                    .stream()
                    .collect(Collectors.toMap(
                            bucket -> bucket.getKeyAsString(),
                            bucket -> bucket.getDocCount()
                    ));
        }

        FacetResult result = new FacetResult();
        result.setCategoryFacets(categoryFacets);
        result.setAuthorFacets(authorFacets);
        result.setPriceRangeFacets(priceRangeFacets);
        result.setTotalResults(searchHits.getTotalHits());
        return result;
    }

    // Método para buscar por ID
    public Optional<Book> findById(Long id) {
        Book book = elasticsearchOperations.get(id.toString(), Book.class);
        return Optional.ofNullable(book);
    }

    // Método para guardar (indexar) un libro en Elasticsearch
    public Book save(Book book) {
        return elasticsearchOperations.save(book);
    }

    // Método para eliminar un libro de Elasticsearch
    public void delete(Book book) {
        elasticsearchOperations.delete(book);
    }
}
