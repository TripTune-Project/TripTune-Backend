package com.triptune.schedule.repository;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.triptune.travel.enums.ThemeType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
class TravelRouteRepositoryTest  {

    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;


    @Test
    @DisplayName("여행 루트 생성")
    void createTravelRoute() {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트"));
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createCity(country, "서울"));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "부암동"));
        ApiCategory apiCategory = apiCategoryRepository.save(ApiCategoryFixture.createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ATTRACTIONS));
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

        TravelRoute route = TravelRouteFixture.createTravelRoute(schedule, place, 1);

        // when
        travelRouteRepository.save(route);

        // then
        assertThat(route.getRouteId()).isNotNull();
        assertThat(route.getCreatedAt()).isNotNull();
    }

}