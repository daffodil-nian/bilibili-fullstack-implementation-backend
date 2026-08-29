package org.arrinna.bilibilimockbackground.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserWallet;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;
import org.arrinna.bilibilimockbackground.mapper.user.UserWalletMapper;
import org.springframework.stereotype.Service;

@Service
public class UserWalletDao extends ServiceImpl<UserWalletMapper, UserWallet> {

    /**
     *
     * @param id
     * @return
     */
    public UserInfoResp.UserWalletResp getUserWalletByULongId(Long id){
        UserWallet userWallet = lambdaQuery()
                .eq(UserWallet::getUser_id,id)
                .one();
        UserInfoResp.UserWalletResp userWalletResp = UserInfoResp
                .UserWalletResp
                .builder()
                .coin(userWallet.getCoin())
                .bCoin(userWallet.getBCoin())
                .build();
        return userWalletResp;
    }

}
