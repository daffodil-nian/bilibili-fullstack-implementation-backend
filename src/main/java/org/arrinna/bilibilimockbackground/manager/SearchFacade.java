package org.arrinna.bilibilimockbackground.manager;

import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.dto.search.SearchRequest;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@Slf4j
public class SearchFacade {

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest){
        return null;
    }
}
