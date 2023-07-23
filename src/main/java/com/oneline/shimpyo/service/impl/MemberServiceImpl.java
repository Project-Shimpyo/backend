package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.house.dto.FileReq;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.member.MemberGrade;
import com.oneline.shimpyo.domain.member.MemberImage;
import com.oneline.shimpyo.domain.member.dto.*;
import com.oneline.shimpyo.repository.MemberRepository;
import com.oneline.shimpyo.repository.NonMemberReservationRepository;
import com.oneline.shimpyo.repository.dsl.MemberQuerydsl;
import com.oneline.shimpyo.security.CustomBCryptPasswordEncoder;
import com.oneline.shimpyo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;
import static com.oneline.shimpyo.domain.member.GradeName.*;
import static com.oneline.shimpyo.security.handler.CustomSuccessHandler.createCookie;
import static com.oneline.shimpyo.security.jwt.JwtConstants.*;
import static com.oneline.shimpyo.security.jwt.JwtTokenUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberQuerydsl memberQuerydsl;
    private final EntityManager em;
    private final CustomBCryptPasswordEncoder bCryptPasswordEncoder;
    private final NonMemberReservationRepository nonMemberReservationRepository;

    @Override
    @Transactional(readOnly = false)
    public void join(MemberReq request) {
        MemberGrade memberGrade = new MemberGrade(SILVER, 3);
        em.persist(memberGrade);

        Member member = memberRepository.save(new Member(request, bCryptPasswordEncoder, memberGrade));

        MemberImage memberImage = MemberImage.builder()
                .member(member)
                .originalFileName(" ")
                .savedFileName(" ")
                .savedPath(" ")
                .savedPath(" ").build();
        member.setMemberImage(memberImage);

    }

    @Override
    public boolean duplicateEmail(String email) {
        Member findEmail = memberRepository.findByEmail(email);
        //  단건이 아니라 List 로 조회가 되서 서버쪽에서 에러가 난거에요
        if(findEmail == null) return true;  // 없으면
        return false;   // 있으면
    }

    @Override
    public Member checkUser(String email) {
        Member findUser = memberRepository.findByEmail(email);
        return findUser;
    }

    @Override
    public boolean duplicateNickname(String nickname) {
        Member findNickname = memberRepository.findByNickname(nickname);
        if(findNickname == null) return true;   // 없으면
        return false;   // 있으면
    }

    @Override
    @Transactional
    public void changePassword(ResetPasswordReq request) {
        Member findMember = memberQuerydsl.findByMemberWithPhoneNumber(request.getPhoneNumber());

        findMember.resetPassword(request.getPassword(), bCryptPasswordEncoder);
    }

    @Override
    @Transactional
    public void certifiedPhoneNumber(String phoneNumber, String cerNum) {
        String api_key = "NCSTRVCRRQWQRTLB";    // API 키
        String api_secret = "KCGD2SG3U7E0J3OONGYZYYKXP5SOS3Y3"; // API 시크릿 키
        Message coolsms = new Message(api_key, api_secret);

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNumber);    // 수신전화번호
        params.put("from", "01065991802");    // 발신전화번호. 테스트시에는 발신,수신 둘다 본인 번호로 하면 됨
        params.put("type", "SMS");
        params.put("text", "인증번호는" + "["+cerNum+"]" + "입니다.");
        params.put("app_version", "test app 1.2");
        
        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
            log.info(obj.toString());
        } catch (CoolsmsException e) {
            log.info(e.getMessage());
            log.info(String.valueOf(e.getCode()));
        }
    }

    @Override
    public String findByEmailWithPhonNumber(String phoneNumber) {
        return memberQuerydsl.findByEmailListWithPhoneNumber(phoneNumber)
                .stream().filter(o -> o.getSocial() == false)
                .findFirst()
                .orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT)).getEmail();
    }

    @Override
    @Transactional(readOnly = false)
    public void updateRefreshToken(String username, String refreshToken) {
        Member findMember = memberRepository.findByEmail(username);
        if(findMember == null)
           throw new RuntimeException("사용자를 찾을 수 없습니다.");

        findMember.updateRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public Map<String, String> refresh(String refreshToken, HttpServletResponse response) {
        Member member = memberRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new BaseException(JWT_TOKEN_WRONG));

        // === Refresh Token 유효성 검사 === //
        boolean verifyRefreshToken = verifyRefreshToken(refreshToken);

        if(!verifyRefreshToken) new BaseResponse<>(JWT_REFRESH_WRONG);

        // === Access Token 재발급 === //
        long now = System.currentTimeMillis();

        if(member == null)
            new BaseResponse<>(MEMBER_NONEXISTENT);

        String accessToken
                = reissuanceAccessToken(member, true, AT_EXP_TIME, now);

        Map<String, String> accessTokenResponseMap = new HashMap<>();

        String newRefreshToken = ressuanceRefreshToken(member, true, RT_EXP_TIME, now);
        response.addHeader("Set-Cookie", createCookie(newRefreshToken).toString());
        member.updateRefreshToken(newRefreshToken);

        accessTokenResponseMap.put(AT_HEADER, accessToken);
        accessTokenResponseMap.put("profile", member.getMemberImage().getSavedPath());
        accessTokenResponseMap.put("nickname", member.getNickname());
        return accessTokenResponseMap;
    }

    @Override
    public boolean duplicatePhoneNumber(String phoneNumber) {
        Member findMember = memberRepository.findByMemberWithPhoneNumber(phoneNumber, false);
        if(findMember != null)
            return false;

        return true;
    }

    @Override
    @Transactional
    public void removeRefreshToken(Long id) {
        memberRepository.removeRefreshToken(id);
    }

    @Override
    @Transactional
    public Member oauthJoin(OAuthInfoReq oAuthInfoReq) {
        Member member = memberRepository.findById(oAuthInfoReq.getId()).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));

        member.oAuthJoin(oAuthInfoReq.getPhoneNumber(), oAuthInfoReq.getNickname());
        return member;
    }

    @Override
    public void findByMemberWithPhoneNumber(String phoneNumber) {
        Member member = memberRepository.findByMemberWithPhoneNumber(phoneNumber, false);

        if(member == null) {
            throw new BaseException(MEMBER_NONEXISTENT);
        }
    }

    @Override
    @Transactional
    public void updateNickname(String nickname, Long id) {
        memberRepository.findById(id).get().setNickname(nickname);
    }

    @Override
    @Transactional
    public void updateEmail(String email, Long id) {
        memberRepository.findById(id).get().setEmail(email);
    }

    @Override
    @Transactional
    public void updatePhoneNumber(String phoneNumber, Long id) {
        memberRepository.findById(id).get().setPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional
    public void updatePassword(String password, Long id) {
        memberRepository.findById(id).get().setPassword(bCryptPasswordEncoder.encode(password));
    }

    @Override
    public void checkNonMemberReservation(NonMemberReservationInfoReq request) {
        nonMemberReservationRepository.findByMerchantUid(request.getReservationCode())
                .orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));
    }

    @Override
    public void certifiedNonMemberPhoneNumber(String phoneNumber, String reservationCode) {
        String api_key = "NCSTRVCRRQWQRTLB";    // API 키
        String api_secret = "KCGD2SG3U7E0J3OONGYZYYKXP5SOS3Y3"; // API 시크릿 키
        Message coolsms = new Message(api_key, api_secret);

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNumber);    // 수신전화번호
        params.put("from", "01065991802");    // 발신전화번호. 테스트시에는 발신,수신 둘다 본인 번호로 하면 됨
        params.put("type", "SMS");
        params.put("text", "예약번호는" + "["+reservationCode+"]" + "입니다.");
        params.put("app_version", "test app 1.2");

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
            log.info(obj.toString());
        } catch (CoolsmsException e) {
            log.info(e.getMessage());
            log.info(String.valueOf(e.getCode()));
        }
    }

    @Override
    @Transactional
    public void changeProfile(Member member, FileReq fileReq, String selfIntroduce) {
        Member findMember = memberRepository.findById(member.getId()).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));

        MemberImage memberImage = MemberImage.builder()
                .member(findMember)
                .originalFileName(fileReq.getOriginalFileName())
                .savedFileName(fileReq.getOriginalFileName())
                .savedPath(fileReq.getSavedURL())
                .savedPath(fileReq.getSavedURL()).build();
        findMember.setMemberImage(memberImage);
        findMember.setComments(selfIntroduce);
    }
    
    @Override
    public MemberProfileRes findMemberProfile(Long memberId) {
        return memberQuerydsl.findMemberProfile(memberId);
    }

}
