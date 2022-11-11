package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    /**
     * @return PasswordEncoder - Spring5 부터 인코더 생성 방법이 변경
     *  다양한 암호화 알고리즘을 변경 가능
     *   처음에는 SecurityConfig에 빈으로 생성하였으나 SecurityConfig 에서 CustomEmailPasswordAuthProvider를 의존하고
     *   CustomEmailPasswordAuthProvider는 passwordEncoder(SecurityConfig)에 의존하여 순환참조가 일어나
     *   따로 빈을 만드는 AppConfig를 생성하
     *
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // Default 사용
    }
}