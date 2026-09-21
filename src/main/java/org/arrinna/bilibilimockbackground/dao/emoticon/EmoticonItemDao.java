package org.arrinna.bilibilimockbackground.dao.emoticon;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.arrinna.bilibilimockbackground.common.constant.EmoticonConstant;
import org.arrinna.bilibilimockbackground.domain.entity.emoticon.EmoticonItem;
import org.arrinna.bilibilimockbackground.mapper.emoticon.EmoticonItemMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmoticonItemDao extends ServiceImpl<EmoticonItemMapper, EmoticonItem> {

    public List<EmoticonItem> listOkByPackIds(List<Long> packIds){
        if(packIds == null || packIds.isEmpty()){
            return List.of();
        }
        return lambdaQuery()
                .in(EmoticonItem::getPackId, packIds)
                .eq(EmoticonItem::getStatus, EmoticonConstant.ITEM_OK)
                .list();

    }

}
