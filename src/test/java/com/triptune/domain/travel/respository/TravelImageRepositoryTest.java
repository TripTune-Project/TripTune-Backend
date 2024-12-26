package com.triptune.domain.travel.respository;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.travel.TravelTest;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class TravelImageRepositoryTest extends TravelTest {

    private final TravelImageRepository travelImageRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;
    private final TravelPlaceRepository travelPlaceRepository;

    private TravelPlace travelPlace;
    private TravelImage travelImage1;
    private TravelImage travelImage2;

    @Autowired
    public TravelImageRepositoryTest(TravelImageRepository travelImageRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, ApiContentTypeRepository apiContentTypeRepository, TravelPlaceRepository travelPlaceRepository) {
        this.travelImageRepository = travelImageRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
        this.travelPlaceRepository = travelPlaceRepository;
    }

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, "test1", true));
        travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, "test2", false));

        travelPlace.setApiContentType(apiContentType);
        travelPlace.setTravelImageList(new ArrayList<>(List.of(travelImage1, travelImage2)));
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
