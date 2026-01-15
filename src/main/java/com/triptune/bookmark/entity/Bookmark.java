package com.triptune.bookmark.entity;

import com.triptune.common.entity.BaseCreatedEntity;
import com.triptune.member.entity.Member;
import com.triptune.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private TravelPlace travelPlace;

    private Bookmark(Member member, TravelPlace travelPlace){
        this.member = member;
        this.travelPlace = travelPlace;
    }

    public static Bookmark createBookmark(Member member, TravelPlace travelPlace){
        return new Bookmark(member, travelPlace);
    }
}
