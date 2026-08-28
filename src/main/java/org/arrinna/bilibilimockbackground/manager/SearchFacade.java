package org.arrinna.bilibilimockbackground.manager;

import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.dto.search.SearchDto;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@Slf4j
public class SearchFacade {

    //TODO 这个类等之后开发再完善吧
    public SearchVO searchAll(@RequestBody SearchDto searchRequest){
        //1.集合
        return null;
    }
}
