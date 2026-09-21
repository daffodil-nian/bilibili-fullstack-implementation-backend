package org.arrinna.bilibilimockbackground.service.impl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.constant.COSFilePrefix;
import org.arrinna.bilibilimockbackground.common.constant.DefaultConstant;
import org.arrinna.bilibilimockbackground.common.constant.EmoticonConstant;
import org.arrinna.bilibilimockbackground.common.exception.BusinessException;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.common.util.CosUtil;
import org.arrinna.bilibilimockbackground.dao.emoticon.EmoticonItemDao;
import org.arrinna.bilibilimockbackground.dao.emoticon.EmoticonPackDao;
import org.arrinna.bilibilimockbackground.domain.entity.emoticon.EmoticonItem;
import org.arrinna.bilibilimockbackground.domain.entity.emoticon.EmoticonPack;
import org.arrinna.bilibilimockbackground.domain.vo.emoticon.EmoticonItemVO;
import org.arrinna.bilibilimockbackground.domain.vo.emoticon.EmoticonPackVO;
import org.arrinna.bilibilimockbackground.manager.CosManager;
import org.arrinna.bilibilimockbackground.service.IEmoticonService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * first，this is an emoticon service,
 */
@Service
@Slf4j
public class EmoticonServiceImpl implements IEmoticonService {
//    首先实现自定义icon的方法！

    @Resource
    private EmoticonItemDao emoticonItemDao;
    @Resource
    private EmoticonPackDao emoticonPackDao;
    @Resource
    private CosUtil cosUtil;
    @Resource
    private CosManager cosManager;
    /**
     * 返回所有的表情包项
     * @return
     */

    @Override
    public List<EmoticonPackVO> listPanel(Long uid){
        // 1.首先这个方法是负责获取panel用户控制面板的写法

        List<EmoticonPack> emoticonPacks = new ArrayList<>();
        emoticonPacks.addAll(emoticonPackDao.listOfficialOn());

        //2.然后如果packs是空的就return 空列表
        if(emoticonPacks!=null){
            emoticonPacks.addAll(emoticonPackDao.listMyCustomOn(uid));
        }
        if(emoticonPacks.isEmpty()){
            return List.of();
        }

        //3.接下来就是获取对应的pack_id，然后再通过pack_id再去获取到想要的东西

        List<Long> packIds=emoticonPacks
                .stream()
                .map(EmoticonPack::getId)
                .toList();

        //然后跟据pack_id获取到对应的emoticonItem
        Map<Long, List<EmoticonItem>> itemMap = emoticonItemDao.listOkByPackIds(packIds)
                .stream()
                .collect(Collectors.groupingBy(EmoticonItem::getPackId));

        //接下来就可以跟据id来获取到自己想要的表情包了！！！



        List<EmoticonPackVO> result=new ArrayList<>();

        for (EmoticonPack p:emoticonPacks){
            EmoticonPackVO vo = new EmoticonPackVO();
            vo.setId(p.getId());
            vo.setCoverUrl(p.getCoverUrl());
            vo.setType(p.getType());
            vo.setName(p.getName());
            List<EmoticonItemVO> emoticonItems = itemMap.getOrDefault(p.getId(),
                    List.of())
                    .stream()
                    .map(this::toItemVO)
                    .toList()
                    ;
            vo.setEmoticonItems(emoticonItems);
            result.add(vo);
        }

        return result;
    }


    public  long createPark(Long uid,String name){

        return 0;

    }

    /**
     * 获取表情包的vo数据
     * @param emoticonItem
     * @return
     */
    private EmoticonItemVO toItemVO(EmoticonItem emoticonItem){
        EmoticonItemVO vo = new EmoticonItemVO();
        vo.setId(emoticonItem.getId());
        vo.setName(emoticonItem.getName());
        vo.setUrl(emoticonItem.getUrl());
        vo.setCode(emoticonItem.getCode());
        return vo;
    }


