package org.arrinna.bilibilimockbackground.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.domain.vo.UserVO;
import org.arrinna.bilibilimockbackground.service.IUserService;

/**
 * 这是用户数据源
 */
public class UserDataSource implements DataSource<UserVO>{

    @Resource
    private IUserService userService;


    /**
     * 根据关键词搜索出用户角色
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize) {

        return null;
    }
}
