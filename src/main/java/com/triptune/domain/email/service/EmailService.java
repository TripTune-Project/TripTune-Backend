package com.triptune.domain.email.service;

import com.triptune.domain.email.dto.VerifyAuthRequest;
import com.triptune.domain.member.dto.FindPasswordDTO;
import com.triptune.domain.common.exception.DataExistException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
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

    private final String IMAGE_FOLDER_PATH = "static/images/";

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.change-password.url}")
    private String passwordURL;

    @Value("${spring.jwt.token.password-expiration-time}")
    private long passwordExpirationTime;


    /**
     * 회원가입 인증번호 증명
     * @param verifyAuthRequest
     * @return 발급된 인증번호랑 사용자 입력이랑 같을 시 true, 다를 경우 false
     */
    public Boolean verify(VerifyAuthRequest verifyAuthRequest){
        String savedCode = redisUtil.getData(verifyAuthRequest.getEmail());

        log.info("code found by email: {}", savedCode);

        if (savedCode == null){
            return false;
        }

        return savedCode.equals(verifyAuthRequest.getAuthCode());
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
        MimeMessage emailForm = certificationEmailTemplate(email);

        // 이메일 발송
        log.info("certification number email sent completed");
        javaMailSender.send(emailForm);
    }


    /**
     * 비밀번호 찾기 이메일 요청 : 비밀번호 변경 링크 제공
     * @param findPasswordDTO
     * @throws MessagingException
     */
    public void findPassword(FindPasswordDTO findPasswordDTO) throws MessagingException {
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
    private MimeMessage certificationEmailTemplate(String email) throws MessagingException {
        String authCode = createCode();

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject("[TripTune] 이메일 인증 코드가 발급되었습니다.");
        helper.setTo(email);
        helper.setCc(senderEmail);

        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("authCode", authCode);

        Context context = new Context();
        emailValues.forEach((key, value) -> {
            context.setVariable(key, value);
        });

        String certificationMailHTML = templateEngine.process("certificationMail", context);

        helper.setText(certificationMailHTML, true);
        helper.addInline("image", new ClassPathResource(IMAGE_FOLDER_PATH + "logo-removebg.png"));

        // 유효기간 3분
        redisUtil.saveExpiredData(email, authCode, 300);

        return message;
    }


    /**
     * 비밀번호 변경 링크가 포함된 비밀번호 찾기 템플릿 생성
     * @param findPasswordDTO
     * @return 이메일 객체 {@link MimeMessage}
     * @throws MessagingException
     */
    private MimeMessage findPasswordEmailTemplate(FindPasswordDTO findPasswordDTO) throws MessagingException {
        String passwordToken = jwtUtil.createToken(findPasswordDTO.getUserId(), passwordExpirationTime);
        String changePasswordURL = passwordURL + passwordToken;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject("[TripTune] 비밀번호 재설정을 위한 안내 메일입니다.");
        helper.setTo(findPasswordDTO.getEmail());
        helper.setCc(senderEmail);

        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("changePasswordURL", changePasswordURL);

        Context context = new Context();
        emailValues.forEach((key, value) -> {
            context.setVariable(key, value);
        });

        String passwordMailHTML = templateEngine.process("passwordMail", context);
        helper.setText(passwordMailHTML, true);

        helper.addInline("image", new ClassPathResource(IMAGE_FOLDER_PATH + "logo-removebg.png"));

        // 유효기간 1시간
        redisUtil.saveExpiredData(passwordToken, findPasswordDTO.getEmail(), 3600);

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
