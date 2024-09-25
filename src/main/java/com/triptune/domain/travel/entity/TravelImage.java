package com.triptune.domain.travel.entity;

import com.triptune.domain.common.entity.File;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TravelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_image_id")
    private Long travelImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private TravelPlace travelPlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Builder
    public TravelImage(Long travelImageId, TravelPlace travelPlace, File file) {
        this.travelImageId = travelImageId;
        this.travelPlace = travelPlace;
        this.file = file;
    }
}
