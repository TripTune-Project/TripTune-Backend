package com.triptune.common.entity;

import com.triptune.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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


    @Builder
    public ApiContentType(Long contentTypeId, String contentTypeName) {
        this.contentTypeId = contentTypeId;
        this.contentTypeName = contentTypeName;
    }
}
