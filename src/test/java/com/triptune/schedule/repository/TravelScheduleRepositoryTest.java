package com.triptune.schedule.repository;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.repository.dto.RouteQueryDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static com.triptune.schedule.enums.AttendeePermission.*;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

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
        City city = cityRepository.save(CityFixture.createCity(country, "서울"));
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
    @DisplayName("일정 생성")
    void createTravelSchedule() {
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("테스트 일정");

        // when
        travelScheduleRepository.save(schedule);

        // then
        assertThat(schedule.getScheduleId()).isNotNull();
        assertThat(schedule.getCreatedAt()).isEqualTo(schedule.getUpdatedAt());
    }

    @Test
    @DisplayName("전체 일정 목록 조회")
    void findTravelSchedules() throws Exception{
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, CHAT));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        List<ScheduleInfoQueryDto> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule3.getScheduleName());
        assertThat(content.get(0).getAttendeeRole()).isEqualTo(AttendeeRole.AUTHOR);
        assertThat(content.get(0).getThumbnailS3ObjectKey()).isNull();
        assertThat(content.get(0).getAuthorNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getAuthorS3ObjectKey()).isEqualTo(member1.getProfileImage().getS3ObjectKey());

        assertThat(content.get(1).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(1).getAttendeeRole()).isEqualTo(AttendeeRole.GUEST);
        assertThat(content.get(1).getThumbnailS3ObjectKey()).isEqualTo(place2Thumb.getS3ObjectKey());
        assertThat(content.get(1).getAuthorNickname()).isEqualTo(member2.getNickname());
        assertThat(content.get(1).getAuthorS3ObjectKey()).isEqualTo(member2.getProfileImage().getS3ObjectKey());

        assertThat(content.get(2).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(2).getAttendeeRole()).isEqualTo(AttendeeRole.AUTHOR);
        assertThat(content.get(2).getThumbnailS3ObjectKey()).isEqualTo(place1Thumb.getS3ObjectKey());
        assertThat(content.get(2).getAuthorNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(2).getAuthorS3ObjectKey()).isEqualTo(member1.getProfileImage().getS3ObjectKey());

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, CHAT));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.findSharedTravelSchedules(pageable, member1.getMemberId());

        // then
        List<ScheduleInfoQueryDto> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getAttendeeRole()).isEqualTo(AttendeeRole.GUEST);
        assertThat(content.get(0).getThumbnailS3ObjectKey()).isEqualTo(place2Thumb.getS3ObjectKey());
        assertThat(content.get(0).getAuthorNickname()).isEqualTo(member2.getNickname());
        assertThat(content.get(0).getAuthorS3ObjectKey()).isEqualTo(member2.getProfileImage().getS3ObjectKey());

        assertThat(content.get(1).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(1).getAttendeeRole()).isEqualTo(AttendeeRole.AUTHOR);
        assertThat(content.get(1).getThumbnailS3ObjectKey()).isEqualTo(place1Thumb.getS3ObjectKey());
        assertThat(content.get(1).getAuthorNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(1).getAuthorS3ObjectKey()).isEqualTo(member1.getProfileImage().getS3ObjectKey());

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1));

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1));


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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1));


        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.searchTravelSchedules(pageable, "1", member1.getMemberId());

        // then
        List<ScheduleInfoQueryDto> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getAttendeeRole()).isEqualTo(AttendeeRole.AUTHOR);
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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<ScheduleInfoQueryDto> response = travelScheduleRepository.searchSharedTravelSchedules(pageable, "1", member1.getMemberId());

        // then
        List<ScheduleInfoQueryDto> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getAttendeeRole()).isEqualTo(AttendeeRole.AUTHOR);
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
    @DisplayName("일정 삭제")
    void deleteById(){
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, READ));

        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule, place2WithThumb, 3));

        // when
        travelScheduleRepository.deleteById(schedule.getScheduleId());

        // then
        // 일정 삭제됐는지 확인
        Optional<TravelSchedule> deletedSchedule = travelScheduleRepository.findById(schedule.getScheduleId());
        assertThat(deletedSchedule).isEmpty();

        // 해당 일정의 참석자 정보 삭제됐는지 확인
        List<TravelAttendee> deletedAttendees = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(deletedAttendees).isEmpty();

        // 해당 일정의 여행 루트 정보 삭제됐는지 확인
        Page<RouteQueryDto> deletedRoutes = travelRouteRepository.findAllByScheduleId(PageUtils.defaultPageable(1), schedule.getScheduleId());
        assertThat(deletedRoutes.getTotalElements()).isEqualTo(0);
        assertThat(deletedRoutes.getContent()).isEmpty();

        // 회원 정보 삭제 안됐는지 확인
        Optional<Member> savedMember = memberRepository.findByEmail(member1.getEmail());
        assertThat(savedMember).isNotEmpty();
    }


}
