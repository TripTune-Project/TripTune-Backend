package com.triptune.common.fixture;

import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;

public class CityFixture {

    public static City createCity(Country country, String cityName){
        return City.createCity(country, cityName);
    }

}
