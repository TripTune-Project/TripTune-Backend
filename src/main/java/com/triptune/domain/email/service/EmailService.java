package com.triptune.domain.email.service;

import com.triptune.global.util.RedisUtil;
import com.triptune.domain.email.dto.EmailDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * 회원가입 인증번호 증명
     * @param emailDTO
     * @return 발급된 인증번호랑 사용자 입력이랑 같을 시 true, 다를 경우 false
     */
    public Boolean verify(EmailDTO.Verify emailDTO){
        String savedCode = redisUtil.getData(emailDTO.getEmail());

        log.info("code found by email: {}", savedCode);

        if (savedCode == null){
            return false;
        }

        return savedCode.equals(emailDTO.getAuthCode());
    }

    /**
     * 회원가입 인증번호 이메일 전송
     * @param email
     * @throws MessagingException
     */
    public void verifyRequest(String email) throws MessagingException {

        if(redisUtil.existData(email)){
            redisUtil.deleteData(email);
        }

        // 이메일 폼 생성
        MimeMessage emailForm = createCertificationEmailForm(email);

        // 이메일 발송
        log.info("certification number email sent completed");
        javaMailSender.send(emailForm);
    }

    /**
     * 아이디 찾기 이메일 전송
     * @param userId
     * @param email
     * @throws MessagingException
     */
    public void findId(String userId, String email) throws MessagingException {
        MimeMessage emailForm = createFindIdEmailForm(userId, email);

        log.info("id search email sent completed");
        javaMailSender.send(emailForm);
    }


    /**
     * 이메일 인증코드 생성 요청 후 회원가입 인증번호 이메일 폼 생성
     * @param email 수신자 이메일
     * @return 이메일 객체 {@link MimeMessage}
     * @throws MessagingException
     */
    private MimeMessage createCertificationEmailForm(String email) throws MessagingException {
        String authCode = createCode();

        String content = "<br>" +
                "안녕하세요. TripTune 팀입니다.<br><br>" +
                "이메일 인증 절차에 따라 인증 번호를 발급해드립니다.<br>" +
                "아래의 인증 번호를 확인란에 입력해 인증을 완료해 주시기 바랍니다.<br><br>" +
                "<div style=\"background-color:#F2F2F2; padding:30px; width:60%; text-align:center;\">" +
                "<div>인증번호 :&emsp;<b>123456</b></div>" +
                "<div style=\"font-size:3%; margin-top:3%; color:#FE2E2E;\">인증번호는 발송된 시점부터 3분간만 유효합니다.</div>" +
                "</div>" +
                "<br><br>" +
                "이용해 주셔서 감사합니다.<br><br>";

        MimeMessage message = javaMailSender.createMimeMessage();
        // 수신자 메일 주소 설정
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[TripTune] 이메일 인증 코드가 발급되었습니다.");
        message.setFrom(senderEmail);
        message.setText(content, "utf-8", "html");

        // 유효기간 3분
        redisUtil.setDataExpire(email, authCode, 180L);

        return message;
    }


    /**
     * 아이디 찾기 이메일 폼 생성
     * @param userId
     * @param email 수신자 이메일
     * @return 이메일 객체 {@link MimeMessage}
     * @throws MessagingException
     */
    private MimeMessage createFindIdEmailForm(String userId, String email) throws MessagingException {
        String content = "<br><br>" +
                "안녕하세요. TripTune 팀입니다.<br><br>" +
                "회원님께서 조회하신 아이디는 다음과 같습니다.<br><br>" +
                "<div style=\"background-color:#F2F2F2; padding:30px;  height:20px; width:60%; text-align:center;\">" +
                "아이디 :&emsp;&emsp;<b>" + userId + "</b>" +
                "</div>" +
                "<br><br>" +
                "이용해 주셔서 감사합니다." +
                "<br><br>";

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[TripTune] 요청하신 아이디 정보 안내드립니다.");
        message.setFrom(senderEmail);
        message.setText(content, "utf-8", "html");

        return message;
    }


    /**
     * 인증 코드 생성
     * @return 인증 코드
     */
    private String createCode() {
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
