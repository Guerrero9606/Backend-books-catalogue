package com.unir.catalogue.data.elasticsearch;

import java.util.List;

public interface BookSearchRepositoryCustom {
    FacetResult searchBooksWithFacets(String title, String author, Boolean visible, String isbn, Double price);
}
