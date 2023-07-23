package com.oneline.shimpyo.service;

import com.oneline.shimpyo.domain.house.dto.FileReq;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.member.dto.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface MemberService {
    // 회원가입
    void join(MemberReq member);

    // 이메일 중복체크
    boolean duplicateEmail(String email);

    // 본인인증 ( 이메일 )
    Member checkUser(String email);
    
    // 닉네임 중복체크
    boolean duplicateNickname(String nickname);

    void changePassword(ResetPasswordReq password);

    void certifiedPhoneNumber(String phoneNumber, String numStr);

    String findByEmailWithPhonNumber(String phoneNumber);

    void updateRefreshToken(String username, String refreshToken);

    Map<String, String> refresh(String refreshToken, HttpServletResponse response);

    boolean duplicatePhoneNumber(String phoneNumber);

    void removeRefreshToken(Long id);

    Member oauthJoin(OAuthInfoReq oAuthInfoReq);

    void findByMemberWithPhoneNumber(String phoneNumber);

    void updateNickname(String nickname, Long id);

    void updateEmail(String email, Long id);

    void updatePhoneNumber(String phoneNumber, Long id);

    void updatePassword(String password, Long id);

    void checkNonMemberReservation(NonMemberReservationInfoReq request);

    void certifiedNonMemberPhoneNumber(String phoneNumber, String reservationCode);

    void changeProfile(Member member, FileReq fileReq, String selfIntroduce);

    MemberProfileRes findMemberProfile(Long userId);

}
