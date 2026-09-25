package com.triptune.schedule.repository;

import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.ApiContentTypeFixture;
import com.triptune.common.fixture.CityFixture;
import com.triptune.common.fixture.CountryFixture;
import com.triptune.common.fixture.DistrictFixture;
import com.triptune.common.repository.ApiContentTypeRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.global.config.JpaConfig;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaConfig.class})
@ActiveProfiles("h2")
class TravelRouteRepositoryTest  {

    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private EntityManager em;

    private TravelSchedule schedule;
    private TravelPlace place;

    @BeforeEach
    void setUp(){
        schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트"));
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createSeoul(country));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "부암동"));
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ATTRACTIONS));
        place = travelPlaceRepository.save(TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                "여행지"
        ));
    }


    @Test
    @DisplayName("여행 루트 생성")
    void createTravelRoute() {
        // given
        TravelRoute route = TravelRouteFixture.createRoute(schedule, place, 1);

        // when
        travelRouteRepository.save(route);

        // then
        assertThat(route.getRouteId()).isNotNull();
        assertThat(route.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("일정에 여행루트 모두 삭제")
    void deleteAllByScheduleId() {
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place, 3));
        TravelRoute route4 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place, 4));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        TravelRoute otherRoute = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule2, place, 1));

        // when
        travelRouteRepository.deleteAllByScheduleId(schedule.getScheduleId());

        // then
        em.flush();
        em.clear();

        List<TravelRoute> travelRoutes = travelRouteRepository.findAll();
        assertThat(travelRoutes).hasSize(1);
        assertThat(travelRoutes).doesNotContain(route1);
        assertThat(travelRoutes).doesNotContain(route2);
        assertThat(travelRoutes).doesNotContain(route3);
        assertThat(travelRoutes).doesNotContain(route4);
        assertThat(travelRoutes.get(0).getRouteId()).isEqualTo(otherRoute.getRouteId());
    }

}