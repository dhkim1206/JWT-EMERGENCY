package com.example.demo.config;


import com.example.demo.model.Authority;
import com.example.demo.model.Member;
import com.example.demo.model.MemberAuth;
import com.example.demo.model.dto.MemberReqDTO;
import com.example.demo.repository.AuthorityRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * initDataBaseForLocal 설명 : 로컬에서 테스트용으로 사용하기위해 데이터를 넣어 두기 위한 코드
 **/
@Profile("local") // local 용
@Component
@RequiredArgsConstructor
public class initDataBaseForLocal {

    private final initDataBaseForLocalService initDataBaseForLocalService;

    @PostConstruct
    private void init() {
        this.initDataBaseForLocalService.init();
    }

    @Component
    @RequiredArgsConstructor
    static class initDataBaseForLocalService {
        private final AuthService authService;
        private final MemberRepository memberRepository;
        private final AuthorityRepository authorityRepository;


        @Transactional
        public void init() {

            authorityRepository.save(new Authority(MemberAuth.ROLE_ADMIN));
            authorityRepository.save(new Authority(MemberAuth.ROLE_USER));


            authService.signup(new MemberReqDTO(
                    "admin@admin.com",
                    "1234",
                    "admin1"
            ));

            authService.signup(new MemberReqDTO(
                    "user@user.com",
                    "1234",
                    "user1"
            ));

            Member admin = memberRepository.findByEmail("admin@admin.com").get();
            Member user = memberRepository.findByEmail("user@user.com").get();

            admin.addAuthority(authorityRepository.findByAuthorityName(MemberAuth.ROLE_ADMIN).get());
            admin.activate(true);
            user.activate(true);
        }
    }
}
