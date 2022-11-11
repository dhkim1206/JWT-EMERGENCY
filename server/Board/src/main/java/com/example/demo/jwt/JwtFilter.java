package com.example.demo.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JwtFilter 설명 : 디스패처 포워딩이 되어도 단 한번만 실행되는 필터
 * /auth 로 시작하는 모든 요청은 그냥 통과
 * 그외에는 토큰의 값을 검사
 * reponse에 직접 에러의 정보를 하드 코딩했는데
 * 이는 Filter에서는 @ExceptionHandler가 먹히지 않기 때문
 * 좀 더 좋은 방법이 있을 듯
 *
 * JwtFilter를 SpringSecurity 설정에 추가할거임
 * 커스터마이징한 CustomEmailPasswordAuthProvider는 AuthenticationManagerBuilder를 통해서 추가 가능
 *
 * UsernamePasswordAuthenticationFilter 앞에 추가한 이유는 딱히 없지만 SecurityContext를 쓰기 위해서
 * 앞단의 필터들을 지나야 하므로 UsernamePasswordAuthenticationFilter에서도 SecurityContext를 사용하는 것으로 보아
 * (정확히는 AbstractAuthenticationProcessingFilter의 successfulAuthentication 메서드)
 * UsernamePasswordAuthenticationFilter 이전에 추가하는 사람들이 많은 듯
 **/

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    // 실제 필터링 로직은 doFilterInternal 에 들어감
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if(request.getServletPath().startsWith("/auth")) {
            filterChain.doFilter(request,response);
        }else {
            String token = resolveToken(request);

            log.debug("token  = {}",token);
            if(StringUtils.hasText(token)) {
                int flag = tokenProvider.validateToken(token);

                log.debug("flag = {}",flag);
                // 토큰 유효함
                if(flag == 1) {
                    this.setAuthentication(token);
                }else if(flag == 2) { // 토큰 만료
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter out = response.getWriter();
                    log.debug("doFilterInternal Exception CALL!");
                    out.println("{\"error\": \"ACCESS_TOKEN_EXPIRED\", \"message\" : \"엑세스토큰이 만료되었습니다.\"}");
                }else { //잘못된 토큰
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter out = response.getWriter();
                    log.debug("doFilterInternal Exception CALL!");
                    out.println("{\"error\": \"BAD_TOKEN\", \"message\" : \"잘못된 토큰 값입니다.\"}");
                }
            }
            else {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                out.println("{\"error\": \"EMPTY_TOKEN\", \"message\" : \"토큰 값이 비어있습니다.\"}");
            }
        }
    }

    /**
     *
     * @param token
     * 토큰이 유효한 경우 SecurityContext에 저장
     */
    private void setAuthentication(String token) {
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Request Header 에서 토큰 정보를 꺼내오기
    private String resolveToken(HttpServletRequest request) {
        // bearer : 123123123123123 -> return 123123123123123123
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
