package com.triptune.bookmark.repository;

import com.triptune.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {
    boolean existsByMember_UserIdAndTravelPlace_PlaceId(@Param("userId") String userId, @Param("placeId") Long placeId);
    void deleteByMember_UserIdAndTravelPlace_PlaceId(@Param("userId") String userId, @Param("placeId") Long placeId);
    void deleteAllByMember_UserId(@Param("userId") String userId);
}
