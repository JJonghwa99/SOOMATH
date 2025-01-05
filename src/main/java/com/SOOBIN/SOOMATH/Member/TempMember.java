package com.SOOBIN.SOOMATH.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class TempMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; // 로그인할 때 쓸 아이디
    private String displayName; // 학생이름

    private String nickName; // 랭킹에 쓸 이름
    private String schoolName; // 학교이름

    @ElementCollection // 다중 선택 항목 지원
    private List<String> separated; // 분반

    private int grade; // 학년
    private boolean auth; // 인증여부
    private String password;
}
