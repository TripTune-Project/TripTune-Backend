package com.triptune.bookmark.service;

import com.triptune.bookmark.dto.request.BookmarkRequest;
import com.triptune.bookmark.entity.Bookmark;
import com.triptune.bookmark.enumclass.BookmarkSortType;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkService {

    private final MemberRepository memberRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final BookmarkRepository bookmarkRepository;

    public void createBookmark(String email, BookmarkRequest bookmarkRequest) {
        if (isExistBookmark(email, bookmarkRequest.getPlaceId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_BOOKMARK);
        }

        Member member = getMemberByEmail(email);
        TravelPlace travelPlace = findTravelPlaceByPlaceId(bookmarkRequest.getPlaceId());

        Bookmark bookmark = Bookmark.from(member, travelPlace);
        bookmarkRepository.save(bookmark);

        travelPlace.increaseBookmarkCnt();
    }


    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private TravelPlace findTravelPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findById(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }

    public void deleteBookmark(String email, Long placeId) {
        if (!isExistBookmark(email, placeId)){
            throw new DataNotFoundException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteByMember_EmailAndTravelPlace_PlaceId(email, placeId);

        TravelPlace travelPlace = findTravelPlaceByPlaceId(placeId);
        travelPlace.decreaseBookmarkCnt();
    }

    private boolean isExistBookmark(String email, Long placeId){
        return bookmarkRepository.existsByMember_EmailAndTravelPlace_PlaceId(email, placeId);
    }

    public Page<TravelPlace> getBookmarkTravelPlaces(String email, Pageable pageable, BookmarkSortType sortType) {
        return bookmarkRepository.findBookmarksByEmail(email, pageable, sortType);
    }
}
