package com.triptune.domain.schedule.repository;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.config.QueryDSLConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TravelRouteRepositoryTests extends ScheduleTest {
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final FileRepository fileRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;
    private final TravelRouteRepository travelRouteRepository;



    @Autowired
    public TravelRouteRepositoryTests(TravelScheduleRepository travelScheduleRepository, TravelPlacePlaceRepository travelPlaceRepository, FileRepository fileRepository, CityRepository cityRepository, CountryRepository countryRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository, TravelRouteRepository travelRouteRepository) {
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelPlaceRepository = travelPlaceRepository;
        this.fileRepository = fileRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.travelImageRepository = travelImageRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
        this.travelRouteRepository = travelRouteRepository;
    }
    
    @Test
    @DisplayName("여행 루트 중 첫번째 여행지의 이미지 목록 조회")
    void findPlaceImagesOfFirstRoute(){
        // given
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "성북구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        TravelPlace travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        TravelPlace travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));
        File file1 = fileRepository.save(createFile("test1", true));
        File file2 = fileRepository.save(createFile("test2", false));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, file2));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, file1));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, file2));

        travelPlace1.setApiContentType(apiContentType);
        travelPlace1.setTravelImageList(Arrays.asList(travelImage1, travelImage2));
        travelPlace2.setApiContentType(apiContentType);
        travelPlace2.setTravelImageList(Arrays.asList(travelImage3, travelImage4));

        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule(null,"테스트"));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace2, 2));

        schedule.setTravelRouteList(Arrays.asList(route1, route2));
        
        // when
        List<TravelImage> response = travelRouteRepository.findPlaceImagesOfFirstRoute(schedule.getScheduleId());
        
        
        // then
        assertEquals(response.size(), 2);
        assertEquals(response.get(0).getTravelPlace().getPlaceName(), travelPlace1.getPlaceName());
        assertEquals(response.get(0).getTravelPlace().getPlaceId(), travelPlace1.getPlaceId());
        
    }

    @Test
    @DisplayName("여행 루트 중 첫번째 여행지의 이미지 목록 조회 시 이미지가 없는 경우")
    void findPlaceImagesOfFirstRouteNoData(){
        // given
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "성북구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        TravelPlace travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        TravelPlace travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));

        travelPlace1.setApiContentType(apiContentType);
        travelPlace2.setApiContentType(apiContentType);

        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule(null,"테스트"));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace2, 2));

        schedule.setTravelRouteList(Arrays.asList(route1, route2));

        // when
        List<TravelImage> response = travelRouteRepository.findPlaceImagesOfFirstRoute(schedule.getScheduleId());


        // then
        assertEquals(response.size(), 0);

    }
}
