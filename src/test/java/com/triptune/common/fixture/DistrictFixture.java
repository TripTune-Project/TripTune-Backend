package com.triptune.common.fixture;

import com.triptune.common.entity.City;
import com.triptune.common.entity.District;

public class DistrictFixture {
    public static District createDistrict(City city, String districtName){
        return District.createDistrict(city, districtName);
    }
}
