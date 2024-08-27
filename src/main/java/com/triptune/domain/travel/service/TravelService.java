package com.triptune.domain.travel.service;

import com.triptune.domain.travel.repository.TravelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelRepository travelRepository;

    public static void travelPlaceList() {
    }

}
