package com.triptune.bookmark.service;

import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookmarkService {

    private final MemberRepository memberRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void createBookmark(Long memberId, BookmarkRequest bookmarkRequest) {
        if (isExistBookmark(memberId, bookmarkRequest.getPlaceId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_BOOKMARK);
        }

        Member member = getMemberByMemberId(memberId);
        TravelPlace travelPlace = findTravelPlaceByPlaceId(bookmarkRequest.getPlaceId());

        Bookmark bookmark = Bookmark.createBookmark(member, travelPlace);
        bookmarkRepository.save(bookmark);

        travelPlace.increaseBookmarkCnt();
    }


    private Member getMemberByMemberId(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private TravelPlace findTravelPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findById(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }

    @Transactional
    public void deleteBookmark(Long memberId, Long placeId) {
        if (!isExistBookmark(memberId, placeId)){
            throw new DataNotFoundException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteByMember_MemberIdAndTravelPlace_PlaceId(memberId, placeId);

        TravelPlace travelPlace = findTravelPlaceByPlaceId(placeId);
        travelPlace.decreaseBookmarkCnt();
    }

    private boolean isExistBookmark(Long memberId, Long placeId){
        return bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(memberId, placeId);
    }

}
