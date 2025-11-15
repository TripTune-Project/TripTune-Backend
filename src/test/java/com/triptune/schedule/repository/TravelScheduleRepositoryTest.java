package com.triptune.schedule.repository;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
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
import com.triptune.global.config.QueryDSLConfig;
import com.triptune.global.util.PageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
@Transactional
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

    private TravelPlace travelPlace;
    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private Member member1;
    private Member member2;


    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, "test2", false));
        travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, apiContentType, List.of(travelImage1, travelImage2)));

        member1 = memberRepository.save(createMember(null, "member1@email.com"));
        member2 = memberRepository.save(createMember(null, "member2@email.com"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));
    }

    @Test
    @DisplayName("일정 생성")
    void createTravelSchedule() {
        // given
        TravelSchedule schedule = createTravelSchedule(null, "테스트 일정");

        // when
        travelScheduleRepository.save(schedule);

        // then
        assertThat(schedule.getScheduleId()).isNotNull();
        assertThat(schedule.getCreatedAt()).isEqualTo(schedule.getUpdatedAt());
    }

    @Test
    @DisplayName("전체 일정 목록 조회")
    void findTravelSchedulesByEmail(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2));
        schedule3.setTravelAttendees(List.of(attendee3));

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isNotNull();
        assertThat(content.get(0).getStartDate()).isNotNull();

    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터가 없는 경우")
    void findTravelSchedulesByEmailWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void findSharedTravelSchedulesByEmail(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2, attendee3));
        schedule3.setTravelAttendees(List.of(attendee4));

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findSharedTravelSchedulesByMemberId(pageable, member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isNotNull();
        assertThat(content.get(0).getStartDate()).isNotNull();

    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터가 없는 경우")
    void findSharedTravelSchedulesByEmailWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("전체 일정 갯수 조회")
    void countTravelSchedulesByEmail(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2));
        schedule3.setTravelAttendees(List.of(attendee3));

        // when
        Integer response = travelScheduleRepository.countTravelSchedulesByMemberId(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(2);
    }

    @Test
    @DisplayName("일정 갯수 조회 시 데이터가 없는 경우")
    void countTravelSchedulesByEmailWithoutData(){
        // given, when
        Integer response = travelScheduleRepository.countTravelSchedulesByMemberId(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("공유된 일정 갯수 조회")
    void countSharedTravelSchedulesByEmail(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2, attendee3));
        schedule3.setTravelAttendees(List.of(attendee4));

        // when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByMemberId(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(1);
    }

    @Test
    @DisplayName("공유된 일정 갯수 조회 시 데이터가 없는 경우")
    void countSharedTravelSchedulesByEmailWithoutData(){
        // given, when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByMemberId(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 일정 목록 중 검색")
    void searchTravelSchedulesByEmail(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        attendee4.getTravelSchedule().setScheduleName("테스트23");

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2, attendee3));
        schedule3.setTravelAttendees(List.of(attendee4));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, "2", member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule2.getStartDate());

    }

    @Test
    @DisplayName("전체 일정 목록 검색 시 데이터가 없는 경우")
    void searchTravelSchedulesByEmailWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, "ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }


    @Test
    @DisplayName("공유된 일정 목록 검색")
    void searchSharedTravelSchedulesByEmailAndKeyword(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2, attendee3));
        schedule3.setTravelAttendees(List.of(attendee4));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchSharedTravelSchedulesByMemberIdAndKeyword(pageable, "2", member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule2.getStartDate());

    }

    @Test
    @DisplayName("공유된 일정 목록 검색 시 데이터가 없는 경우")
    void searchSharedTravelSchedulesByEmailAndKeywordWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchSharedTravelSchedulesByMemberIdAndKeyword(pageable, "ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("전체 일정 키워드 검색 갯수 조회")
    void countTravelSchedulesByEmailAndKeyword(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2));
        schedule3.setTravelAttendees(List.of(attendee3));

        // when
        Integer response = travelScheduleRepository.countTravelSchedulesByMemberIdAndKeyword("2", member1.getMemberId());

        // then
        assertThat(response).isEqualTo(1);
    }

    @Test
    @DisplayName("일정 갯수 키워드 검색 시 데이터가 없는 경우")
    void countTravelSchedulesByEmailAndKeywordWithoutData(){
        // given
        // when
        Integer response = travelScheduleRepository.countTravelSchedulesByMemberIdAndKeyword("ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("공유된 일정 키워드 검색 갯수 조회")
    void countSharedTravelSchedulesByEmailAndKeyword(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1));
        schedule2.setTravelAttendees(List.of(attendee2, attendee3));
        schedule3.setTravelAttendees(List.of(attendee4));

        // when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword("2", member1.getMemberId());

        // then
        assertThat(response).isEqualTo(1);
    }

    @Test
    @DisplayName("공유된 일정 키워드 검색 갯수 조회 시 데이터가 없는 경우")
    void countSharedTravelSchedulesByEmailAndKeywordWithoutData(){
        // given, when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword("ㅁㄴㅇㄹ", member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("일정 삭제")
    void deleteById(){
        // given, when
        travelScheduleRepository.deleteById(schedule1.getScheduleId());

        // then
        // 일정 삭제됐는지 확인
        Optional<TravelSchedule> schedule = travelScheduleRepository.findById(schedule1.getScheduleId());
        assertThat(schedule).isEmpty();

        // 해당 일정의 참석자 정보 삭제됐는지 확인
        List<TravelAttendee> attendees = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule1.getScheduleId());
        assertThat(attendees).isEmpty();

        // 해당 일정의 여행 루트 정보 삭제됐는지 확인
        Page<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(PageUtils.defaultPageable(1), schedule1.getScheduleId());
        assertThat(routes.getTotalElements()).isEqualTo(0);
        assertThat(routes.getContent()).isEmpty();

        // 회원 정보 삭제 안됐는지 확인
        Optional<Member> member = memberRepository.findByEmail(member1.getEmail());
        assertThat(member).isNotEmpty();
    }


}
