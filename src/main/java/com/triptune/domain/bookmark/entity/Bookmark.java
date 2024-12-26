package com.triptune.domain.bookmark.entity;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.travel.entity.TravelPlace;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Bookmark {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Bookmark(Long bookmarkId, Member member, TravelPlace travelPlace, LocalDateTime createdAt) {
        this.bookmarkId = bookmarkId;
        this.member = member;
        this.travelPlace = travelPlace;
        this.createdAt = createdAt;
    }

    public static Bookmark from(Member member, TravelPlace travelPlace){
        return Bookmark.builder()
                .member(member)
                .travelPlace(travelPlace)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
