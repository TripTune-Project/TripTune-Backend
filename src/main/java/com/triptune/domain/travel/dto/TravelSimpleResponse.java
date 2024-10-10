package com.triptune.domain.travel.dto;

import com.triptune.domain.common.entity.File;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.global.response.PageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class TravelSimpleResponse {
    private Long placeId;
    private String country;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private String placeName;
    private String thumbnailUrl;

    @Builder
    public TravelSimpleResponse(Long placeId, String country, String city, String district, String address, String detailAddress, String placeName, String thumbnailUrl) {
        this.placeId = placeId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.placeName = placeName;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setThumbnailUrl(TravelPlace travelPlace) {
        this.thumbnailUrl = File.getThumbnailUrl(travelPlace.getTravelImageList());
    }

    public static TravelSimpleResponse entityToDto(TravelPlace travelPlace){
        String thumbnailUrl = File.getThumbnailUrl(travelPlace.getTravelImageList());

        return TravelSimpleResponse.builder()
                .placeId(travelPlace.getPlaceId())
                .country(travelPlace.getCountry().getCountryName())
                .city(travelPlace.getCity().getCityName())
                .district(travelPlace.getDistrict().getDistrictName())
                .address(travelPlace.getAddress())
                .detailAddress(travelPlace.getDetailAddress())
                .placeName(travelPlace.getPlaceName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public static List<TravelSimpleResponse> entityListToDtoList(List<TravelPlace> travelPlaceList){
        return travelPlaceList.stream()
                .map(TravelSimpleResponse::entityToDto)
                .collect(Collectors.toList());
    }

    public static Page<TravelSimpleResponse> entityPageToDtoPage(Page<TravelPlace> travelPlacePage, Pageable pageable){
        List<TravelSimpleResponse> travelSimpleResponseList = new ArrayList<>();

        if (!travelPlacePage.getContent().isEmpty()){
            travelSimpleResponseList = TravelSimpleResponse.entityListToDtoList(travelPlacePage.getContent());
        }

        return new PageImpl<>(travelSimpleResponseList, pageable, travelPlacePage.getTotalElements());
    }


}
