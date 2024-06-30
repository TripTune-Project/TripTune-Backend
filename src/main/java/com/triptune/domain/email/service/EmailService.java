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

    public Boolean verify(EmailDTO.Verify emailDTO){
        String codeFoundByEmail = redisUtil.getData(emailDTO.getEmail());

        log.info("code found by email: {}", codeFoundByEmail);

        if (codeFoundByEmail == null){
            return false;
        }

        return codeFoundByEmail.equals(emailDTO.getAuthCode());
    }


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



    private MimeMessage createCertificationEmailForm(String email) throws MessagingException {
        String authCode = createCode();

        String content = "<br><br>" +
                "안녕하세요. TripTune 팀입니다.<br><br>" +
                "이메일 인증 절차에 따라 인증 번호를 발급해드립니다.<br>" +
                "아래의 인증 번호를 확인란에 입력해 인증을 완료해 주시기 바랍니다.<br><br>" +
                "<div style=\"background-color:#F2F2F2; padding:30px;  height:20px; width:60%; text-align:center;\">" +
                "인증번호 :&emsp;&emsp;<b>" + authCode + "</b>" +
                "</div>" +
                "<br><br>" +
                "이용해 주셔서 감사합니다." +
                "<br><br>";

        MimeMessage message = javaMailSender.createMimeMessage();
        // 수신자 메일 주소 설정
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[TripTune] 이메일 인증 코드가 발급되었습니다.");
        message.setFrom(senderEmail);
        message.setText(content, "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 60 * 30L);

        return message;
    }
    


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
