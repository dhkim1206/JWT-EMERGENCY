package com.example.demo.controller;


import com.example.demo.model.dto.MemberRespDTO;
import com.example.demo.model.dto.MemberUpdateDTO;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("")
    public MemberRespDTO getMyInfo() {
        return memberService.getMyInfo();
    }

    @GetMapping("/{email}")
    public MemberRespDTO getMemberInfo(@PathVariable String email) {
        return memberService.getMemberInfo(email);
    }

    @PutMapping("")
    public void updateMember(@RequestBody MemberUpdateDTO dto) {
        memberService.updateMemberInfo(dto);
    }

    /**
     * @PreAuthorize 는 ControllerAdvice에 의해 에러핸들링됨
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admintest")
    public String adminTest() {
        return "ADMIN OK!";
    }
}
