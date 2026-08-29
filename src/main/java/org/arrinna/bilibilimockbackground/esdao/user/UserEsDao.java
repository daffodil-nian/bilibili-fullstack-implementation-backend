package org.arrinna.bilibilimockbackground.esdao.user;

import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserEsDao extends ElasticsearchRepository<UserEsDoc, Long> {
}
