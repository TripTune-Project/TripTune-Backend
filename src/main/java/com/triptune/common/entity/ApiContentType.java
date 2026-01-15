package com.triptune.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_type_id")
    private Long contentTypeId;

    @Column(name = "content_type_name")
    private String contentTypeName;

    @Column(name = "api_content_type_id")
    private Integer apiContentTypeId;


    private ApiContentType(String contentTypeName, Integer apiContentTypeId) {
        this.contentTypeName = contentTypeName;
        this.apiContentTypeId = apiContentTypeId;
    }

    public static ApiContentType createApiContentType(String contentTypeName, Integer apiContentTypeId) {
        return new ApiContentType(contentTypeName, apiContentTypeId);
    }


}
