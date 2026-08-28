package org.arrinna.bilibilimockbackground.esdao;

import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserEsDao extends ElasticsearchRepository<UserEsDoc, Long> {
}
