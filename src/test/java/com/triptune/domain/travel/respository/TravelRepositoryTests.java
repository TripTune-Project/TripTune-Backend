package com.triptune.domain.travel.respository;

import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocationResponse;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TravelRepositoryTests {

    @Autowired
    private TravelRepository travelRepository;

    @Test
    @DisplayName("성공: 위치 정보에 따른 여행지 목록 조회")
    void successFindNearByTravelPlaceList(){
        // given
        Pageable pageable = PageRequest.of(0, 5);
        TravelLocationRequest travelLocationRequest = TravelLocationRequest.builder()
                .latitude(37.4970465429)
                .longitude(127.0281573537)
                .build();
        int radius = 5;   // 5km 이내

        // when
        Page<TravelLocationResponse> result = travelRepository.findNearByTravelPlaceList(pageable, travelLocationRequest, radius);

        // then
        List<TravelLocationResponse> content = result.getContent();
        assertNotEquals(result.getTotalElements(), 0);
        assertNotNull(content.get(0));
        System.out.println(content.get(0));
    }

}
