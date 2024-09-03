package com.triptune.domail.travel.respository;

import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.repository.TravelImageFileRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class TravelImageFileRepositoryTests {

    @Autowired
    private TravelImageFileRepository travelImageFileRepository;

    @Test
    @DisplayName("성공: placeId를 이용해서 List<TravelImageFile> 조회")
    void successFindByPlaceId(){
        // given
        Long placeId = 1L;

        // when
        List<TravelImageFile> result = travelImageFileRepository.findByTravelPlacePlaceId(placeId);

        // then
        assertEquals(result.get(0).getTravelPlace().getPlaceId(), placeId);
    }
}
