package com.triptune.global.util;

import com.triptune.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class NicknameGenerator {
    private static final String[] adjectives = {
        "신나는", "즐거운", "반짝이는", "행복한", "포근한", "느긋한", "설레는", "따뜻한", "짜릿한", "평화로운",
            "놀라운", "자유로운", "유쾌한", "멋진", "빠른"
    };

    private static final String[] nouns = {
        "여행자", "방랑자", "나그네", "떠돌이", "탐험가", "모험가", "뚜벅이", "유랑객", "여행객", "유목민"
    };

    private static final Random random = new Random();

    private final MemberRepository memberRepository;


    public String createNickname(){
        while(true){
            String nickname = generateRandomNickname();

            if (!validateUniqueNickname(nickname)){
                return nickname;
            }
        }
    }


    private boolean validateUniqueNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    public static String generateRandomNickname(){
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];

        int number = random.nextInt(999) + 1;
        String formattedNumber = String.format("%03d", number);

        return adjective + noun + formattedNumber;
    }
}
