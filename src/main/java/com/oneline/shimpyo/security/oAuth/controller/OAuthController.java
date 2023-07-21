package com.oneline.shimpyo.security.oAuth.controller;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.member.dto.OAuthInfoReq;
import com.oneline.shimpyo.repository.MemberRepository;
import com.oneline.shimpyo.security.oAuth.dto.OAuth2IdReq;
import com.oneline.shimpyo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.oneline.shimpyo.domain.BaseResponseStatus.MEMBER_NONEXISTENT;
import static com.oneline.shimpyo.domain.BaseResponseStatus.NICKNAME_DUPLICATE;
import static com.oneline.shimpyo.security.handler.CustomSuccessHandler.createCookie;
import static com.oneline.shimpyo.security.jwt.JwtConstants.*;
import static com.oneline.shimpyo.security.jwt.JwtTokenUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/api/oauth2-join")
    public BaseResponse<Void> oauthJoin(@RequestBody OAuthInfoReq oAuthInfoReq) {

        memberService.oauthJoin(oAuthInfoReq);
        // 엑세스, 리프레시 쿠키 담아서 반환
        return new BaseResponse<>();
    }


    @GetMapping("/api/social/login/{id}")
    public BaseResponse<Map<String, String>>  socialLogin(@PathVariable Long id, HttpServletResponse response) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));

        String accessToken = generateOAuth2Token(member, true, AT_EXP_TIME);
        String refreshToken = generateOAuth2Token(member, true, RT_EXP_TIME);

        // Refresh Token DB에 저장
        memberService.updateRefreshToken(member.getEmail(), refreshToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.addHeader("Set-Cookie", createCookie(refreshToken).toString());

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(AT_HEADER, accessToken);

        return new BaseResponse<>(responseMap);
    }

    @GetMapping("/api/oauth2-access")
    public BaseResponse<Map<String, String>> oauthToken(@RequestBody OAuth2IdReq request,
                                                        HttpServletResponse response) {
        Member member = memberRepository.findById(request.getId()).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));

        String accessToken = generateOAuth2Token(member, true, AT_EXP_TIME);
        String refreshToken = generateOAuth2Token(member, true, RT_EXP_TIME);

        // Refresh Token DB에 저장
        memberService.updateRefreshToken(member.getEmail(), refreshToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.addHeader("Set-Cookie", createCookie(refreshToken).toString());

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(AT_HEADER, accessToken);

        return new BaseResponse<>(responseMap);
    }

}
