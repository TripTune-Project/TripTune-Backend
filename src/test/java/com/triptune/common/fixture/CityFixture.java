package com.triptune.common.fixture;

import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;

public class CityFixture {

    public static City createCity(Country country, String cityName){
        return City.createCity(country, cityName);
    }

    public static City createSeoul(Country country){
        return City.createCity(country, "서울특별시");
    }

    public static City createBusan(Country country){
        return City.createCity(country, "부산광역시");
    }

}
