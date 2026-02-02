package com.triptune.travel.fixture;

import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;

public class TravelImageFixture {

    public static TravelImage createTravelImage(TravelPlace travelPlace, String fileName, boolean isThumbnail){
        return TravelImage.createTravelImage(
                travelPlace,
                "/test/" + fileName + ".jpg",
                "/img/test/" + fileName + ".jpg",
                fileName + "_original.jpg",
                fileName + ".jpg",
                "jpg",
                20,
                isThumbnail
        );
    }
}
