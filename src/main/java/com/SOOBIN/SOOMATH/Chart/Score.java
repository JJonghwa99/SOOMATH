package com.SOOBIN.SOOMATH.Chart;

import com.SOOBIN.SOOMATH.Member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scores")
@Getter
@Setter
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 성적 데이터와 회원 간의 관계 설정

    private String exam;   // 예: "1.1" (1주차 1회차)
    private Integer score; // 성적 값
    private String separated; //분반

    private String month;  // 예: "11" (11월)
}