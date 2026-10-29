package com.triptune.bookmark.service;

import com.triptune.bookmark.dto.request.BookmarkCreateRequest;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
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
    public void createBookmark(Long memberId, BookmarkCreateRequest createRequest) {
        if (isExistBookmark(memberId, createRequest.getPlaceId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_BOOKMARK);
        }

        Member member = getMember(memberId);
        TravelPlace travelPlace = getPlace(createRequest.getPlaceId());

        Bookmark bookmark = Bookmark.createBookmark(member, travelPlace);
        bookmarkRepository.save(bookmark);

        travelPlace.increaseBookmarkCnt();
    }


    private Member getMember(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void deleteBookmark(Long memberId, Long placeId) {
        if (!isExistBookmark(memberId, placeId)){
            throw new DataNotFoundException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteByMemberIdAndPlaceId(memberId, placeId);

        TravelPlace travelPlace = getPlace(placeId);
        travelPlace.decreaseBookmarkCnt();
    }

    private TravelPlace getPlace(Long placeId){
        return travelPlaceRepository.findById(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }


    private boolean isExistBookmark(Long memberId, Long placeId){
        return bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(memberId, placeId);
    }

}
