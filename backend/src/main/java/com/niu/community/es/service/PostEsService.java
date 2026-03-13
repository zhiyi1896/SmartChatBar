package com.niu.community.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.niu.community.common.config.AppProperties;
import com.niu.community.common.exception.BusinessException;
import com.niu.community.common.model.PageResult;
import com.niu.community.es.entity.PostDocument;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PostEsService {

    private final ElasticsearchClient elasticsearchClient;
    private final AppProperties appProperties;

    public PostEsService(ElasticsearchClient elasticsearchClient, AppProperties appProperties) {
        this.elasticsearchClient = elasticsearchClient;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void initIndex() {
        try {
            boolean exists = elasticsearchClient.indices().exists(builder -> builder.index(indexName())).value();
            if (!exists) {
                elasticsearchClient.indices().create(builder -> builder
                    .index(indexName())
                    .mappings(mb -> mb
                        .properties("id", Property.of(p -> p.long_(lp -> lp)))
                        .properties("userId", Property.of(p -> p.long_(lp -> lp)))
                        .properties("title", Property.of(p -> p.text(tp -> tp)))
                        .properties("content", Property.of(p -> p.text(tp -> tp)))
                        .properties("authorName", Property.of(p -> p.keyword(k -> k)))
                        .properties("likeCount", Property.of(p -> p.integer(ip -> ip)))
                        .properties("commentCount", Property.of(p -> p.integer(ip -> ip)))
                        .properties("viewCount", Property.of(p -> p.integer(ip -> ip)))
                        .properties("createTime", Property.of(p -> p.keyword(k -> k))))
                );
            }
        } catch (IOException ex) {
            throw new BusinessException("初始化 ES 索引失败: " + ex.getMessage());
        }
    }

    public void save(PostDocument document) {
        try {
            IndexRequest<PostDocument> request = IndexRequest.of(builder -> builder
                .index(indexName())
                .id(String.valueOf(document.getId()))
                .document(document)
                .refresh(Refresh.True));
            elasticsearchClient.index(request);
        } catch (IOException ex) {
            throw new BusinessException("同步帖子到 ES 失败: " + ex.getMessage());
        }
    }

    public void delete(Long postId) {
        try {
            elasticsearchClient.delete(DeleteRequest.of(builder -> builder
                .index(indexName())
                .id(String.valueOf(postId))
                .refresh(Refresh.True)));
        } catch (ElasticsearchException ex) {
            if (ex.status() != 404) {
                throw new BusinessException("删除 ES 文档失败: " + ex.getMessage());
            }
        } catch (IOException ex) {
            throw new BusinessException("删除 ES 文档失败: " + ex.getMessage());
        }
    }

    public PageResult<PostDocument> search(String keyword, int page, int pageSize) {
        try {
            SearchRequest request = SearchRequest.of(builder -> builder
                .index(indexName())
                .from((page - 1) * pageSize)
                .size(pageSize)
                .sort(sort -> sort.field(field -> field.field("_score").order(SortOrder.Desc)))
                .sort(sort -> sort.field(field -> field.field("id").order(SortOrder.Desc)))
                .query(query -> {
                    if (!StringUtils.hasText(keyword)) {
                        return query.matchAll(matchAll -> matchAll);
                    }
                    return query.bool(bool -> bool
                        .should(should -> should.match(match -> match.field("title").query(keyword)))
                        .should(should -> should.match(match -> match.field("content").query(keyword)))
                        .minimumShouldMatch("1"));
                })
                .highlight(highlight -> highlight
                    .fields("title", HighlightField.of(field -> field))
                    .fields("content", HighlightField.of(field -> field))
                    .preTags("<em>")
                    .postTags("</em>")));

            SearchResponse<PostDocument> response = elasticsearchClient.search(request, PostDocument.class);
            List<PostDocument> list = response.hits().hits().stream().map(this::mapHit).toList();
            long total = response.hits().total() == null ? list.size() : response.hits().total().value();
            return new PageResult<>(list, total, page, pageSize);
        } catch (IOException ex) {
            throw new BusinessException("搜索帖子失败: " + ex.getMessage());
        }
    }

    public void syncBatch(List<PostDocument> documents) {
        for (PostDocument document : documents) {
            save(document);
        }
    }

    private PostDocument mapHit(Hit<PostDocument> hit) {
        PostDocument source = hit.source();
        if (source == null) {
            return new PostDocument();
        }
        Map<String, List<String>> highlight = hit.highlight() == null ? new HashMap<>() : hit.highlight();
        if (highlight.containsKey("title") && !highlight.get("title").isEmpty()) {
            source.setTitle(String.join("", highlight.get("title")));
        }
        if (highlight.containsKey("content") && !highlight.get("content").isEmpty()) {
            source.setContent(String.join("", highlight.get("content")));
        }
        return source;
    }

    private String indexName() {
        return appProperties.getEsIndexName();
    }
}
