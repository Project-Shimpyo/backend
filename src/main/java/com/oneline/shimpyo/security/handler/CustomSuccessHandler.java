package com.oneline.shimpyo.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.security.auth.PrincipalDetails;
import com.oneline.shimpyo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.oneline.shimpyo.security.jwt.JwtConstants.*;
import static com.oneline.shimpyo.security.jwt.JwtTokenUtil.generateRefreshToken;
import static com.oneline.shimpyo.security.jwt.JwtTokenUtil.generateToken;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@Component
@Slf4j
@Primary
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        PrincipalDetails member = (PrincipalDetails) authentication.getPrincipal();

        String accessToken = generateToken(member, true, AT_EXP_TIME);
        String refreshToken = generateRefreshToken(member, true, RT_EXP_TIME);

        // Refresh Token DB에 저장
        memberService.updateRefreshToken(member.getUsername(), refreshToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.addHeader("Set-Cookie", createCookie(refreshToken).toString());

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(AT_HEADER, accessToken);
        BaseResponse<Map<String, String>> mapBaseResponse = new BaseResponse<>(responseMap);
        new ObjectMapper().writeValue(response.getWriter(), mapBaseResponse);
    }

    public static ResponseCookie createCookie(String refreshToken) {

        ResponseCookie cookie = ResponseCookie.from(RT_HEADER, refreshToken)
                .path("/")
                .sameSite("Lax")
                .secure(false)
                .httpOnly(true)
                .maxAge(60 * 60 * 24).build();

//        Cookie cookie = new Cookie(RT_HEADER, refreshToken);
//        cookie.setHttpOnly(true);
////        cookie.setSecure(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(60 * 60 * 24);
        return cookie;
    }
}
