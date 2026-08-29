package org.arrinna.bilibilimockbackground.service.impl;

import org.arrinna.bilibilimockbackground.domain.entity.user.UserLvInfo;
import org.arrinna.bilibilimockbackground.domain.enums.ArticleSortEnum;
import org.arrinna.bilibilimockbackground.domain.enums.BiliLVEnum;
import org.arrinna.bilibilimockbackground.domain.enums.SearchTypeEnum;
import org.arrinna.bilibilimockbackground.domain.enums.UserSortEnum;
import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.arrinna.bilibilimockbackground.domain.vo.UserVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.SearchEsReq;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.esdao.article.ColumnEsDao;
import org.arrinna.bilibilimockbackground.esdao.user.UserEsDao;
import org.arrinna.bilibilimockbackground.esdao.user.UserEsSearch;
import org.arrinna.bilibilimockbackground.service.ISearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import org.springframework.data.domain.Sort;
import jakarta.annotation.Resource;

import java.util.List;

@Service
public class SearchServiceImpl implements ISearchService {
    //todo 完善搜索关键字查看用户的功能
    //todo 用elasticSearch实现模糊查询
    //todo 数据库的模糊查询只能查连续的字符串匹配，不符合要求


    @Resource
    private UserEsDao userEsDao;
    @Resource
    private UserEsSearch userEsSearch;
    @Resource
    private ColumnEsDao columnEsDao;

    @Override
    public SearchVO search(SearchEsReq req){

        SearchTypeEnum searchTypeEnum=SearchTypeEnum.of(req.getSearchType());

        //然后分类讨论看看是啥
        switch(searchTypeEnum){
            case VIDEO->{
                //todo 查看视频

            }
            case USER -> {
                //todo 查看用户
                //调用es
                return doSearchUser(req.getKeyword(),
                 req.getSortCode(), req.getCurrentNum(), req.getPageSize());
            }
            case POST -> {
                //todo 查看帖子

                return null;
            }
            case ANIME -> {
                //todo 查看番剧

                return null;
            }
            case LIVE->{
                //todo 查看直播

                return null;
            }
            case ARTICLE -> {
                //todo 查看文章
                return doSearchArticle(req.getKeyword(), req.getSortCode()
                        ,req.getCurrentNum(), req.getPageSize());
            }
        }

        return null;
    }


    private SearchVO doSearchUser(String keyword,Integer sortType,
                                  long currentNum,long pageSize){
       //1.首先根据排序的类型获取枚举对象
       UserSortEnum userSortEnum=UserSortEnum.of(sortType);

       String sortField= userSortEnum.getColumn();
       Sort sort = Sort.unsorted();

       if(sortField!=null){
        //默认为粉丝的count
        Sort.Direction direction="asc".equalsIgnoreCase(userSortEnum.getOrder())
        ?Sort.Direction.ASC:
        Sort.Direction.DESC;
        sort=Sort.by(direction,userSortEnum.getColumn());

       }

       Pageable pageable = org.springframework.data.domain.PageRequest
       .of((int) Math.max(currentNum-1,0),(int) pageSize,sort);

       //接下来就分页查询吧~
        Page<UserEsDoc> page = userEsSearch.searchNickname(keyword,pageable);

        //然后把结果返回回去
        List<UserVO> userList = page.getContent().stream()
                .map(doc->{
                    UserLvInfo userLvInfo = new UserLvInfo();
                    //然后根据枚举类完善它
                    BiliLVEnum biliLVEnum = BiliLVEnum.of(doc.getLevel());
                    userLvInfo.setLevel(biliLVEnum.getLevel());
                    userLvInfo.setLevelName(biliLVEnum.getDesc());
                    userLvInfo.setNeedAddExp(biliLVEnum.getNeedAddExp());
                    userLvInfo.setTotalExp(biliLVEnum.getTotalExp());

                    UserInfoResp.UserFollowResp userFollowResp = new UserInfoResp.UserFollowResp();
                    userFollowResp.setFansCount(doc.getFansCount());

                   UserVO userVO= UserVO.builder()
                            .UID(doc.getUserId())
                            .nickname(doc.getNickname())
                            .avatar(doc.getAvatar())
                            .userLvInfo(userLvInfo)
                            .signature(doc.getSignature())
                            .userFollowInfo(userFollowResp)
                            .videoCount(doc.getVideoCount())
                            .build();
                    return userVO;
                })
                .toList();
        //最后改成List类型就可以了
        SearchVO vo = new SearchVO();
        vo.setUserList(userList);
        vo.setCurrentNum(currentNum);
        vo.setPageSize(pageSize);
       return vo;
    }

    private SearchVO doSearchArticle(String keyword,Integer sortType
            ,long currentNum,long pageSize){
        //1.首先是看看排序
        ArticleSortEnum sortEnum = ArticleSortEnum.of(sortType);

        //2.获取是以什么样子的顺序排的顺序之后开始实现代码

        String sort_field=sortEnum.getColumn();

        //TODO 2026年8月29日晚上暂时写到这里，明天把剩下的部分完善！！！
        return null;
    }
}
