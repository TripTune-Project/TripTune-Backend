package com.triptune.domain.email.service;

import com.triptune.domain.email.dto.EmailTemplateRequest;
import com.triptune.domain.email.dto.VerifyAuthRequest;
import com.triptune.domain.member.dto.request.FindPasswordRequest;
import com.triptune.global.exception.DataExistException;
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
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private static final String IMAGE_FOLDER_PATH = "static/images/";
    private static final String LOGO_IMAGE_NAME = "logo-removebg.png";

    private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final TemplateEngine templateEngine;


    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.change-password.url}")
    private String passwordURL;

    @Value("${spring.jwt.token.password-expiration-time}")
    private long passwordExpirationTime;

    @Value("${app.email.certification-duration}")
    private long certificationDuration;

    @Value("${app.email.reset-password-duration}")
    private long resetPasswordDuration;


    public boolean verifyAuthCode(VerifyAuthRequest verifyAuthRequest){
        String savedCode = redisUtil.getData(verifyAuthRequest.getEmail());

        log.info("이메일에 저장된 인증 코드: {}", savedCode);

        if (savedCode == null){
            return false;
        }

        return savedCode.equals(verifyAuthRequest.getAuthCode());
    }


    public void sendCertificationEmail(String email) throws MessagingException {
        validateEmail(email);

        String authCode = createAuthCode();

        EmailTemplateRequest templateRequest = new EmailTemplateRequest(
                "[TripTune] 이메일 인증 코드가 발급되었습니다.",
                email,
                Map.of("authCode", authCode),
                "certificationEmail"
        );

        MimeMessage emailForm = createEmailTemplate(templateRequest);

        javaMailSender.send(emailForm);
        redisUtil.saveExpiredData(email, authCode, certificationDuration);

        log.info("인증 이메일 전송 완료");
    }

    public void validateEmail(String email){
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }

        if(redisUtil.existData(email)){
            redisUtil.deleteData(email);
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

        log.info("비밀번호 초기화 이메일 전송 완료");
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
