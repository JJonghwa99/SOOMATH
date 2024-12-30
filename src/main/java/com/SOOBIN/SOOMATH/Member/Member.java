package com.SOOBIN.SOOMATH.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id; // 수정된 부분
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; // 로그인할 때 쓸 아이디
    private String displayName; // 학생이름
    private String schoolName; // 학교이름
    private int grade; // 학년
    private boolean auth; // 인증여부
    private String password;
}
