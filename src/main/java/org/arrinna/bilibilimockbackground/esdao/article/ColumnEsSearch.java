package org.arrinna.bilibilimockbackground.esdao.article;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.arrinna.bilibilimockbackground.domain.esdoc.ColumnEsDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.arrinna.bilibilimockbackground.esdao.user.UserEsSearch.splitUniqueChars;

/**
 * TODO 先new一个空的类，后续再补充
 */
@Component
@RequiredArgsConstructor
public class ColumnEsSearch {

    private final ElasticsearchOperations operations;


    public Page<ColumnEsDoc> searchColumnTitle(String keyword, Pageable pageable){
        //1.
        if(!StringUtils.hasText(keyword)){
            return Page.empty(pageable);
        }
        List<String> chars = splitUniqueChars(keyword.trim());
        Query query = Query.of(q->q.bool(b->{
            for (String c:chars)
                //一个一个遍历，要求都要包含
                b.must(m->m.match(t->t.field("title").query(c)));
            return b;
        }
        ));

        NativeQuery nativeQuery= NativeQuery.builder()
        .withQuery(query)
                .withPageable(pageable)
                .build();


        SearchHits<ColumnEsDoc> searchHits = operations.search(nativeQuery, ColumnEsDoc.class);
        List<ColumnEsDoc> list = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(list,pageable,searchHits.getTotalHits());
    }
}