    @Override
    public EmoticonItemVO upload(Long uid, MultipartFile file, String name){

        //1.首先卡后缀ing
        final ArrayList<String> suffixList = List.of("jpg", "png", "jpeg").stream()
                .map(String::toLowerCase).collect(Collectors.toCollection(ArrayList::new));

        //2.然后如果表情包的大小超过限制范围就抛出异常
        AssertUtil.isFalse(file.getSize()> DefaultConstant.MAX_EMOTICON_SIZE,
                ErrorCodeEnum.EMOTICON_SIZE_LIMIT);

        //3.
        String PicType=file.getContentType();
        log.info("图片类型是:{}",PicType);
        String suffix=PicType.split("/")[1];
        log.info("图片后缀是:{}",suffix);
        AssertUtil.isFalse(!suffixList.contains(suffix),ErrorCodeEnum.EMOTICON_TYPE_ERROR);

        //4.初步校验通过之后就可以开始调用dao层了
        byte[] bytes;
        //如果服务器突然断网了就抛出这个异常
        try {
            bytes = file.getBytes(); // 受检异常，必须 try 或 throws
        } catch (IOException e) {
            throw new BusinessException(ErrorCodeEnum.EMOTICON_UPLOAD_FAIL);
        }
        String hash = DigestUtils.md5DigestAsHex(bytes);

        //5.接下来就是把emoticon pack new 出来使用
        EmoticonPack pack = ensureMyPack(uid);
        //6.接下来就调用emoticonItemDao来保存表情包

        long count = emoticonItemDao.lambdaQuery()
                .eq(EmoticonItem::getPackId,pack.getId())
                .eq(EmoticonItem::getStatus,EmoticonConstant.ITEM_OK)
                .count();
        //7.如果超过了指定数量就准备抛出异常
        AssertUtil.isFalse(count>=EmoticonConstant.ITEM_COUNT,ErrorCodeEnum.EMOTICON_UPLOAD_LIMIT);

        //8.没到上线就可以插入进去了！
        EmoticonItem emoticonItem = emoticonItemDao.lambdaQuery()
                .eq(EmoticonItem::getPackId,pack.getId())
                .eq(EmoticonItem::getUrl,pack.getCoverUrl())
                .one();

        AssertUtil.isFalse(emoticonItem != null, ErrorCodeEnum.EMOTICON_DUPLICATE);

        String filename = UUID.randomUUID()+"."+suffix;
        //然后可以上传，要用上cosUtil工具类
        String filepath = String.format(COSFilePrefix.EMOTICON_PREFIX,hash,filename);


        File temp = null;
        try{
            temp=File.createTempFile("emoji_", "");
            file.transferTo(temp);
            cosManager.putObject(filepath,temp);
            log.info("上传成功"+filepath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.EMOTICON_UPLOAD_FAIL);
        }

        //9.最后写入数据库中去
        /**
         * private Long id;
         *     private Long packId;
         *     private String code;       // [微笑]
         *     private String name;
         *     private String url;        // 相对路径
         *     private Integer sort;
         *     private Integer status;
         *     private LocalDateTime createTime;
         *     private LocalDateTime updateTime;
         */
        EmoticonItem item = new EmoticonItem();
        if(name==null||name.isBlank()) name="自定义";
        Long packId= pack.getId();
        item.setPackId(packId);
        item.setUrl(filepath); //存储在数据库中是相对路径
        item.setStatus(EmoticonConstant.ITEM_OK);
        item.setName(name);
        item.setCode(nextCode(packId,item.getName()));
        item.setSort(0);
        emoticonItemDao.save(item);

        EmoticonItemVO vo = toItemVO(item);
        vo.setUrl(cosUtil.toFullUrl(filepath));//vo返回的是完整路径
        return vo;
    }
    private EmoticonPack ensureMyPack(Long uid){
        EmoticonPack pack = emoticonPackDao.lambdaQuery()
                .eq(EmoticonPack::getOwnerUid,uid)
                .eq(EmoticonPack::getType, EmoticonConstant.PACK_CUSTOM)
                .one()
                ;

        if(pack!=null){
            return pack;
        }


        /**
         * 如果不存在就创建
         * pack的 private Long id;
         *     private String name;
         *     private String coverUrl;
         *     private Integer type;      // 1官方 2自定义
         *     private Long ownerUid;
         *     private Integer status;
         *     private Integer sort;
         *     private LocalDateTime createTime;
         *     private LocalDateTime updateTime;
         *     这么设计可以这么理解，name可以自己生成，然后type如果是自定义的话就是自己给自己提供的用了
         *     其他的属性也是一样的道理
         */

        pack = new EmoticonPack();

        pack.setName("我的表情包");
        pack.setType(EmoticonConstant.PACK_CUSTOM);//自定义表情包
        pack.setCreateTime(LocalDateTime.now());
        pack.setOwnerUid(uid);
        pack.setStatus(EmoticonConstant.PACK_ON);
        pack.setSort(0);

        emoticonPackDao.save(pack);
        return pack;
    }

    /**
     * 让AI生成的一个自己生成表情包对应的code的写法
     * @param packId
     * @param name
     * @return
     */
    private String nextCode(Long packId, String name){
        String base = "[" + name + "]";
        if (emoticonItemDao.lambdaQuery()
                .eq(EmoticonItem::getPackId, packId)
                .eq(EmoticonItem::getCode, base)
                .count() == 0) {
            return base;
        }
        for (int i = 2; i < 1000; i++) {
            String code = "[" + name + "_" + i + "]";
            long n = emoticonItemDao.lambdaQuery()
                    .eq(EmoticonItem::getPackId, packId)
                    .eq(EmoticonItem::getCode, code)
                    .count();
            if (n == 0) {
                return code;
            }
        }
        return "[自定义_" + System.currentTimeMillis() + "]";
    }

}
