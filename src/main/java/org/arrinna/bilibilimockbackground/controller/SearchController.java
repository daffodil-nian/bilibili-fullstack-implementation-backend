package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.SearchEsReq;
import org.arrinna.bilibilimockbackground.service.ISearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.arrinna.bilibilimockbackground.common.constant.DefaultConstant.SEARCH_SUCCESS_MSG;

@RestController
@RequestMapping("/api/search")
@Slf4j
public class SearchController {

    @Resource
    private ISearchService searchService;

    @PostMapping("/all")
    public Result<SearchVO> searchUser(@RequestBody SearchEsReq req) {

        return Result.Success(searchService.search(req), SEARCH_SUCCESS_MSG);
    }
}
