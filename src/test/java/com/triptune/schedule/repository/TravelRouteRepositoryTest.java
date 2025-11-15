package com.triptune.schedule.repository;

import com.triptune.BaseTest;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.repository.ApiCategoryRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("h2")
class TravelRouteRepositoryTest extends ScheduleTest {

    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;


    @Test
    @DisplayName("여행 루트 생성")
    void createTravelRoute() {
        // given
        TravelSchedule schedule = createTravelSchedule(null, "테스트");
        travelScheduleRepository.save(schedule);
        Country country = createCountry();
        countryRepository.save(country);
        City city = createCity(country);
        cityRepository.save(city);
        District district = createDistrict(city, "부암동");
        districtRepository.save(district);
        ApiCategory apiCategory = createApiCategory();
        apiCategoryRepository.save(apiCategory);

        TravelPlace place = createTravelPlace(null, country, city, district, apiCategory);
        travelPlaceRepository.save(place);

        TravelRoute route = createTravelRoute(schedule, place, 1);

        // when
        travelRouteRepository.save(route);

        // then
        assertThat(route.getRouteId()).isNotNull();
        assertThat(route.getCreatedAt()).isNotNull();
    }

}