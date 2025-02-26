package com.unir.catalogue.data.elasticsearch;

import com.unir.catalogue.data.model.Book;
import lombok.Data;
import java.util.List;

@Data
public class SearchResponse {
    private List<Book> books;
    private FacetResult facets;
}
