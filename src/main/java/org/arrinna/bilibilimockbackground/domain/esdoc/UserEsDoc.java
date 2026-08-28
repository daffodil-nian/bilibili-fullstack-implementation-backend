package org.arrinna.bilibilimockbackground.domain.esdoc;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.Data;

@Data
@Setting(settingPath = "es/bili-user-settings.json")
@Document(indexName = "bili_user")
public class UserEsDoc {
    @Id
    private Long userId;

    @Field(type = FieldType.Text,analyzer="ngram_analyzer",searchAnalyzer = "ngram_analyzer")
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String avatar;

    @Field(type = FieldType.Keyword)
    private String signature;

    @Field(type = FieldType.Integer)
    private Integer level;

    @Field(type = FieldType.Integer)
    private Integer fansCount;

    @Field(type = FieldType.Integer)
    private Integer videoCount;

}
