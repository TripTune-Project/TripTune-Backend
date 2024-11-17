package com.triptune.domain.schedule.repository;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.global.config.QueryDSLConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("test")
public class TravelAttendeeRepositoryTest extends ScheduleTest {
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final MemberRepository memberRepository;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private Member member1;
    private Member member2;


    @Autowired
    public TravelAttendeeRepositoryTest(TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, MemberRepository memberRepository) {
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.memberRepository = memberRepository;
    }


    @BeforeEach
    void setUp(){
        member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        member1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        member2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

    }

    @Test
    @DisplayName("일정 참가자인지 확인 true 반환")
    void existsAttendee_true(){
        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(schedule1.getScheduleId(), member1.getUserId());

        // then
        assertTrue(response);
    }

    @Test
    @DisplayName("일정 참가자인지 확인 false 반환")
    void existsAttendee_false(){
        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(schedule1.getScheduleId(), member2.getUserId());

        // then
        assertFalse(response);
    }


    @Test
    @DisplayName("일정 참가자인지 확인 false 반환")
    void existsAttendee_fals(){
        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_UserId(0L, member2.getUserId());

        // then
        assertFalse(response);
    }
}
