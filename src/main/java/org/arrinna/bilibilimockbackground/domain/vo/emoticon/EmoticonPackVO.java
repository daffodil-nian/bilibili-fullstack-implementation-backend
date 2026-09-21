package org.arrinna.bilibilimockbackground.domain.vo.emoticon;

import lombok.Data;

import java.util.List;

@Data
public class EmoticonPackVO {
    private Long id;
    private Integer type;
    private String name;
    private String coverUrl;
    private List<EmoticonItemVO> emoticonItems;
}
