package com.smartgig.project.service.impl;

import com.smartgig.project.document.ProjectDocument;
import com.smartgig.project.dto.request.ProjectSearchRequest;
import com.smartgig.project.service.ProjectSearchService;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectSearchServiceImpl implements ProjectSearchService {
    private final ElasticsearchOperations operations;

    @Override
    public Page<ProjectDocument> searchProjects(ProjectSearchRequest request) {
        int page = Math.max(request.getPage(), 0);
        int size = Math.min(Math.max(request.getSize(), 1), 100);
        PageRequest pageable = PageRequest.of(page, size);

        String status = "OPEN";

        BoolQuery.Builder bool = new BoolQuery.Builder();
        bool.must(MatchQuery.of(m -> m.field("status").query(status))._toQuery());

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            bool.must(MultiMatchQuery.of(m -> m.query(request.getKeyword()).fields("title^3", "description^1"))._toQuery());
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            bool.filter(TermQuery.of(t -> t.field("category").value(request.getCategory()))._toQuery());
        }

        if (request.getComplexityLevel() != null && !request.getComplexityLevel().isBlank()) {
            bool.filter(TermQuery.of(t -> t.field("complexityLevel").value(request.getComplexityLevel()))._toQuery());
        }

        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<FieldValue> values = request.getSkills().stream().map(FieldValue::of).toList();
            bool.filter(TermsQuery.of(t -> t.field("skillNames").terms(tv -> tv.value(values)))._toQuery());
        }

        if (request.getMinBudget() != null) {
            bool.filter(RangeQuery.of(r -> r.field("budgetMin").gte(JsonData.of(request.getMinBudget())))._toQuery());
        }

        if (request.getMaxBudget() != null) {
            bool.filter(RangeQuery.of(r -> r.field("budgetMax").lte(JsonData.of(request.getMaxBudget())))._toQuery());
        }

        if (Boolean.TRUE.equals(request.getOnlyActive())) {
            bool.filter(RangeQuery.of(r -> r.field("deadline").gte(JsonData.of(LocalDateTime.now(ZoneOffset.UTC))))._toQuery());
        }

        Query queryDsl = Query.of(q -> q.bool(bool.build()));

        String sortBy = request.getSortBy() == null ? "" : request.getSortBy();
        boolean hasKeyword = request.getKeyword() != null && !request.getKeyword().isBlank();

        var qb = NativeQuery.builder().withQuery(queryDsl).withPageable(pageable);
        if (hasKeyword && (sortBy.isBlank() || sortBy.equalsIgnoreCase("relevance"))) {
            qb.withSort(Sort.by(Sort.Order.desc("_score")));
        } else if (sortBy.equalsIgnoreCase("budget_asc")) {
            qb.withSort(Sort.by(Sort.Order.asc("budgetMin")));
        } else if (sortBy.equalsIgnoreCase("budget_desc")) {
            qb.withSort(Sort.by(Sort.Order.desc("budgetMax")));
        } else {
            qb.withSort(Sort.by(Sort.Order.desc("isFeatured"), Sort.Order.desc("createdAt")));
        }

        NativeQuery query = qb.build();
        SearchHits<ProjectDocument> hits = operations.search(query, ProjectDocument.class);
        List<ProjectDocument> content = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }
}

