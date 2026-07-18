package org.arrinna.bilibilimockbackground.service.impl;

import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.algorithm.SnowAlgorithm;
import org.arrinna.bilibilimockbackground.common.config.SnowConfig;
import org.arrinna.bilibilimockbackground.common.util.GenerateUserDefaultNickname;
import org.arrinna.bilibilimockbackground.service.IAuthService;
import org.springframework.stereotype.Service;

/**
 * @Author Arrinna
 */
@Service
public class AuthServiceImpl implements IAuthService {

    @Resource
    private SnowAlgorithm snowAlgorithm;

    public String login(String username,String password){
        //用户想登录首先得发送HTTP请求，请求字段得有，然后token不要少

        //先处理请求


        return "login";
    }

    /**
     * 先用账户名密码注册，然后再想短信注册方式吧，毕竟这个不是很科学
     * @return
     */
    public String register(){
        //首先我们用上雪花算法来生成id，保证这个ID的唯一性

        Long id = snowAlgorithm.nextIdInstance();
        //bilibili注册后昵称默认是bili前缀后面11个字母
        String nickname= GenerateUserDefaultNickname.getInstance().nextUniqueName();
        //要确定是否重复，如果

        //sex默认是未知
        //birth_day默认是2000-01-01
        //avatar默认是默认头像
        return "register";
    }
}
