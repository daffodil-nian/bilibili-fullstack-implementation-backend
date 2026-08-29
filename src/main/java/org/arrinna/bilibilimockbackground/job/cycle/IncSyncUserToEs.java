package org.arrinna.bilibilimockbackground.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.arrinna.bilibilimockbackground.esdao.user.UserEsDao;
import org.arrinna.bilibilimockbackground.job.once.FullSyncUserToEs;
import org.arrinna.bilibilimockbackground.mapper.user.UserMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IncSyncUserToEs {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserEsDao userEsDao;

    /** 每分钟：同步近 5 分钟有更新的用户 */
    @Scheduled(fixedRate = 60_000)
    public void run() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().ge(User::getUpdateTime, since)
        );
        if (users.isEmpty()) {
            return;
        }
        List<UserEsDoc> docs = users.stream()
                .map(FullSyncUserToEs::toDoc)
                .collect(Collectors.toList());
        userEsDao.saveAll(docs);
        log.info("IncSyncUserToEs size={}", docs.size());
    }
}