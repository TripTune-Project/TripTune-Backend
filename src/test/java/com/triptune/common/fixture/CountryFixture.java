package com.triptune.common.fixture;

import com.triptune.common.entity.Country;

public class CountryFixture {

    public static Country createCountry(){
        return Country.createCountry("대한민국");
    }

}
