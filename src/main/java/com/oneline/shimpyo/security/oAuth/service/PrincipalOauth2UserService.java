package com.oneline.shimpyo.security.oAuth.service;

import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.repository.MemberRepository;
import com.oneline.shimpyo.security.CustomBCryptPasswordEncoder;
import com.oneline.shimpyo.security.auth.PrincipalDetails;
import com.oneline.shimpyo.security.oAuth.OAuthAttributes;
import com.oneline.shimpyo.security.oAuth.profile.MemberProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final CustomBCryptPasswordEncoder bCryptPasswordEncoder;

    private final EntityManager em;

    @Autowired
    private MemberRepository memberRepository;
    
    @Transactional
    // 코드를 보내서 엑세스토큰으로 소셜쪽에 요청해서 유저 정보를 받아온 상태
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest); // OAuth 서비스(kakao, google, naver)에서 가져온 유저 정보를 담고있음

        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId(); // OAuth 서비스 이름(ex. kakao, naver, google)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName(); // OAuth 로그인 시 키(pk)가 되는 값

        Map<String, Object> attributes = oAuth2User.getAttributes(); // OAuth 서비스의 유저 정보들

        MemberProfile memberProfile = OAuthAttributes.extract(registrationId, attributes); // registrationId에 따라 유저 정보를 통해 공통된 UserProfile 객체로 만들어 줌
        memberProfile.setProvider(registrationId);

        Member member = saveOrUpdate(memberProfile);

        Map<String, Object> customAttribute = customAttribute(attributes, userNameAttributeName, memberProfile, registrationId);

        return new PrincipalDetails(member, customAttribute);
    }

    private Map customAttribute(Map attributes, String userNameAttributeName, MemberProfile memberProfile, String registrationId) {
        Map<String, Object> customAttribute = new LinkedHashMap<>();
        customAttribute.put(userNameAttributeName, attributes.get(userNameAttributeName));
        customAttribute.put("provider", registrationId);
        customAttribute.put("email", memberProfile.getEmail());
        return customAttribute;
    }

    // 가입
    @Transactional
    private Member saveOrUpdate(MemberProfile memberProfile) {
        Member member = memberRepository.findByEmailAndProvider(memberProfile.getEmail(), memberProfile.getProvider())
                .map(m -> m.update(memberProfile.getEmail())) // OAuth 서비스 사이트에서 유저 정보 변경이 있을 수 있기 때문에 우리 DB에도 update
                .orElse(memberProfile.toMember(em, bCryptPasswordEncoder)); // 정보가 없다면 신규회원임으로 가입

        if(member.getSocial()) {
           log.info("이미 회원가입 되있는 사용자");
           return member;
        }
        else {
            log.info("최초 로그인");
            return memberRepository.save(member);
        }
    }
}
