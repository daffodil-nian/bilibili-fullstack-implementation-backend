package org.arrinna.bilibilimockbackground.dao.emoticon;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.common.constant.EmoticonConstant;
import org.arrinna.bilibilimockbackground.domain.entity.emoticon.EmoticonPack;
import org.arrinna.bilibilimockbackground.mapper.emoticon.EmoticonPackMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmoticonPackDao extends ServiceImpl<EmoticonPackMapper,EmoticonPack> {


    /**
     * 这个代码是负责从数据库中获取到官方的表情包数据
     * @return
     */
    public List<EmoticonPack> listOfficialOn() {
        //返回的是type=1的并且状态是ON的
        return lambdaQuery()
                .eq(EmoticonPack::getType,EmoticonConstant.PACK_OFFICIAL)
                .eq(EmoticonPack::getStatus, EmoticonConstant.PACK_ON)
                .orderByDesc(EmoticonPack::getSort)
                .list();
    }


    /**
     * 判断自己的custom是否能找到uid
     * @param uid
     * @return
     */
    public List<EmoticonPack> listMyCustomOn(Long uid){

        return lambdaQuery()
                .eq(EmoticonPack::getOwnerUid,uid)
                .eq(EmoticonPack::getType,EmoticonConstant.PACK_CUSTOM)
                .eq(EmoticonPack::getStatus, EmoticonConstant.PACK_ON)
                .orderByDesc(EmoticonPack::getSort)
                .list();
    }




}
