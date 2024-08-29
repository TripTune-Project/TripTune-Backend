package com.triptune.domain.travel.controller;

import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.service.TravelService;
import com.triptune.global.response.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travels")
@Tag(name = "Travel Place", description = "여행지 탐색 관련 API")
@RequiredArgsConstructor
public class TravelApiController {

    private final TravelService travelService;

    @GetMapping("/list")
    @Operation(summary = "(미완)여행지 탐색 목록 조회", description = "여행지 탐색 메뉴에서 여행지 목록을 제공한다.")
    public ApiPageResponse<?> travelPlaceList(@RequestBody TravelLocationRequest travelLocationRequest, Pageable pageable){
        Page<TravelResponse> response = travelService.travelPlaceList(travelLocationRequest, pageable);
        return ApiPageResponse.okResponse(response);
    }

}
