package com.triptune.schedule.repository;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
public class TravelAttendeeRepositoryTest extends ScheduleTest {
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private MemberRepository memberRepository;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private Member member1;
    private Member member2;



    @BeforeEach
    void setUp(){
        member1 = memberRepository.save(createMember(null, "member1@email.com"));
        member2 = memberRepository.save(createMember(null, "member2@email.com"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));

        travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));
        travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
    }

    @Test
    @DisplayName("참석자 생성")
    void createTravelAttendee() {
        // given
        TravelAttendee attendee = createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);

        // when
        travelAttendeeRepository.save(attendee);

        // then
        assertThat(attendee.getAttendeeId()).isNotNull();
        assertThat(attendee.getCreatedAt()).isEqualTo(attendee.getUpdatedAt());
    }

    @Test
    @DisplayName("일정 참가자인지 확인 true 반환")
    void existsAttendee_true(){
        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(schedule1.getScheduleId(), member1.getMemberId());

        // then
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("일정 참가자인지 확인 false 반환")
    void existsAttendee_false(){
        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(schedule1.getScheduleId(), member2.getMemberId());

        // then
        assertThat(response).isFalse();
    }

    @Test
    @DisplayName("일정 작성자 닉네임 조회")
    void findAuthorNicknameByScheduleId(){
        // given, when
        String response = travelAttendeeRepository.findAuthorNicknameByScheduleId(schedule1.getScheduleId());

        // then
        assertThat(response).isEqualTo(member1.getNickname());
    }


}
