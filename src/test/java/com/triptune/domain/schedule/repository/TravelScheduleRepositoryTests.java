package com.triptune.domain.schedule.repository;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.config.QueryDSLConfig;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class TravelScheduleRepositoryTests extends ScheduleTest {
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final TravelRouteRepository travelRouteRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;
    private final MemberRepository memberRepository;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private Member member1;
    private Member member2;


    @Autowired
    public TravelScheduleRepositoryTests(TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, TravelPlacePlaceRepository travelPlaceRepository, TravelRouteRepository travelRouteRepository, CityRepository cityRepository, CountryRepository countryRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository, MemberRepository memberRepository) {
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
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, "test2", false));

        travelPlace.setApiContentType(apiContentType);
        travelPlace.setTravelImageList(new ArrayList<>(List.of(travelImage1, travelImage2)));

        member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));

    }


    @Test
    @DisplayName("findTravelSchedulesByAttendee(): 내가 참석한 여행지 목록 조회")
    void findTravelSchedulesByAttendee(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        member1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        member2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByAttendee(pageable, member1.getMemberId());

        // then
        List<TravelSchedule> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isNotNull();
        assertThat(content.get(0).getStartDate()).isNotNull();

    }

    @Test
    @DisplayName("findTravelSchedulesByAttendee(): 내가 참석한 여행지 목록 조회 시 데이터가 없는 경우")
    void findTravelSchedulesByAttendeeWithoutData(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);

        // when
        Page<TravelSchedule> response = travelScheduleRepository.findTravelSchedulesByAttendee(pageable, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("getTotalElementByTravelSchedules(): 내가 참석한 여행지 갯수 조회")
    void getTotalElementByTravelSchedules(){
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        member1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        member2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        // when
        Integer response = travelScheduleRepository.getTotalElementByTravelSchedules(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(2);
    }

    @Test
    @DisplayName("getTotalElementByTravelSchedules(): 내가 참석한 여행지 갯수 조회 시 데이터가 없는 경우")
    void getTotalElementByTravelSchedulesWithoutData(){
        // given
        // when
        Integer response = travelScheduleRepository.getTotalElementByTravelSchedules(member1.getMemberId());

        // then
        assertThat(response).isEqualTo(0);
    }

    @Test
    @DisplayName("deleteById(): 일정 삭제")
    void deleteById(){
        // given
        // when
        travelScheduleRepository.deleteById(schedule1.getScheduleId());

        // then
        // 일정 삭제됐는지 확인
        Optional<TravelSchedule> schedule = travelScheduleRepository.findByScheduleId(schedule1.getScheduleId());
        assertTrue(schedule.isEmpty());

        // 해당 일정의 참석자 정보 삭제됐는지 확인
        List<TravelAttendee> attendees = travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(schedule1.getScheduleId());
        assertTrue(attendees.isEmpty());

        // 해당 일정의 여행 루트 정보 삭제됐는지 확인
        Page<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(PageUtil.defaultPageable(1), schedule1.getScheduleId());
        assertEquals(routes.getTotalElements(), 0);
        assertTrue(routes.getContent().isEmpty());

        // 사용자 정보 삭제 안됐는지 확인
        Optional<Member> member = memberRepository.findByUserId(member1.getUserId());
        assertFalse(member.isEmpty());
    }


}
