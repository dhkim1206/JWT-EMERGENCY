package com.example.demo.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * UsernamePasswordAuthenticationToken을 대신하여 커스터 마이징한 토큰
 * UsernamePasswordAuthenticationToken의 상당부분을 그대로 가져옴
 */
public class CustomEmailPasswordAuthToken extends AbstractAuthenticationToken {

    private final Object principal;

    private Object credentials;

    public CustomEmailPasswordAuthToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    public CustomEmailPasswordAuthToken(Object principal, Object credentials,
                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
