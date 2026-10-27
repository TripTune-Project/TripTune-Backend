package com.triptune.schedule.repository;

import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.global.websocket.event.ScheduleEvent;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.route.repository.TravelRouteRepository;
import com.triptune.attendee.fixture.TravelAttendeeFixture;
import com.triptune.route.fixture.TravelRouteFixture;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.config.QuerydslConfig;
import com.triptune.global.util.PageUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static com.triptune.attendee.enums.AttendeePermission.*;
import static com.triptune.attendee.enums.AttendeeRole.*;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
public class TravelScheduleRepositoryTest {
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private EntityManager em;

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace placeWithoutThumb;

    private TravelImage place1Thumb;
    private TravelImage place2Thumb;

    private Member member1;
    private Member member2;


    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createSeoul(country));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS));

        place1WithThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                    country,
                    city,
                    district,
                    apiContentType,
                    "여행지1"
                )
        );

        place1Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place1WithThumb, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place1WithThumb, "test2", false));

        place2WithThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                    country,
                    city,
                    district,
                    apiContentType,
                    "여행지3"
                )
        );

        place2Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place2WithThumb, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place2WithThumb, "test2", false));

        placeWithoutThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                    country,
                    city,
                    district,
                    apiContentType,
                    "여행지2"
                )
        );


        ProfileImage profileImage1 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member1"));
        member1 = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member2"));
        member2 = memberRepository.save(MemberFixture.createNativeTypeMember("member2@email.com", profileImage2));

    }


    @Test
    @DisplayName("전체 일정 목록 조회")
    void findTravelSchedules() {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getContent())
                .extracting(
                        ScheduleInfoQueryDto::getScheduleName,
                        ScheduleInfoQueryDto::getAttendeeRole,
                        ScheduleInfoQueryDto::getThumbnailS3ObjectKey,
                        ScheduleInfoQueryDto::getAuthorNickname,
                        ScheduleInfoQueryDto::getAuthorS3ObjectKey)
                .containsExactly(
                        tuple(schedule3.getScheduleName(), AUTHOR, null,
                                member1.getNickname(), member1.getProfileImage().getS3ObjectKey()),
                        tuple(schedule2.getScheduleName(), GUEST, place2Thumb.getS3ObjectKey(),
                                member2.getNickname(), member2.getProfileImage().getS3ObjectKey()),
                        tuple(schedule1.getScheduleName(), AUTHOR, place1Thumb.getS3ObjectKey(),
                                member1.getNickname(), member1.getProfileImage().getS3ObjectKey())
                );

    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터가 없는 경우")
    void findTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void findSharedTravelSchedules()  {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.findSharedTravelSchedules(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent())
                .extracting(
                        ScheduleInfoQueryDto::getScheduleName,
                        ScheduleInfoQueryDto::getAttendeeRole,
                        ScheduleInfoQueryDto::getThumbnailS3ObjectKey,
                        ScheduleInfoQueryDto::getAuthorNickname,
                        ScheduleInfoQueryDto::getAuthorS3ObjectKey)
                .containsExactly(
                        tuple(schedule2.getScheduleName(), GUEST, place2Thumb.getS3ObjectKey(),
                                member2.getNickname(), member2.getProfileImage().getS3ObjectKey()),
                        tuple(schedule1.getScheduleName(), AUTHOR, place1Thumb.getS3ObjectKey(),
                                member1.getNickname(), member1.getProfileImage().getS3ObjectKey())
                );


    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터가 없는 경우")
    void findSharedTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("전체 일정 갯수 조회")
    void countTravelSchedules(){
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        // when
        Integer response = travelScheduleRepository.countTravelSchedules(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(3);
    }

    @Test
    @DisplayName("일정 갯수 조회 시 데이터가 없는 경우")
    void countTravelSchedules_emptyResult(){
        // given, when
        Integer response = travelScheduleRepository.countTravelSchedules(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("공유된 일정 갯수 조회")
    void countSharedTravelSchedules(){
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));


        // when
        Integer response = travelScheduleRepository.countSharedTravelSchedules(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(2);
    }

    @Test
    @DisplayName("공유된 일정 갯수 조회 시 데이터가 없는 경우")
    void countSharedTravelSchedules_emptyResult(){
        // given, when
        Integer response = travelScheduleRepository.countSharedTravelSchedules(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 일정 검색")
    void searchTravelSchedules(){
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));


        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.searchTravelSchedules(pageable, "1", member1.getMemberId());

        // then
        List<ScheduleInfoQueryDto> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getAttendeeRole()).isEqualTo(AUTHOR);
        assertThat(content.get(0).getThumbnailS3ObjectKey()).isEqualTo(place1Thumb.getS3ObjectKey());
        assertThat(content.get(0).getAuthorNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getAuthorS3ObjectKey()).isEqualTo(member1.getProfileImage().getS3ObjectKey());
    }

    @Test
    @DisplayName("전체 일정 검색 시 데이터가 없는 경우")
    void searchTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.searchTravelSchedules(pageable, "ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedTravelSchedules(){
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.searchSharedTravelSchedules(pageable, "1", member1.getMemberId());

        // then
        List<ScheduleInfoQueryDto> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getAttendeeRole()).isEqualTo(AUTHOR);
        assertThat(content.get(0).getThumbnailS3ObjectKey()).isEqualTo(place1Thumb.getS3ObjectKey());
        assertThat(content.get(0).getAuthorNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getAuthorS3ObjectKey()).isEqualTo(member1.getProfileImage().getS3ObjectKey());
    }

    @Test
    @DisplayName("공유된 일정 검색 시 데이터가 없는 경우")
    void searchSharedTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.searchSharedTravelSchedules(pageable, "ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("일정 생성")
    void createTravelSchedule() {
        // given
        TravelSchedule schedule = TravelScheduleFixture.createSchedule("테스트 일정");

        // when
        TravelSchedule savedSchedule = travelScheduleRepository.save(schedule);

        // then
        em.flush();
        em.clear();

        TravelSchedule findSchedule = travelScheduleRepository.findById(savedSchedule.getScheduleId()).orElseThrow();

        assertThat(findSchedule.getScheduleId()).isEqualTo(savedSchedule.getScheduleId());
        assertThat(findSchedule.getScheduleVersion()).isZero();
        assertThat(findSchedule.getRouteVersion()).isZero();
        assertThat(findSchedule.getCreatedAt()).isEqualTo(findSchedule.getUpdatedAt());
    }

    @Test
    @DisplayName("routeVersion 조회")
    void findRouteVersionById() {
        // given
        TravelSchedule savedSchedule = travelScheduleRepository.save(
                TravelScheduleFixture.createSchedule("테스트 일정")
        );

        // when
        Integer routeVersion = travelScheduleRepository.findRouteVersionById(savedSchedule.getScheduleId())
                .orElseThrow();

        // then
        assertThat(routeVersion).isZero();
    }

    @Test
    @DisplayName("일정 수정")
    void updateSchedule() {
        // given
        TravelSchedule savedSchedule = travelScheduleRepository.save(
                TravelScheduleFixture.createSchedule("테스트 일정")
        );

        // when
        int updated = travelScheduleRepository.updateSchedule(
                savedSchedule.getScheduleId(),
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                savedSchedule.getScheduleVersion()
        );

        // then
        em.flush();
        em.clear();

        TravelSchedule findSchedule = travelScheduleRepository.findById(savedSchedule.getScheduleId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(findSchedule.getScheduleName()).isEqualTo("수정 테스트");
        assertThat(findSchedule.getScheduleVersion()).isEqualTo(1);
        assertThat(findSchedule.getRouteVersion()).isZero();

    }



    @Test
    @DisplayName("일정 수정 시 버전 충돌로 0 반환")
    void updateSchedule_conflictScheduleVersion() {
        // given
        TravelSchedule savedSchedule = travelScheduleRepository.save(
                TravelScheduleFixture.createSchedule("테스트 일정")
        );

        // when
        int updated = travelScheduleRepository.updateSchedule(
                savedSchedule.getScheduleId(),
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                1
        );

        // then
        assertThat(updated).isEqualTo(0);
    }


    @Test
    @DisplayName("여행루트 버전 증가")
    void increaseRouteVersion() {
        // given
        TravelSchedule savedSchedule = travelScheduleRepository.save(
                TravelScheduleFixture.createSchedule("테스트 일정")
        );

        // when
        int updated = travelScheduleRepository.increaseRouteVersion(
                savedSchedule.getScheduleId(),
                savedSchedule.getScheduleVersion()
        );

        // then
        em.flush();
        em.clear();

        TravelSchedule findSchedule = travelScheduleRepository.findById(savedSchedule.getScheduleId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(findSchedule.getScheduleVersion()).isZero();
        assertThat(findSchedule.getRouteVersion()).isEqualTo(1);

    }



    @Test
    @DisplayName("여행루트 버전 증가 시 버전 충돌로 0 반환")
    void increaseRouteVersion_conflictRouteVersion() {
        // given
        TravelSchedule savedSchedule = travelScheduleRepository.save(
                TravelScheduleFixture.createSchedule("테스트 일정")
        );

        // when
        int updated = travelScheduleRepository.increaseRouteVersion(
                savedSchedule.getScheduleId(),
                1
        );

        // then
        assertThat(updated).isEqualTo(0);
    }


}
