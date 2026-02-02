package com.triptune.schedule.repository;

import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.triptune.schedule.enums.AttendeePermission.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
public class TravelAttendeeRepositoryTest {
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private Member member1;
    private Member member2;



    @BeforeEach
    void setUp(){
        ProfileImage profileImage1 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member1Image"));
        member1 = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member2Image"));
        member2 = memberRepository.save(MemberFixture.createNativeTypeMember("member2@email.com", profileImage2));

        schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));
        schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트3"));
    }

    @Test
    @DisplayName("참석자 생성")
    void createTravelAttendee() {
        // given
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, READ));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member2));

        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member1, ALL);

        // when
        travelAttendeeRepository.save(guest);

        // then
        assertThat(guest.getAttendeeId()).isNotNull();
        assertThat(guest.getCreatedAt()).isEqualTo(guest.getUpdatedAt());
    }

    @Test
    @DisplayName("일정 참가자인지 확인 true 반환")
    void existsAttendee_true(){
        // given
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, READ));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member2));

        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(schedule1.getScheduleId(), member1.getMemberId());

        // then
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("일정 참가자인지 확인 false 반환")
    void existsAttendee_false(){
        // given
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, READ));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member2));


        // when
        boolean response = travelAttendeeRepository.existsByTravelSchedule_ScheduleIdAndMember_MemberId(schedule1.getScheduleId(), member2.getMemberId());

        // then
        assertThat(response).isFalse();
    }

    @Test
    @DisplayName("일정 작성자 닉네임 조회")
    void findAuthorNicknameByScheduleId(){
        // given
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, READ));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member2));

        // when
        String response = travelAttendeeRepository.findAuthorNicknameByScheduleId(schedule1.getScheduleId());

        // then
        assertThat(response).isEqualTo(member1.getNickname());
    }


}
