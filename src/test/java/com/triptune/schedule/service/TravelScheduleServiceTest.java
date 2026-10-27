package com.triptune.schedule.service;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.schedule.exception.ConflictScheduleVersionException;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.chat.fixture.ChatMessageFixture;
import com.triptune.attendee.fixture.TravelAttendeeFixture;
import com.triptune.route.fixture.TravelRouteFixture;
import com.triptune.schedule.dto.response.*;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.chat.entity.ChatMessage;
import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.attendee.enums.AttendeeRole;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.chat.repository.ChatMessageRepository;
import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.route.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.page.SchedulePageResponse;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import org.apache.http.auth.AUTH;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.triptune.attendee.enums.AttendeePermission.*;
import static com.triptune.attendee.enums.AttendeeRole.AUTHOR;
import static com.triptune.attendee.enums.AttendeeRole.GUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TravelScheduleServiceTest {
    @InjectMocks private TravelScheduleService travelScheduleService;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private TravelRouteRepository travelRouteRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private S3ObjectManager s3ObjectManager;
    @Mock private ApplicationEventPublisher publisher;

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace placeWithoutThumb;

    private TravelImage place1Thumb;
    private String place1ThumbUrl;

    private Member curMember;
    private Member member2;

    private ProfileImage curMemberProfileImage;
    private ProfileImage member2ProfileImage;

    private String curMemberProfileUrl;
    private String member2ProfileUrl;


    @BeforeEach
    void setUp(){
        Country country = CountryFixture.createCountry();
        City city = CityFixture.createSeoul(country);
        District district = DistrictFixture.createDistrict(city, "중구");
        ApiContentType apiContentType = ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS);

        curMemberProfileImage = ProfileImageFixture.createProfileImage("member1Image");
        curMember = MemberFixture.createNativeTypeMemberWithId(1L, "curMember@email.com", curMemberProfileImage);
        curMemberProfileUrl = "http://www.test.com/" + curMemberProfileImage.getS3ObjectKey();

        member2ProfileImage = ProfileImageFixture.createProfileImage("member2Image");
        member2 = MemberFixture.createNativeTypeMemberWithId(2L, "member2@email.com", member2ProfileImage);
        member2ProfileUrl = "http://www.test.com/" + member2ProfileImage.getS3ObjectKey();

        place1WithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                "여행지1"
        );
        place1Thumb = TravelImageFixture.createTravelImage(place1WithThumb, "test1", true);
        TravelImageFixture.createTravelImage(place1WithThumb, "test2", false);
        place1ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place2WithThumb = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                "여행지3"
        );
        TravelImageFixture.createTravelImage(place2WithThumb, "test1", true);
        TravelImageFixture.createTravelImage(place2WithThumb, "test2", false);

        placeWithoutThumb = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiContentType,
                "여행지2"
        );
    }


    @Test
    @DisplayName("내 일정 목록 조회")
    void getAllTravelSchedules(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("당일 여행");
        TravelAttendee schedule1Author = TravelAttendeeFixture.createAuthorAttendee(schedule1, curMember);

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createSchedule("역사 여행");
        TravelAttendee schedule3Author = TravelAttendeeFixture.createAuthorAttendee(schedule3, curMember);

        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule1, schedule1Author, schedule1Author, place1Thumb.getS3ObjectKey()),
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, null),
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule3, schedule3Author, schedule3Author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, PageUtils.schedulePageable(1), schedules.size());


        when(travelScheduleRepository.findTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);
        when(s3ObjectManager.generateS3ObjectUrl(place1Thumb.getS3ObjectKey()))
                .thenReturn(place1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(null))
                .thenReturn(null);
        when(s3ObjectManager.generateS3ObjectUrl(curMemberProfileImage.getS3ObjectKey()))
                .thenReturn(curMemberProfileUrl);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllTravelSchedules(1, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent())
                .extracting(
                        ScheduleInfoResponse::getScheduleName,
                        ScheduleInfoResponse::getThumbnailUrl,
                        ScheduleInfoResponse::getRole,
                        s -> s.getAuthor().getProfileUrl()
                )
                .containsExactly(
                        tuple(schedule1.getScheduleName(), place1ThumbUrl, AUTHOR, curMemberProfileUrl),
                        tuple(schedule2.getScheduleName(), null, GUEST, member2ProfileUrl),
                        tuple(schedule3.getScheduleName(), null, AUTHOR, curMemberProfileUrl)
                );
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 공유된 일정이 없는 경우")
    void getAllTravelSchedules_noSharedScheduleData(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createSchedule("역사 여행");
        TravelAttendee author = TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule, author, author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllTravelSchedules(1, curMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(curMember.getNickname());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 일정 데이터 없는 경우")
    void getAllTravelSchedules_noScheduleData(){
        // given
        Page<ScheduleInfoQueryDto> emptySchedulePage = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );

        when(travelScheduleRepository.findTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllTravelSchedules(1, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).hasSize(0);
        assertThat(response.getContent()).isEmpty();
        verify(travelRouteRepository, times(0)).findAllByScheduleId(any(), any());
    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 썸네일 없는 경우")
    void getAllTravelSchedules_noImageThumbnail(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("당일 여행");
        TravelAttendee schedule1Author = TravelAttendeeFixture.createAuthorAttendee(schedule1, curMember);

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createSchedule("역사 여행");
        TravelAttendee schedule3Author = TravelAttendeeFixture.createAuthorAttendee(schedule3, curMember);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule1, schedule1Author, schedule1Author, null),
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, null),
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule3, schedule3Author, schedule3Author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);
        when(s3ObjectManager.generateS3ObjectUrl(null))
                .thenReturn(null);
        when(s3ObjectManager.generateS3ObjectUrl(curMemberProfileImage.getS3ObjectKey()))
                .thenReturn(curMemberProfileUrl);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllTravelSchedules(1, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent())
                .extracting(
                        ScheduleInfoResponse::getScheduleName,
                        ScheduleInfoResponse::getThumbnailUrl,
                        ScheduleInfoResponse::getRole,
                        s -> s.getAuthor().getProfileUrl()
                )
                .containsExactly(
                        tuple(schedule1.getScheduleName(), null, AUTHOR, curMemberProfileUrl),
                        tuple(schedule2.getScheduleName(), null, GUEST, member2ProfileUrl),
                        tuple(schedule3.getScheduleName(), null, AUTHOR, curMemberProfileUrl)
                );

    }



    @Test
    @DisplayName("공유된 일정 목록 조회")
    void getSharedTravelSchedules(){
        // given
        TravelScheduleFixture.createSchedule("당일 여행");
        TravelScheduleFixture.createSchedule("역사 여행");

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(3);
        when(s3ObjectManager.generateS3ObjectUrl(null))
                .thenReturn(null);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.getSharedTravelSchedules(1, curMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();
        assertThat(content.get(0).getAuthor().getProfileUrl()).isEqualTo(member2ProfileUrl);
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 일정 데이터 없는 경우")
    void getSharedTravelSchedules_noScheduleData(){
        // given
        Page<ScheduleInfoQueryDto> emptySchedulePage = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );

        when(travelScheduleRepository.findSharedTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getSharedTravelSchedules(1, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).hasSize(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getSharedTravelSchedules_noImageThumbnail(){
        // given
        TravelScheduleFixture.createSchedule("당일 여행");

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);
        when(s3ObjectManager.generateS3ObjectUrl(null))
                .thenReturn(null);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getSharedTravelSchedules(1, curMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();
        assertThat(content.get(0).getAuthor().getProfileUrl()).isEqualTo(member2ProfileUrl);
    }


    @Test
    @DisplayName("수정 권한 있는 내 일정 목록 조회")
    void getEnableEditTravelSchedules(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");
        TravelSchedule schedule2 = TravelScheduleFixture.createScheduleWithId(2L, "도보 여행");

        Page<TravelSchedule> schedulePage = PageUtils.createPage(
                List.of(schedule1, schedule2),
                PageUtils.scheduleModalPageable(1),
                2
        );

        when(travelScheduleRepository.findEnableEditTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelAttendeeRepository.findAuthorNicknameByScheduleId(anyLong()))
                .thenReturn(curMember.getNickname())
                .thenReturn(member2.getNickname());

        // when
        Page<ScheduleOverviewResponse> response = travelScheduleService.getEnableEditTravelSchedules(1, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent())
                .extracting(
                        ScheduleOverviewResponse::getScheduleName,
                        ScheduleOverviewResponse::getAuthor,
                        ScheduleOverviewResponse::getRouteVersion
                )
                .containsExactly(
                        tuple(schedule1.getScheduleName(), curMember.getNickname(), schedule1.getRouteVersion()),
                        tuple(schedule2.getScheduleName(), member2.getNickname(), schedule2.getRouteVersion())
                );
    }

    @Test
    @DisplayName("수정 권한 있는 내 일정 목록 조회 시 일정 데이터 존재하지 않는 경우")
    void getEnableEditSchedule_noSchedulesData(){
        // given
        Page<TravelSchedule> schedulePage = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.scheduleModalPageable(1),
                0
        );

        when(travelScheduleRepository.findEnableEditTravelSchedules(any(Pageable.class), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);

        // when
        Page<ScheduleOverviewResponse> response = travelScheduleService.getEnableEditTravelSchedules(1, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("내 일정 검색")
    void searchAllTravelSchedules(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("당일 여행");
        TravelAttendee schedule1Author = TravelAttendeeFixture.createAuthorAttendee(schedule1, curMember);

        String keyword = "서울";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule1, schedule1Author, schedule1Author, place1Thumb.getS3ObjectKey())
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());


        when(travelScheduleRepository.searchTravelSchedules(any(Pageable.class), eq(keyword), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(schedules.size());
        when(s3ObjectManager.generateS3ObjectUrl(place1Thumb.getS3ObjectKey()))
                .thenReturn(place1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(curMemberProfileImage.getS3ObjectKey()))
                .thenReturn(curMemberProfileUrl);


        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllTravelSchedules(1, keyword, curMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1ThumbUrl);
        assertThat(content.get(0).getAuthor().getProfileUrl()).isEqualTo(curMemberProfileUrl);

    }

    @Test
    @DisplayName("내 일정 검색 시 검색 결과가 없는 경우")
    void searchAllTravelSchedules_emptyResult(){
        // given
        String keyword = "ㅁㄴㅇㄹ";

        Page<ScheduleInfoQueryDto> emptySchedulePage = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );

        when(travelScheduleRepository.searchTravelSchedules(any(Pageable.class), eq(keyword), eq(curMember.getMemberId())))
                .thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllTravelSchedules(1, keyword, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("내 일정 검색 시 이미지 썸네일 없는 경우")
    void searchAllTravelSchedules_noImageThumbnail(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("당일 여행");
        TravelAttendee schedule1Author = TravelAttendeeFixture.createAuthorAttendee(schedule1, curMember);

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule1, schedule1Author, schedule1Author, null),
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedules(any(Pageable.class), eq(keyword), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(schedules.size());
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(1);
        when(s3ObjectManager.generateS3ObjectUrl(null))
                .thenReturn(null);
        when(s3ObjectManager.generateS3ObjectUrl(curMemberProfileImage.getS3ObjectKey()))
                .thenReturn(curMemberProfileUrl);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllTravelSchedules(1, keyword, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting(
                        ScheduleInfoResponse::getScheduleName,
                        ScheduleInfoResponse::getThumbnailUrl,
                        ScheduleInfoResponse::getRole,
                        s -> s.getAuthor().getProfileUrl()
                )
                .containsExactly(
                        tuple(schedule1.getScheduleName(), null, AUTHOR, curMemberProfileUrl),
                        tuple(schedule2.getScheduleName(),  null, GUEST, member2ProfileUrl)
                );
    }


    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedTravelSchedules(){
        // given
        TravelScheduleFixture.createSchedule("당일 여행");

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, place1Thumb.getS3ObjectKey())
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedules(any(Pageable.class), eq(keyword), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(schedules.size());
        when(s3ObjectManager.generateS3ObjectUrl(place1Thumb.getS3ObjectKey()))
                .thenReturn(place1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchSharedTravelSchedules(1, keyword, curMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1ThumbUrl);
        assertThat(content.get(0).getRole()).isEqualTo(GUEST);
        assertThat(content.get(0).getAuthor().getProfileUrl()).isEqualTo(member2ProfileUrl);
    }

    @Test
    @DisplayName("공유된 일정 검색 시 일정 데이터 없는 경우")
    void searchSharedTravelSchedules_noScheduleData(){
        // given
        String keyword = "테스트";

        Page<ScheduleInfoQueryDto> emptySchedulePage = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );

        when(travelScheduleRepository.searchSharedTravelSchedules(any(Pageable.class), eq(keyword), eq(curMember.getMemberId())))
                .thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchSharedTravelSchedules(1, keyword, curMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공유된 일정 검색 시 이미지 썸네일 없는 경우")
    void searchSharedTravelSchedules_noImageThumbnail(){
        // given
        TravelScheduleFixture.createSchedule("당일 여행");

        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("도보 여행");
        TravelAttendee schedule2Author = TravelAttendeeFixture.createAuthorAttendee(schedule2, member2);
        TravelAttendee schedule2Current = TravelAttendeeFixture.createGuestAttendee(schedule2, curMember, CHAT);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<ScheduleInfoQueryDto> schedules = List.of(
                TravelScheduleFixture.createScheduleInfoQueryDto(schedule2, schedule2Current, schedule2Author, null)
        );
        Page<ScheduleInfoQueryDto> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedules(any(Pageable.class), eq(keyword), eq(curMember.getMemberId())))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(curMember.getMemberId()))
                .thenReturn(2);
        when(travelScheduleRepository.countSharedTravelSchedules(curMember.getMemberId()))
                .thenReturn(schedules.size());
        when(s3ObjectManager.generateS3ObjectUrl(null))
                .thenReturn(null);
        when(s3ObjectManager.generateS3ObjectUrl(member2ProfileImage.getS3ObjectKey()))
                .thenReturn(member2ProfileUrl);


        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchSharedTravelSchedules(1, keyword, curMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getThumbnailUrl()).isNull();
        assertThat(content.get(0).getRole()).isEqualTo(GUEST);
        assertThat(content.get(0).getAuthor().getProfileUrl()).isEqualTo(member2ProfileUrl);

    }

    @Test
    @DisplayName("일정 생성")
    void createTravelSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleCreateRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        when(memberRepository.findById(curMember.getMemberId()))
                .thenReturn(Optional.of(curMember));
        when(travelScheduleRepository.save(any(TravelSchedule.class)))
                .thenReturn(schedule);

        // when
        travelScheduleService.createTravelSchedule(request, curMember.getMemberId());

        // then
        verify(memberRepository).findById(anyLong());
        verify(travelScheduleRepository).save(any());
    }

    @Test
    @DisplayName("일정 생성 시 저장된 회원 정보 없어 예외 발생")
    void createTravelSchedule_memberNotFound(){
        // given
        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleCreateRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.createTravelSchedule(request, 1004L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

    }

    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");

        List<PlaceQueryDto> places = List.of(
                TravelPlaceFixture.createPlaceQueryDto(place1WithThumb, place1Thumb.getS3ObjectKey()),
                TravelPlaceFixture.createPlaceQueryDto(placeWithoutThumb, null)
        );
        Page<PlaceQueryDto> placePages = PageUtils.createPage(places, PageUtils.defaultPageable(1), places.size());

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findNearbyTravelPlacesFromJungGu(any(Pageable.class)))
                .thenReturn(placePages);
        when(s3ObjectManager.generateS3ObjectUrl(place1Thumb.getS3ObjectKey()))
                .thenReturn(place1ThumbUrl);

        // when
        ScheduleDetailResponse response = travelScheduleService.getScheduleDetail(schedule.getScheduleId(), 1);

        // then
        assertThat(response.getScheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(response.getScheduleVersion()).isEqualTo(schedule.getScheduleVersion());
        assertThat(response.getCreatedAt()).isEqualTo(schedule.getCreatedAt());
        assertThat(response.getPlaceList().getTotalElements()).isEqualTo(2);
        assertThat(response.getPlaceList().getContent())
                .extracting(
                        PlaceResponse::getPlaceName,
                        PlaceResponse::getThumbnailUrl
                )
                .containsExactly(
                        tuple(place1WithThumb.getPlaceName(), place1ThumbUrl),
                        tuple(placeWithoutThumb.getPlaceName(), null)
                );

    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 없는 경우")
    void getScheduleDetail_emptyResult(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");

        Page<PlaceQueryDto> placePages = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findNearbyTravelPlacesFromJungGu(any(Pageable.class)))
                .thenReturn(placePages);

        // when
        ScheduleDetailResponse response = travelScheduleService.getScheduleDetail(schedule.getScheduleId(), 1);

        // then
        assertThat(response.getScheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(response.getScheduleVersion()).isEqualTo(schedule.getScheduleVersion());
        assertThat(response.getCreatedAt()).isEqualTo(schedule.getCreatedAt());
        assertThat(response.getPlaceList().getTotalElements()).isEqualTo(0);
        assertThat(response.getPlaceList().getContent()).isEmpty();
    }


    @Test
    @DisplayName("일정 상세 조회 시 일정을 찾을 수 없어 예외 발생")
    void getScheduleDetail_scheduleNotFound(){
        // given
        when(travelScheduleRepository.findById(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.getScheduleDetail(1004L, 1));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("일정 수정")
    void updateTravelSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");
        TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);

        int scheduleVersion = schedule.getScheduleVersion();

        ScheduleUpdateRequest request = TravelScheduleFixture.createScheduleUpdateRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                scheduleVersion
        );

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));
        when(travelScheduleRepository.updateSchedule(schedule.getScheduleId(), request.getScheduleName(), request.getStartDate(), request.getEndDate(), request.getScheduleVersion()))
                .thenReturn(1);

        // when
        ScheduleUpdateResponse response
                = travelScheduleService.updateTravelSchedule(request, curMember.getMemberId(), schedule.getScheduleId());

        // then
        assertThat(response.getScheduleVersion()).isEqualTo(scheduleVersion+1);
    }


    @Test
    @DisplayName("일정 수정 시 일정 데이터 없어 예외 발생")
    void updateSchedule_Travel_scheduleNotFound(){
        // given
        ScheduleUpdateRequest request = TravelScheduleFixture.createScheduleUpdateRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                3
        );

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.updateTravelSchedule(request, 1L, 1004L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
    }


    @Test
    @DisplayName("일정 수정 시 요청 회원에게 접근 권한이 없어 예외 발생")
    void updateSchedule_forbiddenTravelTravelSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");
        TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);

        ScheduleUpdateRequest request = TravelScheduleFixture.createScheduleUpdateRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                3
        );

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.updateTravelSchedule(request, 1004L, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);

    }

    @Test
    @DisplayName("일정 수정 시 요청 회원에게 수정 권한이 없어 예외 발생")
    void updateTravelSchedule_forbiddenEdit(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);
        TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ);

        ScheduleUpdateRequest request = TravelScheduleFixture.createScheduleUpdateRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                3
        );

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.updateTravelSchedule(request, member2.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }



    @Test
    @DisplayName("일정 수정 시 요청 값에서 버전 충돌로 예외 발생")
    void updateTravelSchedule_conflictScheduleVersion_byRequest(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");
        TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);

        ScheduleUpdateRequest request = TravelScheduleFixture.createScheduleUpdateRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                schedule.getScheduleVersion()+1
        );

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));

        // when
        ConflictScheduleVersionException fail = assertThrows(ConflictScheduleVersionException.class,
                () -> travelScheduleService.updateTravelSchedule(request, curMember.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.CONFLICT_SCHEDULE_VERSION);
        assertThat(fail.getEventResponse().scheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(fail.getEventResponse().startDate()).isEqualTo(schedule.getStartDate());
        assertThat(fail.getEventResponse().endDate()).isEqualTo(schedule.getEndDate());
        assertThat(fail.getEventResponse().scheduleVersion()).isEqualTo(schedule.getScheduleVersion());
    }



    @Test
    @DisplayName("일정 수정 시 UPDATE 쿼리에서 버전 충돌로 예외 발생")
    void updateTravelSchedule_conflictScheduleVersion_bySQL(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");
        TravelAttendee.createAuthor(schedule, curMember);

        ScheduleUpdateRequest request = TravelScheduleFixture.createScheduleUpdateRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                schedule.getScheduleVersion()
        );

        when(travelScheduleRepository.findById(schedule.getScheduleId()))
                .thenReturn(Optional.of(schedule));
        when(travelScheduleRepository.updateSchedule(schedule.getScheduleId(), request.getScheduleName(), request.getStartDate(), request.getEndDate(), request.getScheduleVersion()))
                .thenReturn(0);

        // when
        ConflictScheduleVersionException fail = assertThrows(ConflictScheduleVersionException.class,
                () -> travelScheduleService.updateTravelSchedule(request, curMember.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.CONFLICT_SCHEDULE_VERSION);
    }


    @Test
    @DisplayName("일정 삭제")
    void deleteTravelSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");

        TravelAttendee author = TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);
        TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ);

        TravelRouteFixture.createRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createRoute(schedule, placeWithoutThumb, 2);
        TravelRouteFixture.createRoute(schedule, place2WithThumb, 3);

        ChatMessage message1 = ChatMessageFixture.createChatMessage(1L, 1L, "hello1");
        ChatMessage message2 = ChatMessageFixture.createChatMessage(1L, 1L, "hello2");
        ChatMessage message3 = ChatMessageFixture.createChatMessage(1L, 2L, "hello3");
        List<ChatMessage> chatMessages = List.of(message1, message2, message3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(schedule.getScheduleId(), curMember.getMemberId()))
                .thenReturn(Optional.of(author));
        when(chatMessageRepository.findAllByScheduleId(schedule.getScheduleId()))
                .thenReturn(chatMessages);

        // when
        assertDoesNotThrow(() -> travelScheduleService.deleteTravelSchedule(schedule.getScheduleId(), curMember.getMemberId()));

        // then
        verify(chatMessageRepository).deleteAllByScheduleId(1L);
    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 없는 경우")
    void deleteTravelSchedule_noChatMessageData(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");

        TravelAttendee author = TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);
        TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ);

        TravelRouteFixture.createRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createRoute(schedule, placeWithoutThumb, 2);
        TravelRouteFixture.createRoute(schedule, place2WithThumb, 3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(schedule.getScheduleId(), curMember.getMemberId()))
                .thenReturn(Optional.of(author));
        when(chatMessageRepository.findAllByScheduleId(schedule.getScheduleId()))
                .thenReturn(Collections.emptyList());

        // when
        assertDoesNotThrow(() -> travelScheduleService.deleteTravelSchedule(schedule.getScheduleId(), curMember.getMemberId()));

        // then
        verify(chatMessageRepository, times(0)).deleteAllByScheduleId(1L);
    }

    @Test
    @DisplayName("일정 삭제 시 작성자가 아닌 회원의 삭제 요청으로 인해 예외 발생")
    void deleteSchedule_notAuthor_forbiddenTravelTravelSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorAttendee(schedule, curMember);
        TravelAttendee guest = TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ);

        TravelRouteFixture.createRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createRoute(schedule, placeWithoutThumb, 2);
        TravelRouteFixture.createRoute(schedule, place2WithThumb, 3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(schedule.getScheduleId(), member2.getMemberId()))
                .thenReturn(Optional.of(guest));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.deleteTravelSchedule(schedule.getScheduleId(), member2.getMemberId()));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
    }


}
