package com.triptune.domain.bookmark.repository;

import com.triptune.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkCustomRepository {
    boolean existsByMember_UserIdAndTravelPlace_PlaceId(String userId, Long placeId);
    void deleteByMember_UserIdAndTravelPlace_PlaceId(String userId, Long placeId);
    void deleteAllByMember_UserId(String userId);
}
