package com.triptune.domain.email.service;

import com.triptune.domain.member.dto.FindDTO;
import com.triptune.domain.member.exception.DataExistException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.exception.ErrorCode;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import com.triptune.domain.email.dto.EmailDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.find-password.url}")
    private String findPasswordURL;

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
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }

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
     * 비밀번호 찾기 이메일 요청 : 비밀번호 변경 링크 제공
     * @param findPasswordDTO
     * @throws MessagingException
     */
    public void findPassword(FindDTO.FindPassword findPasswordDTO) throws MessagingException {
        MimeMessage emailForm = findPasswordEmailTemplate(findPasswordDTO);

        log.info("password recovery email sent completed");
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
                "<div style=\"background-color:#F2F2F2; padding:30px; width:80%; text-align:center;\">" +
                "<div>인증번호 :&emsp;<b>" + authCode + "</b></div>" +
                "<div style=\"font-size:3%; margin-top:3%; color:#FE2E2E;\">인증번호는 발송된 시점부터 5분간만 유효합니다.</div>" +
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
        redisUtil.setDataExpire(email, authCode, 300);

        return message;
    }


    /**
     * 비밀번호 변경 링크가 포함된 비밀번호 찾기 템플릿 생성
     * @param findPasswordDTO
     * @return 이메일 객체 {@link MimeMessage}
     * @throws MessagingException
     */
    private MimeMessage findPasswordEmailTemplate(FindDTO.FindPassword findPasswordDTO) throws MessagingException {
        String userId = findPasswordDTO.getUserId();
        String passwordToken = jwtUtil.createPasswordToken(userId);

        findPasswordURL += passwordToken;
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject("[TripTune] 비밀번호 재설정을 위한 안내 메일입니다.");
        helper.setTo(findPasswordDTO.getEmail());
        helper.setCc(senderEmail);

        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("findPasswordURL", findPasswordURL);

        Context context = new Context();
        emailValues.forEach((key, value) -> {
            context.setVariable(key, value);
        });

        String passwordMailHTML = templateEngine.process("passwordMail", context);
        helper.setText(passwordMailHTML, true);

        helper.addInline("image", new ClassPathResource("static/images/logo-removebg.png"));

        // 유효기간 1시간
        redisUtil.setDataExpire(passwordToken, findPasswordDTO.getEmail(), 3600);

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
