package com.triptune.common.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ApiContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_type_id")
    private Long contentTypeId;

    @Column(name = "content_type_name")
    private String contentTypeName;

    @Column(name = "api_content_type_id")
    private Integer apiContentTypeId;


    @Builder
    public ApiContentType(Long contentTypeId, String contentTypeName, Integer apiContentTypeId) {
        this.contentTypeId = contentTypeId;
        this.contentTypeName = contentTypeName;
        this.apiContentTypeId = apiContentTypeId;
    }
}
