package com.SOOBIN.SOOMATH.Member;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Separated {
    @Id
    @Column(unique = true) // 중복을 방지하여 1-1 같은 값을 가질 수 있도록 설정
    private String sCode;
    private String sName; // 강좌 이름
    @ElementCollection
    private List<String> sMonth;
}

