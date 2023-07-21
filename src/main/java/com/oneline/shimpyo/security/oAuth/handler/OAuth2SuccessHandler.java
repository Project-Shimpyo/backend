package com.oneline.shimpyo.security.oAuth.handler;

import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.repository.MemberRepository;
import com.oneline.shimpyo.security.auth.PrincipalDetails;
import com.oneline.shimpyo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        Member member = principal.getMember();

        // 이미 회원
        if(member.getSocial()) {
            response.sendRedirect(UriComponentsBuilder.fromUriString("http://shimpyo.o-r.kr/social/login")
                    .queryParam("user_id", member.getId())
                    .build()
                    .encode(UTF_8)
                    .toUriString());
        }

        // 최초 로그인이라면 회원가입 창 redirect
        else {
            response.sendRedirect(UriComponentsBuilder.fromUriString("http://shimpyo.o-r.kr/social/add_info")
                    .queryParam("user_id", member.getId())
                    .build()
                    .encode(UTF_8)
                    .toUriString());
        }
    }
}
