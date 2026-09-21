package org.arrinna.bilibilimockbackground.controller;

import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.Result;
import org.arrinna.bilibilimockbackground.domain.vo.emoticon.EmoticonItemVO;
import org.arrinna.bilibilimockbackground.domain.vo.emoticon.EmoticonPackVO;
import org.arrinna.bilibilimockbackground.service.IEmoticonService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 表情包管理器，可增加，删除，修改表情包功能
 */
@RestController
@RequestMapping("/api/emoticon")
public class EmoticonController {
    //1.

    @Resource
    private IEmoticonService EmoticonService;


    //2.实现自己上传表情包到我的表情中
    @PostMapping("/upload")
    public Result<EmoticonItemVO> upload(
            @RequestAttribute("uid") Long uid,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name
    ){

        return Result.Success(EmoticonService.upload(uid,file,name),"表情包上传成功！");
    }
    @GetMapping("/panel")
    public Result<List<EmoticonPackVO>> listPanel( @RequestAttribute("uid") Long uid){

        return Result.Success(EmoticonService.listPanel(uid),"表情包列表获取成功！");
    }


}
