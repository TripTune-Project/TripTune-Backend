package com.triptune.domain.common.entity;

import com.triptune.domain.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ApiContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_type_id")
    private Long contentTypeId;

    @Column(name = "content_type_name")
    private String contentTypeName;

    @OneToMany(mappedBy = "apiContentType", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelPlace> travelPlaceList;

    @Builder
    public ApiContentType(Long contentTypeId, String contentTypeName, List<TravelPlace> travelPlaceList) {
        this.contentTypeId = contentTypeId;
        this.contentTypeName = contentTypeName;
        this.travelPlaceList = travelPlaceList;
    }
}
