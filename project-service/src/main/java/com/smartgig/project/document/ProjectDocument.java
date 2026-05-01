package com.smartgig.project.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "projects")
@Setting(settingPath = "elasticsearch/project-settings.json")
@Mapping(mappingPath = "elasticsearch/project-mapping.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long projectId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Double)
    private Double budgetMin;

    @Field(type = FieldType.Double)
    private Double budgetMax;

    @Field(type = FieldType.Keyword)
    private String budgetType;

    @Field(type = FieldType.Keyword)
    private List<String> skillNames;

    @Field(type = FieldType.Keyword)
    private String complexityLevel;

    @Field(type = FieldType.Boolean)
    private Boolean isFeatured;

    @Field(type = FieldType.Integer)
    private Integer viewsCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime deadline;

    @Field(type = FieldType.Long)
    private Long clientId;
}

