package com.oneline.shimpyo.security.filter;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.exception.ErrorResponse;
import com.oneline.shimpyo.repository.MemberRepository;
import com.oneline.shimpyo.security.auth.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

import static com.oneline.shimpyo.domain.BaseResponseStatus.JWT_TOKEN_WRONG;
import static com.oneline.shimpyo.security.jwt.JwtConstants.*;
import static com.oneline.shimpyo.security.jwt.JwtTokenUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.commons.lang.StringUtils.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("진입");
        String servletPath = request.getServletPath();
        String token = null;

        if(servletPath.equals("/api/refresh"))
            token = extractRefreshToken(request);
        else
            token = extractToken(request);

        // 회원 전용 인증 절차
        if(!isEmpty(token)) {
            try {
                if(validateToken(token)) {
                    String username = getUsernameFromToken(token);
                    Member member = memberRepository.findByEmail(username);
                    PrincipalDetails principalDetails = new PrincipalDetails(member);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                else {
                    log.info("JWT 토큰이 잘못되었습니다.");
                    response.setStatus(SC_BAD_REQUEST);
                    response.setContentType(APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("utf-8");
                    BaseResponse<Void> baseResponse = new BaseResponse<>(JWT_TOKEN_WRONG);
                    new ObjectMapper().writeValue(response.getWriter(), baseResponse);
                    return;
                }
            } catch (TokenExpiredException e) {
                log.info("Access Token이 만료되었습니다.");
                response.setStatus(SC_UNAUTHORIZED);
                response.setContentType(APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("utf-8");
                ErrorResponse errorResponse = new ErrorResponse(401, "Access Token이 만료되었습니다.");
                new ObjectMapper().writeValue(response.getWriter(), errorResponse);
            } catch (Exception e) {
                log.info("JWT 토큰이 잘못되었습니다. message : {}", e.getMessage());
                response.setStatus(SC_BAD_REQUEST);
                response.setContentType(APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("utf-8");

                ErrorResponse errorResponse = new ErrorResponse(400, "잘못된 JWT Token 입니다.");
                new ObjectMapper().writeValue(response.getWriter(), errorResponse);
            }
        }

        else if(isEmpty(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication authentication = createAnonymousAuthentication();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Authentication createAnonymousAuthentication() {
        String key = "myAnonymousKey";
        String principal = "anonymousUser";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));

        return new AnonymousAuthenticationToken(key, principal, authorities);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String value = null;
        for (Cookie cookie : cookies) {
            value = cookie.getValue();
        }
        if(value != null)
            return value;

        return null;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_HEADER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

}