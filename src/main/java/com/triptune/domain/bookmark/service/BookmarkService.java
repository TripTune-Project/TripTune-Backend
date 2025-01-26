package com.triptune.domain.bookmark.service;

import com.triptune.domain.bookmark.dto.request.BookmarkRequest;
import com.triptune.domain.bookmark.entity.Bookmark;
import com.triptune.domain.bookmark.repository.BookmarkRepository;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkService {

    private final MemberRepository memberRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final BookmarkRepository bookmarkRepository;

    public void createBookmark(String userId, BookmarkRequest bookmarkRequest) {
        if (isExistBookmark(userId, bookmarkRequest.getPlaceId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_BOOKMARK);
        }

        Member member = findMemberByUserId(userId);
        TravelPlace travelPlace = findTravelPlaceByPlaceId(bookmarkRequest.getPlaceId());

        Bookmark bookmark = Bookmark.from(member, travelPlace);
        bookmarkRepository.save(bookmark);

        travelPlace.updateBookmarkCnt();
    }


    public Member findMemberByUserId(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public TravelPlace findTravelPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }

    public void deleteBookmark(String userId, Long placeId) {
        if (!isExistBookmark(userId, placeId)){
            throw new DataNotFoundException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteByMember_UserIdAndTravelPlace_PlaceId(userId, placeId);
    }

    public boolean isExistBookmark(String userId, Long placeId){
        return bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId(userId, placeId);
    }
}
