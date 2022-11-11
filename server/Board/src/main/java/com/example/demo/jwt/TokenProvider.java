package com.example.demo.jwt;


import com.example.demo.exceptionhandler.AuthorityExceptionType;
import com.example.demo.exceptionhandler.BizException;
import com.example.demo.model.Authority;
import com.example.demo.model.dto.TokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ognl.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Component
public class TokenProvider {

    // JWT claim 정보에 들어갈 멤버의 권한을 나타내는 key 값. ex) auth : ROLE_USER
    private static final String AUTHORITIES_KEY = "auth";

    // HTTP Header 중 Authorization 필드에 들어가는 prefix로서 JWT를 쓴다면 "Bearer "(띄어쓰기 주의)를 사용하도록 하는 일종의 약속 같은 개념입니다.
    private static final String BEARER_TYPE = "Bearer";

    // application.yml 에서 가져오는 토큰의 유효기간과 토큰에 사인하기 위한 키값을 생성합니다.
    private final long ACCESS_TOKEN_EXPIRE_TIME; // 30분
    private final long REFRESH_TOKEN_EXPIRE_TIME; // 7일

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey ,
                         @Value("${jwt.access-token-expire-time}") long accessTime,
                         @Value("${jwt.refresh-token-expire-time}") long refreshTime
    ) {
        this.ACCESS_TOKEN_EXPIRE_TIME = accessTime;
        this.REFRESH_TOKEN_EXPIRE_TIME = refreshTime;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 사용자의 이메일과 권한들을 받아서 토큰을 생성
    // 클레임의 value 값으로 Set이 아니라 String으로 넣어뒀는데
    // 구현의 편의상 String이 편해서 String으로 권한들을 joining
    protected String createToken(String email, Set<Authority> auth, long tokenValid) {
        // ex) sub : abc@abc.com
        Claims claims = Jwts.claims().setSubject(email);

        // ex)  auth : ROLE_USER,ROLE_ADMIN
        claims.put(AUTHORITIES_KEY,
                auth.stream()
                        .map(Authority::getAuthorityName)
                        .collect(Collectors.joining(","))
        );

        // 현재시간
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims) // 토큰 발행 유저 정보
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(new Date(now.getTime() + tokenValid)) // 토큰 만료시간
                .signWith(key,SignatureAlgorithm.HS512) // 키와 알고리즘 설정
                .compact();
    }

    // 엑세스 토큰과 리프레시 토큰을 위 createToken 함수를 재 사용하여 생성
    /**
     * @param email
     * @param auth
     * @return 엑세스 토큰 생성
     */
    public String createAccessToken(String email,Set<Authority> auth) {
        return this.createToken(email,auth,ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     *
     * @param email
     * @param auth
     * @return 리프레시 토큰 생성
     */
    public String createRefreshToken(String email,Set<Authority> auth) {
        return this.createToken(email,auth,REFRESH_TOKEN_EXPIRE_TIME);
    }

    /**
     *
     * @param token
     * @return 토큰 값을 파싱하여 클레임에 담긴 이메일 값을 가져온다.
     */
    public String getMemberEmailByToken(String token) {
        // 토큰의 claim 의 sub 키에 이메일 값이 들어있다.
        return this.parseClaims(token).getSubject();
    }

    /**
     *
     * @param accessToken
     * @param refreshToken
     * @return TOEKN DTO를 생성한다.
     */
    public TokenDTO createTokenDTO(String accessToken, String refreshToken) {
        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType(BEARER_TYPE)
                .build();
    }
/** 토큰에서 Authentication 인스턴스를 가져오는 메서드
// CustomEmailPasswordAuthToken은 UsernamePasswordAuthenticationToken을 커스터마이징한 클래스.*/
    public Authentication getAuthentication(String accessToken) throws BizException{

        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null || !StringUtils.hasText(claims.get(AUTHORITIES_KEY).toString())) {
            throw new BizException(AuthorityExceptionType.NOT_FOUND_AUTHORITY); // 유저에게 아무런 권한이 없습니다.
        }

        log.debug("claims.getAuth = {}",claims.get(AUTHORITIES_KEY));
        log.debug("claims.getEmail = {}",claims.getSubject());

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        authorities.stream().forEach(o->{
            log.debug("getAuthentication -> authorities = {}",o.getAuthority());
        });
    /**
    *  UserDetails 객체를 만들어서 Authentication 리턴
    */
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new CustomEmailPasswordAuthToken(principal, "", authorities);
    }


    /** 토큰을 검사하는 메서드
     * Exception을 throw하지 않고 int 를 리턴한 이유는
     * try catch로 지저분해지는 필터의 코드를 좀 더 가독성있게 수정하기 위함인데
     * 더 좋은 방법이 있을 듯
    */
    public int validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return 1;
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            return 2;
        } catch (Exception e) {
            log.info("잘못된 토큰입니다.");
            return -1;
        }
    }


    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) { // 만료된 토큰이 더라도 일단 파싱을 함
            return e.getClaims();
        }
    }
}
