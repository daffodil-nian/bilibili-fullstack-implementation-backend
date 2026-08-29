package org.arrinna.bilibilimockbackground.esdao.article;

import org.arrinna.bilibilimockbackground.domain.esdoc.ColumnEsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ColumnEsDao extends ElasticsearchRepository<ColumnEsDoc,Long> {

}
