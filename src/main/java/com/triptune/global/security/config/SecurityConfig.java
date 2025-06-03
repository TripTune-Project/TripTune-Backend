package com.triptune.global.security.config;

import com.triptune.global.security.exception.CustomAccessDeniedHandler;
import com.triptune.global.security.exception.CustomAuthenticationEntryPoint;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.security.oauth.handler.OAuth2FailHandler;
import com.triptune.global.security.oauth.handler.OAuth2SuccessHandler;
import com.triptune.global.security.oauth.CustomOAuth2UserService;
import com.triptune.global.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static com.triptune.global.security.config.SecurityConstants.AUTH_TRAVEL_LIST;
import static com.triptune.global.security.config.SecurityConstants.AUTH_WHITELIST;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailHandler oAuth2FailHandler;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // jwt 이용하기 위해서 세션 관리 상태 없음으로 구성
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(AUTH_TRAVEL_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )
                        // 로그인 성공 이후 회원 정보를 가져올 때의 설정을 담당
                        .userInfoEndpoint(c -> c.userService(customOAuth2UserService))
                        // 로그인 성공 시 핸들러
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailHandler)
                )
                .addFilterBefore(new JwtAuthFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        // 인증이 완료되었으나 해당 엔드포인트에 접근할 권한이 없을 경우 발생
                        .accessDeniedHandler(customAccessDeniedHandler)
                        // 인증이 안된 익명의 회원이 인증이 필요한 엔드포인트로 접근한 경우 발생
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .build();
    }


    @Bean
    public BCryptPasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

}
