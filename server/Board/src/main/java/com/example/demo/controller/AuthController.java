package com.example.demo.controller;

import com.example.demo.model.dto.*;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT인증과정과 똑같다.
 * 다음 차례는 JwtFilter 설정
 * 로그인과 재발행 요청에 대해서는 토큰에 대해 검사하면 안되고 그외 모든 요청에 대해서는 검사를 해야한다.
 * 그리고 filter에서 한번 검사하고 난 뒤 requestDispatcher에 의해 다른 요청으로 forward 된다면
 * 또 다시 filter에서 검사할 수 있기 때문에 한 번 들어온 요청에 대해서는 한번만 인증을 거치도록 하는 필터를 구현해야함
 * 이를 위해서는 OncePerRequestFilter 를 확장하여 구현하면됨
 * 만들 필터의 이름은 JwtFilter
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public MemberRespDTO signup(@RequestBody MemberReqDTO memberRequestDto) {
        log.debug("memberRequestDto = {}",memberRequestDto);
        return authService.signup(memberRequestDto);
    }

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginReqDTO loginReqDTO) {
        return authService.login(loginReqDTO);
    }

    @PostMapping("/reissue")
    public TokenDTO reissue(@RequestBody TokenReqDTO tokenRequestDto) {
        return authService.reissue(tokenRequestDto);
    }
}