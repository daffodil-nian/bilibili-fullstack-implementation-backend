package org.arrinna.bilibilimockbackground.esdao.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserEsSearch {

    private final ElasticsearchOperations operations;


    public Page<UserEsDoc> searchNickname(String keyword,Pageable pageable){
        if(!StringUtils.hasText(keyword)){
            return Page.empty(pageable);
        }
        List<String> chars = splitUniqueChars(keyword.trim());
        //然后可以开始搜索匹配了
         Query query =Query.of(q->q.bool(b->{
             for (String c:chars){
                 //一个一个遍历，要求都要包含
                 b.must(m->m.match(t->t.field("nickname").query(c)));
             }
             return b;
         }));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();

        SearchHits<UserEsDoc> hits = operations.search(nativeQuery, UserEsDoc.class);

        List<UserEsDoc> list = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(list, pageable, hits.getTotalHits());//然后分页返回结果

    }

    private List<String> splitUniqueChars(String keyword) {
        Set<String> set = new LinkedHashSet<>();
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            if (!Character.isWhitespace(c)) {
                set.add(String.valueOf(c));
            }
        }
        return new ArrayList<>(set);
    }
}
