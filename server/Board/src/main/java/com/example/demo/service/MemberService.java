package com.example.demo.service;


import com.example.demo.exceptionhandler.BizException;
import com.example.demo.exceptionhandler.MemberExceptionType;
import com.example.demo.model.Member;
import com.example.demo.model.dto.MemberRespDTO;
import com.example.demo.model.dto.MemberUpdateDTO;
import com.example.demo.repository.MemberRepository;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;


/**
 * 멤버 등록 및 조회
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     *
     * @param email
     * @return email에 해당하는 멤버의 정보를 반환한다.
     */
    @Transactional(readOnly = true)
    public MemberRespDTO getMemberInfo(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberRespDTO::of)
                .orElseThrow(()-> new BizException(MemberExceptionType.NOT_FOUND_USER)); // 유저를 찾을 수 없습니다.
    }

    /**
     * @return 현재 securityContext에 있는 유저 정보를 반환한다.
     */
    @Transactional(readOnly = true)
    public MemberRespDTO getMyInfo() {
        return memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .map(MemberRespDTO::of)
                .orElseThrow(()->new BizException(MemberExceptionType.NOT_FOUND_USER));
    }


    /**
     *
     * @param MemberUpdateDTO
     * DirtyChecking 을 통한 멤버 업데이트 ( Email은 업데이트 할 수 없다.)
     */
    @Transactional
    public void updateMemberInfo(MemberUpdateDTO dto) {
        Member member = memberRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new BizException(MemberExceptionType.NOT_FOUND_USER));

        member.updateMember(dto,passwordEncoder);
    }

}
