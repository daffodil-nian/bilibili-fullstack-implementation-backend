package org.arrinna.bilibilimockbackground.domain.esdoc;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Builder
@Data
@Setting(settingPath = "es/bili-column-settings.json")
@Document(indexName = "bili_column")
public class ColumnEsDoc {
    @Id
    private Long columnId;
    @Field(type = FieldType.Long)
    private Long userId;//作者id
    @Field(type = FieldType.Text,analyzer = "ngram_analyzer",searchAnalyzer = "ngram_analyzer")
    private String title;
    @Field(type = FieldType.Keyword)
    private String cover;
    @Field(type = FieldType.Keyword)
    private String tag;
    @Field(type = FieldType.Keyword)
    private String authorNickname;
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer clickCount;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||epoch_millis")
    private LocalDateTime createTime;
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||epoch_millis")
    private LocalDateTime updateTime;
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||epoch_millis")
    private LocalDateTime publishTime;
}
