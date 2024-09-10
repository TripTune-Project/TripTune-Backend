package com.triptune.domain.travel.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.FileRepository;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageFileRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
public class TravelServiceTests {

    @InjectMocks
    private TravelService travelService;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TravelImageFileRepository travelImageFileRepository;


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void findNearByTravelPlaces_withData_success(){
        // given
        TravelLocationRequest request = TravelLocationRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .build();

        int page = 1;

        Country country = Country.builder().countryId(1L).countryName("대한민국").build();
        City city = City.builder().cityId(1L).cityName("서울").build();
        District district = District.builder().districtId(1L).districtName("강남구").build();
        ApiCategory apiCategory = ApiCategory.builder().categoryCode("A0101").categoryName("자연").level(1).build();

        TravelPlace travelPlace = TravelPlace.builder()
                .country(country)
                .city(city)
                .district(district)
                .apiCategory(apiCategory)
                .contentTypeId(1L)
                .placeName("테스트 장소명")
                .address("테스트 주소")
                .latitude(37.5)
                .longitude(127.0281573537)
                .apiContentId(1)
                .createdAt(LocalDateTime.now())
                .build();

        TravelPlace savedTravelPlace = travelRepository.save(travelPlace);

        File file = File.builder()
                .s3ObjectUrl("/test/test1.jpg")
                .originalName("test.jpg")
                .fileName("test1.jpg")
                .fileType("jpg")
                .fileSize(20)
                .createdAt(LocalDateTime.now())
                .isThumbnail(true)
                .build();

        File savedFile = fileRepository.save(file);


        TravelImageFile travelImageFile = TravelImageFile.builder()
                .travelPlace(savedTravelPlace)
                .file(savedFile)
                .build();

        travelImageFileRepository.save(travelImageFile);

        // when
        Page<TravelResponse> response = travelService.findNearByTravelPlaces(request, page);

        // then
        List<TravelResponse> content = response.getContent();
        assertNotEquals(response.getTotalElements(), 0);
        assertEquals(content.get(0).getCity(), "서울");
        assertEquals(content.get(0).getPlaceName(), "테스트 장소명");
        assertEquals(content.get(0).getAddress(), "테스트 주소");
        assertEquals(content.get(0).getLatitude(), 37.5);
        assertEquals(content.get(0).getThumbnailUrl(), "/test/test1.jpg");
        assertNotEquals(content.get(0).getDistance(), 0.0);

    }


    @Test
    @DisplayName("findNearByTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void findNearByTravelPlaces_noData_success(){

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 존재하는 경우")
    void searchTravelPlaces_withData_success(){

    }


    @Test
    @DisplayName("searchTravelPlaces() 성공: 현재 위치에 따른 여행지 목록 조회 시 데이터 없는 경우")
    void searchTravelPlaces_noData_success(){

    }


}
