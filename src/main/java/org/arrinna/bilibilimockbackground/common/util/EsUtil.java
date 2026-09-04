package org.arrinna.bilibilimockbackground.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.dao.user.UserDao;
import org.arrinna.bilibilimockbackground.dao.user.UserFollowDao;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.arrinna.bilibilimockbackground.esdao.user.UserEsDao;
import org.arrinna.bilibilimockbackground.job.once.FullSyncUserToEs;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EsUtil {

    private final ElasticsearchOperations elasticsearchOperations;

    private final UserEsDao userEsDao;
    private final UserDao userDao;
    private final UserFollowDao userFollowDao;

    public void syncUserByUid(Long uid){
        AssertUtil.isFalse(uid==null, ErrorCodeEnum.PARAM_ERROR);
        User user= userDao.lambdaQuery()
                .eq(User::getUId,uid)
                .one();
        FullSyncUserToEs fullSyncUserToEs=new FullSyncUserToEs();
        int count=userFollowDao.getFansCount(uid);
        UserEsDoc doc = fullSyncUserToEs.toDoc(user,count);

        //还要同步更新粉丝数
        int fansCount = userFollowDao.getFansCount(uid);
        doc.setFansCount(fansCount);
        userEsDao.save(doc);
        log.info("同步用户{}到es",uid);
    }

//    public
}
