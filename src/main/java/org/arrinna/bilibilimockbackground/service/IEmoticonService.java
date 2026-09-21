package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.vo.emoticon.EmoticonItemVO;
import org.arrinna.bilibilimockbackground.domain.vo.emoticon.EmoticonPackVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IEmoticonService {

    List<EmoticonPackVO> listPanel(Long uid);

    EmoticonItemVO upload(Long uid, MultipartFile file, String name);
}
