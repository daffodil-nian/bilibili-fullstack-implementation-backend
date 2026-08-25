package org.arrinna.bilibilimockbackground.controller;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.arrinna.bilibilimockbackground.service.ISearchService;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private IUserService userService;
    @Resource
    private ISearchService searchService;

    public Result<SearchVO> searchUser(){
        //1.首先是搜索用户

        return null;
    }

}
