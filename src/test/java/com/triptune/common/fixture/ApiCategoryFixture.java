package com.triptune.common.fixture;

import com.triptune.common.entity.ApiCategory;

public class ApiCategoryFixture {

    public static ApiCategory createApiCategory(){
        return ApiCategory.createApiCategory(
                "A0101",
                "자연",
                "A01",
                1
        );
    }

}
