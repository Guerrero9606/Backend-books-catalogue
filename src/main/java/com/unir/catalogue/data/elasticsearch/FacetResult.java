package com.unir.catalogue.data.elasticsearch;

import lombok.Data;
import java.util.Map;

@Data
public class FacetResult {
    private Map<String, Long> categoryFacets;
    private Map<String, Long> authorFacets;
    private Map<String, Long> priceRangeFacets;
    private long totalResults;
}