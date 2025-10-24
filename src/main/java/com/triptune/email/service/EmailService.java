package com.triptune.email.service;

import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.dto.request.EmailTemplateRequest;
import com.triptune.email.dto.request.VerifyAuthRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.member.dto.request.FindPasswordRequest;
import com.triptune.global.redis.eums.RedisKeyType;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.redis.RedisService;
import com.triptune.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailService {

    private static final String IMAGE_FOLDER_PATH = "static/images/";
    private static final String LOGO_IMAGE_NAME = "logo-removebg.png";
    private static final int LEFT_LIMIT = 48;  // '0'의 ASCII 값
    private static final int RIGHT_LIMIT = 122; // 'z'의 ASCII 값
    private static final int NUMERIC_LIMIT = 57; // 숫자의 최대값 '9'
    private static final int LOWERCASE_LIMIT = 97; // 소문자의 최소값 'a'
    private static final int UPPERCASE_LIMIT = 65; // 대문자의 최소값 'A'
    private static final int TARGET_STRING_LENGTH = 6; // 인증 코드 길이

    private final RedisService redisService;
    private final JavaMailSender javaMailSender;
    private final JwtUtils jwtUtils;
    private final TemplateEngine templateEngine;
    private final MemberRepository memberRepository;


    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.change-password.url}")
    private String passwordURL;

    @Value("${app.backend.email.certification-duration}")
    private long certificationDuration;

    @Value("${app.backend.email.verification-duration}")
    private long verificationDuration;

    @Value("${app.backend.email.reset-password-duration}")
    private long resetPasswordDuration;


    public void sendCertificationEmail(EmailRequest emailRequest) throws MessagingException {
        String email = emailRequest.getEmail();
        validateUniqueEmail(email);
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
        redisService.saveEmailData(RedisKeyType.AUTH_CODE, email, authCode, certificationDuration);

        log.info("인증 이메일 전송 완료 : {}", email);
    }


    private void validateUniqueEmail(String email){
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }

    private void deleteAuthCodeIfExists(String email){
        if(redisService.existEmailData(RedisKeyType.AUTH_CODE, email)){
            redisService.deleteEmailData(RedisKeyType.AUTH_CODE, email);
        }
    }

    public String createAuthCode() {
        Random random = new Random();

        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                .filter(i -> (i <= NUMERIC_LIMIT || i >= UPPERCASE_LIMIT) && (i <= 90 || i >= LOWERCASE_LIMIT))
                .limit(TARGET_STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    public void verifyAuthCode(VerifyAuthRequest verifyAuthRequest){
        String savedCode = redisService.getEmailData(RedisKeyType.AUTH_CODE, verifyAuthRequest.getEmail());

        if(savedCode == null){
            throw new EmailVerifyException(ErrorCode.INVALID_VERIFIED_EMAIL);
        }

        if(!savedCode.equals(verifyAuthRequest.getAuthCode())){
            throw new EmailVerifyException(ErrorCode.INCORRECT_VERIFIED_EMAIL);
        }

        redisService.saveEmailData(RedisKeyType.VERIFIED, verifyAuthRequest.getEmail(), "true", verificationDuration);
    }


    public void sendResetPasswordEmail(FindPasswordRequest findPasswordRequest) throws MessagingException {
        String passwordToken = jwtUtils.createPasswordToken(findPasswordRequest.getEmail());
        String resetPasswordURL = passwordURL + passwordToken;

        EmailTemplateRequest templateRequest = new EmailTemplateRequest(
                "[TripTune] 비밀번호 재설정을 위한 안내 메일입니다.",
                findPasswordRequest.getEmail(),
                Map.of("resetPasswordURL", resetPasswordURL),
                "resetPasswordEmail"
        );

        MimeMessage emailForm = createEmailTemplate(templateRequest);

        javaMailSender.send(emailForm);
        redisService.saveExpiredData(passwordToken, findPasswordRequest.getEmail(), resetPasswordDuration);

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

}
