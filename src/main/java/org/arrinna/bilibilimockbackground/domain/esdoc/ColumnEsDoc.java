package org.arrinna.bilibilimockbackground.domain.esdoc;

import java.sql.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "bili_column")
public class ColumnEsDoc {
    @Id
    private Long columnId;

    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Keyword)
    private String tag;
@Field(type = FieldType.Keyword)
    private String authorNickname;
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Date)
    private Date updateTime;
}
