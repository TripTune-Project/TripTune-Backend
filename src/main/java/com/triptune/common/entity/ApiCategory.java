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



    @Builder
    public ApiCategory(String categoryCode, String categoryName, String parentCode, int level) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.parentCode = parentCode;
        this.level = level;
    }
}
