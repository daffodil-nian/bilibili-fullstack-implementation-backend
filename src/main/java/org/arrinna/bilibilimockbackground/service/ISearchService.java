package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.SearchEsReq;

public interface ISearchService {
    SearchVO searchUser(SearchEsReq req);
}
