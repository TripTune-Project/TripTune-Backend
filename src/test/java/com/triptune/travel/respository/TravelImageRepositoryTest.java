package com.triptune.travel.respository;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.travel.TravelTest;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("h2")
public class TravelImageRepositoryTest extends TravelTest {

    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;

    private TravelPlace travelPlace;
    private TravelImage travelImage1;


    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, "test1", true));
        travelImageRepository.save(createTravelImage(travelPlace, "test2", false));
    }

    @Test
    @DisplayName("placeId 를 이용해서 썸네일 이미지 조회")
    void findThumbnailImageByPlaceId(){
        // given, when
        String response = travelImageRepository.findThumbnailUrlByPlaceId(travelPlace.getPlaceId());

        // then
        assertThat(response).isEqualTo(travelImage1.getS3ObjectUrl());
    }

}
