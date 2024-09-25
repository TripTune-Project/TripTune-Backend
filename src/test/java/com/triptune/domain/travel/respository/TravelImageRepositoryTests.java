package com.triptune.domain.travel.respository;

import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.repository.TravelImageRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class TravelImageRepositoryTests {

    @Autowired
    private TravelImageRepository travelImageRepository;

    @Test
    @DisplayName("성공: placeId를 이용해서 List<TravelImageFile> 조회")
    void successFindByPlaceId(){
        // given
        Long placeId = 1L;

        // when
        List<TravelImage> result = travelImageRepository.findByTravelPlacePlaceId(placeId);

        // then
        assertEquals(result.get(0).getTravelPlace().getPlaceId(), placeId);
    }
}
