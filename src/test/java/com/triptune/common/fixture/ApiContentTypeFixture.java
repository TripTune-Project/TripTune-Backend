package com.triptune.common.fixture;

import com.triptune.common.entity.ApiContentType;
import com.triptune.travel.enums.ThemeType;

public class ApiContentTypeFixture {

    public static ApiContentType createApiContentType(ThemeType themeType){
        return ApiContentType.createApiContentType(
                themeType.getApiContentTypeName(),
                themeType.getApiContentTypeId()
        );
    }
}
