package com.triptune.bookmark.repository;

import com.triptune.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {
    boolean existsByMember_MemberIdAndTravelPlace_PlaceId(@Param("memberId") Long memberId, @Param("placeId") Long placeId);

    @Modifying
    @Query("delete from Bookmark b where b.member.memberId = :memberId and b.travelPlace.placeId = :placeId")
    void deleteByMemberIdAndPlaceId(@Param("memberId") Long memberId, @Param("placeId") Long placeId);

    @Modifying
    @Query("delete from Bookmark b where b.member.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
