package org.arrinna.bilibilimockbackground.job.once;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.esdoc.UserEsDoc;
import org.arrinna.bilibilimockbackground.esdao.UserEsDao;
import org.arrinna.bilibilimockbackground.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FullSyncUserToEs implements CommandLineRunner {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserEsDao userEsDao;

    @Override
    public void run(String... args) throws Exception {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        List<UserEsDoc> docs = new ArrayList<>();
        for (User u : users) {
            docs.add(toDoc(u));
        }
        int batch = 500;
        for (int i = 0; i < docs.size(); i += batch) {
            int end = Math.min(i + batch, docs.size());
            userEsDao.saveAll(docs.subList(i, end));
            log.info("FullSyncUserToEs {} - {}", i, end);
        }
        log.info("FullSyncUserToEs done, total={}", docs.size());
    }

    public static UserEsDoc toDoc(User u) {
        UserEsDoc doc = new UserEsDoc();
        doc.setUserId(u.getUId());
        doc.setNickname(u.getNickname());
        doc.setAvatar(u.getAvatar());
        doc.setSignature(u.getSignature());
        doc.setLevel(u.getLevel() == null ? 0 : u.getLevel());
        doc.setFansCount(0);   // 有粉丝表再填
        doc.setVideoCount(0);
        return doc;
    }
}
