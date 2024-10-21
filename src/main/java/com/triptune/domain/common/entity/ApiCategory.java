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
public class ApiCategory {

    @Id
    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "parent_code")
    private String parentCode;

    @Column(name = "level")
    private int level;

    @OneToMany(mappedBy = "apiCategory", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelPlace> travelPlaceList;


    @Builder
    public ApiCategory(String categoryCode, String categoryName, String parentCode, int level, List<TravelPlace> travelPlaceList) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.parentCode = parentCode;
        this.level = level;
        this.travelPlaceList = travelPlaceList;
    }
}
