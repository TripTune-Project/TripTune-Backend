package com.triptune.domain.email.service;

import com.triptune.domain.email.dto.EmailTemplateRequest;
import com.triptune.domain.email.dto.VerifyAuthRequest;
import com.triptune.domain.member.dto.request.FindPasswordRequest;
import com.triptune.global.enumclass.RedisKeyType;
import com.triptune.domain.member.repository.MemberRepository;
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

import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private static final String IMAGE_FOLDER_PATH = "static/images/";
    private static final String LOGO_IMAGE_NAME = "logo-removebg.png";
    private static final int LEFT_LIMIT = 48;  // '0'의 ASCII 값
    private static final int RIGHT_LIMIT = 122; // 'z'의 ASCII 값
    private static final int NUMERIC_LIMIT = 57; // 숫자의 최대값 '9'
    private static final int LOWERCASE_LIMIT = 97; // 소문자의 최소값 'a'
    private static final int UPPERCASE_LIMIT = 65; // 대문자의 최소값 'A'
    private static final int TARGET_STRING_LENGTH = 6; // 인증 코드 길이

    private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;
    private final JwtUtil jwtUtil;
    private final TemplateEngine templateEngine;


    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.change-password.url}")
    private String passwordURL;

    @Value("${spring.jwt.token.password-expiration-time}")
    private long passwordExpirationTime;

    @Value("${app.backend.email.certification-duration}")
    private long certificationDuration;

    @Value("${app.backend.email.verification-duration}")
    private long verificationDuration;

    @Value("${app.backend.email.reset-password-duration}")
    private long resetPasswordDuration;



    public boolean verifyAuthCode(VerifyAuthRequest verifyAuthRequest){
        String savedCode = redisUtil.getEmailData(RedisKeyType.AUTH_CODE, verifyAuthRequest.getEmail());

        if (savedCode != null && savedCode.equals(verifyAuthRequest.getAuthCode())){
            redisUtil.saveEmailData(RedisKeyType.VERIFIED, verifyAuthRequest.getEmail(), "true", verificationDuration);
            return true;
        }

        return false;
    }

    public void sendCertificationEmail(String email) throws MessagingException {
        deleteAuthCodeIfExists(email);

        String authCode = createAuthCode();

        EmailTemplateRequest templateRequest = new EmailTemplateRequest(
                "[TripTune] 이메일 인증 코드가 발급되었습니다.",
                email,
                Map.of("authCode", authCode),
                "certificationEmail"
        );

        MimeMessage emailForm = createEmailTemplate(templateRequest);

        javaMailSender.send(emailForm);
        redisUtil.saveEmailData(RedisKeyType.AUTH_CODE, email, authCode, certificationDuration);

        log.info("인증 이메일 전송 완료 : {}", email);
    }

    private void deleteAuthCodeIfExists(String email){
        if(redisUtil.existEmailData(RedisKeyType.AUTH_CODE, email)){
            redisUtil.deleteEmailData(RedisKeyType.AUTH_CODE, email);
        }
    }

    public void sendResetPasswordEmail(FindPasswordRequest findPasswordRequest) throws MessagingException {
        String passwordToken = jwtUtil.createToken(findPasswordRequest.getUserId(), passwordExpirationTime);
        String resetPasswordURL = passwordURL + passwordToken;

        EmailTemplateRequest templateRequest = new EmailTemplateRequest(
                "[TripTune] 비밀번호 재설정을 위한 안내 메일입니다.",
                findPasswordRequest.getEmail(),
                Map.of("resetPasswordURL", resetPasswordURL),
                "resetPasswordEmail"
        );

        MimeMessage emailForm = createEmailTemplate(templateRequest);

        javaMailSender.send(emailForm);
        redisUtil.saveExpiredData(passwordToken, findPasswordRequest.getEmail(), resetPasswordDuration);

        log.info("비밀번호 초기화 이메일 전송 완료 : {}", findPasswordRequest.getEmail());
    }

    private MimeMessage createEmailTemplate(EmailTemplateRequest templateRequest) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject(templateRequest.subject());
        helper.setTo(templateRequest.recipientEmail());
        helper.setCc(senderEmail);

        Context context = new Context();
        templateRequest.emailValues().forEach(context::setVariable);

        String emailHTML = templateEngine.process(templateRequest.templateName(), context);
        helper.setText(emailHTML, true);

        helper.addInline("image", new ClassPathResource(IMAGE_FOLDER_PATH + LOGO_IMAGE_NAME));

        return message;
    }

    public String createAuthCode() {
        Random random = new Random();

        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                .filter(i -> (i <= NUMERIC_LIMIT || i >= UPPERCASE_LIMIT) && (i <= 90 || i >= LOWERCASE_LIMIT))
                .limit(TARGET_STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


}
