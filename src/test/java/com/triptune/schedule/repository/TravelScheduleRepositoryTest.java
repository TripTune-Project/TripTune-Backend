package com.triptune.schedule.repository;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enumclass.ThemeType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
public class TravelScheduleRepositoryTest extends ScheduleTest {
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final TravelRouteRepository travelRouteRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;
    private final MemberRepository memberRepository;

    private TravelPlace travelPlace;
    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private Member member1;
    private Member member2;


    @Autowired
    public TravelScheduleRepositoryTest(TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, TravelPlaceRepository travelPlaceRepository, TravelRouteRepository travelRouteRepository, CityRepository cityRepository, CountryRepository countryRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository, MemberRepository memberRepository) {
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.travelPlaceRepository = travelPlaceRepository;
        this.travelRouteRepository = travelRouteRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.travelImageRepository = travelImageRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
        this.memberRepository = memberRepository;
    }

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

        member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));

    }


    @Test
    @DisplayName("전체 일정 목록 조회")
    void findTravelSchedulesByUserId(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isNotNull();
        assertThat(content.get(0).getStartDate()).isNotNull();

    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터가 없는 경우")
    void findTravelSchedulesByUserIdWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void findSharedTravelSchedulesByUserId(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2, attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, member1.getUserId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isNotNull();
        assertThat(content.get(0).getStartDate()).isNotNull();

    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터가 없는 경우")
    void findSharedTravelSchedulesByUserIdWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("전체 일정 갯수 조회")
    void countTravelSchedulesByUserId(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        // when
        Integer response = travelScheduleRepository.countTravelSchedulesByUserId(member1.getUserId());

        // then
        assertThat(response).isEqualTo(2);
    }

    @Test
    @DisplayName("일정 갯수 조회 시 데이터가 없는 경우")
    void countTravelSchedulesByUserIdWithoutData(){
        // given, when
        Integer response = travelScheduleRepository.countTravelSchedulesByUserId(member1.getUserId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("공유된 일정 갯수 조회")
    void countSharedTravelSchedulesByUserId(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2, attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        // when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByUserId(member1.getUserId());

        // then
        assertThat(response).isEqualTo(1);
    }

    @Test
    @DisplayName("공유된 일정 갯수 조회 시 데이터가 없는 경우")
    void countSharedTravelSchedulesByUserIdWithoutData(){
        // given, when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByUserId(member1.getUserId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 일정 목록 중 검색")
    void searchTravelSchedulesByUserId(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        attendee4.getTravelSchedule().setScheduleName("테스트23");

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2, attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, "2", member1.getUserId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule2.getStartDate());

    }

    @Test
    @DisplayName("전체 일정 목록 검색 시 데이터가 없는 경우")
    void searchTravelSchedulesByUserIdWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, "ㅁㄴㅇㄹ", member1.getUserId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }


    @Test
    @DisplayName("공유된 일정 목록 검색")
    void searchSharedTravelSchedulesByUserIdAndKeyword(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2, attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, "2", member1.getUserId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule2.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule2.getStartDate());

    }

    @Test
    @DisplayName("공유된 일정 목록 검색 시 데이터가 없는 경우")
    void searchSharedTravelSchedulesByUserIdAndKeywordWithoutData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, "ㅁㄴㅇㄹ", member1.getUserId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("전체 일정 키워드 검색 갯수 조회")
    void countTravelSchedulesByUserIdAndKeyword(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        // when
        Integer response = travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword("2", member1.getUserId());

        // then
        assertThat(response).isEqualTo(1);
    }

    @Test
    @DisplayName("일정 갯수 키워드 검색 시 데이터가 없는 경우")
    void countTravelSchedulesByUserIdAndKeywordWithoutData(){
        // given
        // when
        Integer response = travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword("ㅁㄴㅇㄹ", member1.getUserId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("공유된 일정 키워드 검색 갯수 조회")
    void countSharedTravelSchedulesByUserIdAndKeyword(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2, attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        // when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword("2", member1.getUserId());

        // then
        assertThat(response).isEqualTo(1);
    }

    @Test
    @DisplayName("공유된 일정 키워드 검색 갯수 조회 시 데이터가 없는 경우")
    void countSharedTravelSchedulesByUserIdAndKeywordWithoutData(){
        // given, when
        Integer response = travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword("ㅁㄴㅇㄹ", member1.getUserId());

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

        // 사용자 정보 삭제 안됐는지 확인
        Optional<Member> member = memberRepository.findByUserId(member1.getUserId());
        assertThat(member).isNotEmpty();
    }


}
