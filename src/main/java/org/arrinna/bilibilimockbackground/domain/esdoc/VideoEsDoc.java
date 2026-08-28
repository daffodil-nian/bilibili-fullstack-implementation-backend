package org.arrinna.bilibilimockbackground.domain.esdoc;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "bili_video")
public class VideoEsDoc {
    
    @Id
    private Long videoId;

    //TODO 视频不了解，未来开发再完善
}
