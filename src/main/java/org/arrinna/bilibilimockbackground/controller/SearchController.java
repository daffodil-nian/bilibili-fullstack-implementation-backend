package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.service.IUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private IUserService userService;

    public Result<Boolean> searchUser(){
        return null;
    }

}
