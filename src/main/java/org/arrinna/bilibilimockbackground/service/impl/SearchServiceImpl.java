package org.arrinna.bilibilimockbackground.service.impl;

import org.arrinna.bilibilimockbackground.domain.enums.SearchTypeEnum;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.SearchEsReq;

public class SearchServiceImpl {
    //todo 完善搜索关键字查看用户的功能
    //todo 用elasticSearch实现模糊查询
    //todo 数据库的模糊查询只能查连续的字符串匹配，不符合要求

    public SearchVO searchUser(SearchEsReq searchEsReq){

        SearchTypeEnum searchTypeEnum=SearchTypeEnum.of(searchEsReq.getSortCode());

        //然后分类讨论看看是啥
        switch(searchTypeEnum){
            case VIDEO->{
                //todo 查看视频
            }
            case USER -> {
                //todo 查看用户
                //调用es
            }
            case POST -> {
                //todo 查看帖子
            }
            case ANIME -> {
                //todo 查看番剧
            }
            case LIVE->{
                //todo 查看直播
            }
            case ARTICLE -> {
                //todo 查看文章
            }
        }

        return null;
    }
}
