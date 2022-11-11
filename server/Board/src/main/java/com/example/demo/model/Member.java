package com.example.demo.model;

import com.example.demo.model.dto.MemberUpdateDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
@Entity
public class Member {

    @JsonIgnore
    @Column(name = "member_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "username",length = 50,nullable = false)
    private String username;

    // Email 을 토큰의 ID로 관리하기 때문에 unique = True
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @JsonIgnore
    @Column(name = "activated")
    private boolean activated;

    @ManyToMany // 다대다 관계 별로 안좋아서 실무에서 잘 안씀,  ONETOMANY로 구조 바꿔야함
    @JoinTable(
            name = "member_authority",
            joinColumns = {@JoinColumn(name="member_id",referencedColumnName = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name",referencedColumnName = "authority_name")}
    )
    private Set<Authority> authorities = new HashSet<>();

    @Builder
    public Member(String username, String email, String password, boolean activated,Set<Authority> authorities) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.activated = activated;
        this.authorities = authorities;
    }

    public void addAuthority(Authority authority) {
        this.getAuthorities().add(authority);
    }

    public void removeAuthority(Authority authority) {
        this.getAuthorities().remove(authority);
    }

    public void activate(boolean flag) {
        this.activated = flag;
    }

    public String getAuthoritiesToString() {
        return this.authorities.stream()
                .map(Authority::getAuthorityName)
                .collect(Collectors.joining(","));
    }

    public void updateMember(MemberUpdateDTO dto, PasswordEncoder passwordEncoder) {
        if(dto.getPassword() != null) this.password = passwordEncoder.encode(dto.getPassword());
        if(dto.getUsername() != null) this.username = dto.getUsername();
        if(dto.getAuthorities().size() > 0) {
            this.authorities = dto.getAuthorities().stream()
                    .filter(MemberAuth::containsKey)
                    .map(MemberAuth::get)
                    .map(Authority::new)
                    .collect(Collectors.toSet());
        }
    }
}
