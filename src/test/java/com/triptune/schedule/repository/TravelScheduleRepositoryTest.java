package com.triptune.schedule.repository;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
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

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
public class TravelScheduleRepositoryTest extends ScheduleTest {
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private TravelPlace placeWithThumbnail1;
    private TravelPlace placeWithThumbnail2;
    private TravelPlace placeWithoutThumbnail;

    private Member member1;
    private Member member2;


    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country, "서울"));
        District district = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));

        placeWithThumbnail1 = travelPlaceRepository.save(
                createTravelPlace(
                    country,
                    city,
                    district,
                    apiCategory,
                    apiContentType,
                    "여행지1"
                )
        );
        travelImageRepository.save(createTravelImage(placeWithThumbnail1, "test1", true));
        travelImageRepository.save(createTravelImage(placeWithThumbnail1, "test2", false));

        placeWithThumbnail2 = travelPlaceRepository.save(
                createTravelPlace(
                    country,
                    city,
                    district,
                    apiCategory,
                    apiContentType,
                    "여행지3"
                )
        );
        travelImageRepository.save(createTravelImage(placeWithThumbnail2, "test1", true));
        travelImageRepository.save(createTravelImage(placeWithThumbnail2, "test2", false));


        placeWithoutThumbnail = travelPlaceRepository.save(
                createTravelPlace(
                    country,
                    city,
                    district,
                    apiCategory,
                    apiContentType,
                    "여행지2"
                )
        );


        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage("member1"));
        member1 = memberRepository.save(createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage("member2"));
        member2 = memberRepository.save(createNativeTypeMember("member2@email.com", profileImage2));

    }

    @Test
    @DisplayName("일정 생성")
    void createTravelSchedule() {
        // given
        TravelSchedule schedule = createTravelSchedule("테스트 일정");

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
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule3.getScheduleName());
        assertThat(content.get(0).getTravelAttendees().size()).isEqualTo(1);
        assertThat(content.get(0).getTravelRoutes().size()).isEqualTo(0);
        assertThat(content.get(1).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(1).getTravelAttendees().size()).isEqualTo(2);
        assertThat(content.get(1).getTravelRoutes().size()).isEqualTo(0);
        assertThat(content.get(2).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(2).getStartDate()).isNotNull();
        assertThat(content.get(2).getTravelAttendees().size()).isEqualTo(2);
        assertThat(content.get(2).getTravelRoutes().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터가 없는 경우")
    void findTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void findSharedTravelSchedules() throws InterruptedException {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findSharedTravelSchedules(pageable, member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getTravelAttendees().size()).isEqualTo(2);
        assertThat(content.get(0).getTravelRoutes().size()).isEqualTo(0);
        assertThat(content.get(1).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(1).getStartDate()).isNotNull();
        assertThat(content.get(1).getTravelAttendees().size()).isEqualTo(2);
        assertThat(content.get(1).getTravelRoutes().size()).isEqualTo(3);

    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터가 없는 경우")
    void findSharedTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedules(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("전체 일정 갯수 조회")
    void countTravelSchedules(){
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

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
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


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
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchTravelSchedules(pageable, "1", member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule1.getStartDate());
        assertThat(content.get(0).getTravelAttendees().size()).isEqualTo(2);
        assertThat(content.get(0).getTravelRoutes().size()).isEqualTo(3);

    }

    @Test
    @DisplayName("전체 일정 검색 시 데이터가 없는 경우")
    void searchTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchTravelSchedules(pageable, "ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedTravelSchedules(){
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchSharedTravelSchedules(pageable, "1", member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule1.getStartDate());
        assertThat(content.get(0).getTravelAttendees().size()).isEqualTo(2);
        assertThat(content.get(0).getTravelRoutes().size()).isEqualTo(3);

    }

    @Test
    @DisplayName("공유된 일정 검색 시 데이터가 없는 경우")
    void searchSharedTravelSchedules_emptyResult(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchSharedTravelSchedules(pageable, "ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();
    }


    @Test
    @DisplayName("일정 삭제")
    void deleteById(){
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 3));

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
        Page<TravelRoute> deletedRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(PageUtils.defaultPageable(1), schedule.getScheduleId());
        assertThat(deletedRoutes.getTotalElements()).isEqualTo(0);
        assertThat(deletedRoutes.getContent()).isEmpty();

        // 회원 정보 삭제 안됐는지 확인
        Optional<Member> savedMember = memberRepository.findByEmail(member1.getEmail());
        assertThat(savedMember).isNotEmpty();
    }


}
