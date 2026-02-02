package com.triptune.travel.respository;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
public class TravelImageRepositoryTest {

    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;



    @Test
    @DisplayName("placeId 를 이용해서 썸네일 이미지 조회")
    void findThumbnailImageByPlaceId(){
        // given
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createCity(country, "서울"));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(ApiCategoryFixture.createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS));

        TravelPlace place = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지"
                )
        );
        TravelImage placeThumbnail = travelImageRepository.save(TravelImageFixture.createTravelImage(place, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place, "test2", false));

        // when
        String response = travelImageRepository.findThumbnailUrlByPlaceId(place.getPlaceId());

        // then
        assertThat(response).isEqualTo(placeThumbnail.getS3ObjectUrl());
    }

}
